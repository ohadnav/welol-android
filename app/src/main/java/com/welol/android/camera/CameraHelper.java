package com.welol.android.camera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Process;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.Surface;
import com.affectiva.android.affdex.sdk.Frame;
import com.welol.android.app.App;
import com.welol.android.util.AppUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * The CameraHelper class encapsulates interaction with the Camera, including configuring and
 * coordinating previewing in a way that is optimized for use with FrameDetector.  Preview
 * frames are delivered through the Listener callback interface.
 */
@SuppressWarnings("deprecation") public class CameraHelper {
  public static final int TARGET_FRAME_RATE = 30;
  private final String TAG = this.getClass().getSimpleName();
  private SafeCamera safeCamera;
  private int displayRotation;
  private Listener listener = null;
  private boolean isPreviewing = false;
  private Display display;
  private Frame.ROTATE frameRotation;
  private int previewWidth;
  private int previewHeight;
  private OrientationHelper orientationHelper;
  private CameraPreviewer cameraPreviewer;
  Camera.PreviewCallback mOneShotPreviewCallback = new Camera.PreviewCallback() {
    @Override public void onPreviewFrame(byte[] data, Camera camera) {
      if (listener != null) {
        listener.onFrameAvailable(data, previewWidth, previewHeight, frameRotation);
      }
      setupPreviewWithCallbackBuffers();
    }
  };
  private Context mContext;
  private boolean mRecording = false;
  private boolean mPaused = false;
  private int mRecordingFrameIndex = 0;
  private long mRecordingId = 0;
  private File mRecordingDir;
  private long mRecordingDurationMs;
  private long mRecordingLastStart;

  public CameraHelper(@NonNull Context context, @NonNull Display display,
      @NonNull Listener listener) {
    if (!checkPermission(context)) {
      throw new IllegalStateException("app does not have camera permission");
    }
    mContext = context;
    this.display = display;
    this.listener = listener;
    displayRotation = display.getRotation();
    frameRotation = Frame.ROTATE.NO_ROTATION;
    orientationHelper = new OrientationHelper(mContext);
    cameraPreviewer = new CameraPreviewer();
    App.setCameraHelper(this);
  }

  private static boolean checkPermission(Context context) {
    return context.checkPermission(Manifest.permission.CAMERA, Process.myPid(), Process.myUid())
        == PackageManager.PERMISSION_GRANTED;
  }

  /**
   * Configures the acquired camera for use with the Affdex SDK, and starts previewing.  Preview
   * frames will be delivered to the listener.
   */
  public void start(@NonNull SurfaceTexture texture) {
    Log.d(TAG, "CameraHelper.start()");
    if (safeCamera == null) {
      throw new IllegalStateException("acquire a camera before calling the start method");
    }
    if (!isPreviewing) {
      initCameraParams();
      Camera.Parameters params = safeCamera.getParameters();
      previewWidth = params.getPreviewSize().width;
      previewHeight = params.getPreviewSize().height;
      setCameraDisplayOrientation();
      startPreviewing(texture);
    }
  }

  public String startOrResumeRecording() {
    if (mRecording || mPaused) {
      if (!mRecording) {
        Log.d(TAG, "resume recording");
        mRecordingLastStart = new Date().getTime();
      }
      mRecording = true;
      mPaused = false;
      return mRecordingDir.getAbsolutePath();
    }
    Log.d(TAG, "start recording");
    // Delete previous recording dir if not currently recording.
    try {
      File previousRecordingDir = new File(mContext.getCacheDir(), getRecordingDir());
      if (previousRecordingDir.isDirectory()) {
        Log.d(TAG, "deleting previous recording");
        for (File file : previousRecordingDir.listFiles()) {
          file.delete();
        }
      }
      previousRecordingDir.delete();
    } catch (Exception ignored) {
    }
    mRecordingLastStart = new Date().getTime();
    mRecordingDurationMs = 0;
    mRecording = true;
    mRecordingFrameIndex = 0;
    mRecordingId = new Date().getTime();
    mRecordingDir = new File(mContext.getCacheDir(), getRecordingDir());
    if (!mRecordingDir.mkdir()) {
      AppUtil.handleThrowable(new IOException("Could not make recording dir."));
    }
    return mRecordingDir.getAbsolutePath();
  }

  public long stopRecording() {
    if (mRecording) {
      // If a "true" stop than increase duration.
      mRecordingDurationMs += new Date().getTime() - mRecordingLastStart;
    }
    Log.d(TAG, "stop recording - " + generateRecordingStats());
    mRecording = false;
    mPaused = false;
    return mRecordingDurationMs;
  }

  public void pauseRecording() {
    if (mRecording) {
      Log.d(TAG, "pause recording - " + generateRecordingStats());
      // If a "true" pause than increase duration.
      mRecordingDurationMs += new Date().getTime() - mRecordingLastStart;
    }
    mRecording = false;
    mPaused = true;
  }

  /**
   * Stops and releases the camera.
   */
  public void stop() {
    Log.d(TAG, "CameraHelper.stop()");
    if (isPreviewing) {
      stopPreviewing();
    }
  }

  /**
   * attempts to open the specified camera
   *
   * @param cameraToOpen one of CameraInfo.
   */
  public void acquire(int cameraToOpen) {
    safeCamera = new SafeCamera(cameraToOpen);
  }

  /**
   * Releases the acquired camera
   */
  public void release() {
    if (safeCamera != null) {
      safeCamera.release();
      safeCamera = null;
    }
  }

  @NonNull private String generateRecordingStats() {
    return "recorded a total of "
        + mRecordingFrameIndex
        + " frames for "
        + mRecordingDurationMs
        + "ms, which is "
        + mRecordingFrameIndex * 1000 / (float) mRecordingDurationMs
        + " FPS.";
  }

  @NonNull private String getRecordingDir() {
    return "recording_" + mRecordingId;
  }

  private void setupPreviewWithCallbackBuffers() {
    // calculate the size for the callback buffers
    Camera.Parameters params = safeCamera.getParameters();
    int previewFormat = params.getPreviewFormat();
    int bitsPerPixel = ImageFormat.getBitsPerPixel(previewFormat);
    Camera.Size size = params.getPreviewSize();

    int bufSize = size.width * size.height * bitsPerPixel / 8;

    // add two buffers to the queue, so the camera can be working with one, while the callback is working with the
    // other. The callback will put each buffer it receives back into the buffer queue when it's done, so the
    // camera can use it again.
    safeCamera.addCallbackBuffer(new byte[bufSize]);
    safeCamera.addCallbackBuffer(new byte[bufSize]);

    safeCamera.setPreviewCallbackWithBuffer(cameraPreviewer);
  }

  /*
    Starts camera preview.
    Method should only be called when state is CameraHelperState.STARTED
 */
  private void startPreviewing(@NonNull SurfaceTexture texture) {
    Log.d(TAG, "startPreviewing");
    try {
      safeCamera.setPreviewTexture(texture);
    } catch (IOException e) {
      AppUtil.handleThrowable(e);
      Log.e(TAG, "Unable to start camera preview  " + e.getMessage());
    }

    orientationHelper.enable();

    // setPreviewCallbackWithBuffer only seems to work if you establish it after the first onPreviewFrame callback
    // (otherwise it never gets called back at all). So, use a one-shot callback for the first one, then
    // swap in the callback that uses the buffers.
    safeCamera.setOneShotPreviewCallback(mOneShotPreviewCallback);

    isPreviewing = true;

    try {
      safeCamera.startPreview();
    } catch (Exception e) {
      AppUtil.handleThrowable(e);
      Log.e(TAG, "Failed to start preview!");
      stopPreviewing();
    }
  }

  /*
      Stops camera preview.
      Method should only be called when state is CameraHelperState.STARTED
   */
  private void stopPreviewing() {
    Log.d(TAG, "stopPreviewing");
    if (isPreviewing) {
      safeCamera.stopPreview();
      safeCamera.setPreviewCallback(null);
      orientationHelper.disable();
    }
    isPreviewing = false;
  }

  // Make the camera image show in the same orientation as the display.
  // This code is partially based on sample code at http://developer.android.com/reference/android/hardware/Camera.html
  private void setCameraDisplayOrientation() {

    int degrees = 0;
    switch (displayRotation) {
      case Surface.ROTATION_0:
        degrees = 0;
        break;
      case Surface.ROTATION_90:
        degrees = 90;
        break;
      case Surface.ROTATION_180:
        degrees = 180;
        break;
      case Surface.ROTATION_270:
        degrees = 270;
        break;
    }

    int rotation;
    Camera.CameraInfo info = safeCamera.getCameraInfo();

    if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
      //determine amount to rotate image and call computeFrameRotation()
      //to have the Frame.ROTATE object ready for CameraDetector to use
      rotation = (info.orientation + degrees) % 360;

      computeFrameRotation(rotation);

      //Android mirrors the image that will be displayed on screen, but not the image
      //that will be sent as bytes[] in onPreviewFrame(), so we compensate for mirroring after
      //calling computeFrameRotation()
      rotation = (360 - rotation) % 360; // compensate the mirror
    } else { // back-facing
      //determine amount to rotate image and call computeFrameRotation()
      //to have the Frame.ROTATE object ready for CameraDetector to use
      rotation = (info.orientation - degrees + 360) % 360;

      computeFrameRotation(rotation);
    }
    safeCamera.setDisplayOrientation(rotation);

    //Now that rotation has been determined (or updated) inform mListener of new frame size.
    if (listener != null) {
      listener.onFrameSizeSelected(previewWidth, previewHeight, frameRotation);
    }
  }

  private void computeFrameRotation(int rotation) {
    switch (rotation) {
      case 0:
        frameRotation = Frame.ROTATE.NO_ROTATION;
        break;
      case 90:
        frameRotation = Frame.ROTATE.BY_90_CW;
        break;
      case 180:
        frameRotation = Frame.ROTATE.BY_180;
        break;
      case 270:
        frameRotation = Frame.ROTATE.BY_90_CCW;
        break;
      default:
        frameRotation = Frame.ROTATE.NO_ROTATION;
    }
  }

  private void initCameraParams() {
    Camera.Parameters cameraParams = safeCamera.getParameters();

            /* dump camera params to logcat - useful when debugging
            String flattened = cameraParams.flatten();
            StringTokenizer tokenizer = new StringTokenizer(flattened, ";");
            Log.d(TAG, "Dump all camera parameters:");
            while (tokenizer.hasMoreElements()) {
                Log.d(TAG, tokenizer.nextToken());
            }
            */

    setOptimalPreviewFrameRate(cameraParams);
    setOptimalPreviewSize(cameraParams, 480);
    safeCamera.setParameters(cameraParams);
  }

  //Sets camera frame to be as close to TARGET_FRAME_RATE as possible
  private void setOptimalPreviewFrameRate(@NonNull Camera.Parameters cameraParams) {
    int targetHiMS = 1000 * TARGET_FRAME_RATE;

    List<int[]> ranges = cameraParams.getSupportedPreviewFpsRange();
    if (ranges == null || ranges.size() <= 1) {
      return; // no options or only one option: no need to set anything.
    }

    int[] optimalRange = null;
    int minDiff = Integer.MAX_VALUE;
    for (int[] range : ranges) {
      int currentDiff = Math.abs(range[1] - targetHiMS);
      if (optimalRange == null || currentDiff <= minDiff) {
        optimalRange = range;
        minDiff = currentDiff;
      }
    }

    if (optimalRange == null) {
      // This should not be reachable, but satisfying a Lint warning about possible null value
      return;
    }

    cameraParams.setPreviewFpsRange(optimalRange[0],
        optimalRange[1]); // this will take the biggest lo range.
  }

  // Finds the closest height - simple algo. NOTE: only height is used as a target, width is ignored!
  private void setOptimalPreviewSize(@NonNull Camera.Parameters cameraParams, int targetHeight) {
    List<Camera.Size> supportedPreviewSizes = cameraParams.getSupportedPreviewSizes();
    // according to Android bug #6271, the emulator sometimes returns null from getSupportedPreviewSizes,
    // although this shouldn't happen on a real device.
    // See https://code.google.com/p/android/issues/detail?id=6271
    if (null == supportedPreviewSizes || supportedPreviewSizes.isEmpty()) {
      Log.d(TAG, "Camera returning null for getSupportedPreviewSizes(), will use default");
      return;
    }

    Camera.Size optimalSize = null;
    double minDiff = Double.MAX_VALUE;

    for (Camera.Size size : supportedPreviewSizes) {
      double currentDiff = Math.abs(size.height - targetHeight);
      if (optimalSize == null || currentDiff < minDiff) {
        optimalSize = size;
        minDiff = currentDiff;
      }
    }

    if (optimalSize == null) {
      // This should not be reachable, but satisfying a Lint warning about possible null value
      return;
    }

    cameraParams.setPreviewSize(optimalSize.width, optimalSize.height);
  }

  public interface Listener {
    void onFrameAvailable(byte[] frame, int width, int height, Frame.ROTATE rotation);

    @SuppressWarnings("EmptyMethod") void onFrameSizeSelected(int width, int height,
        Frame.ROTATE rotation);
  }

  private class OrientationHelper extends OrientationEventListener {

    public OrientationHelper(Context context) {
      super(context);
    }

    // If you quickly rotate 180 degrees, Activity does not restart, so you need this orientation Listener.
    @Override public void onOrientationChanged(int orientation) {
      // this method gets called for every tiny 1 degree change in orientation, so it's called really often
      // if the device is handheld. We don't need to reset the camera display orientation unless there
      // is a change to the display rotation (i.e. a 90/180/270 degree switch).
      if (display.getRotation() != displayRotation) {
        displayRotation = display.getRotation();
        setCameraDisplayOrientation();
      }
    }
  }

  private class CameraPreviewer implements Camera.PreviewCallback {
    @Override public void onPreviewFrame(@NonNull byte[] data, @NonNull Camera camera) {
      if (listener != null) {
        listener.onFrameAvailable(data, previewWidth, previewHeight, frameRotation);
      }
      // Save frame data for video recording.
      if (mRecording) {
        try {
          Camera.Parameters parameters = camera.getParameters();
          Camera.Size size = parameters.getPreviewSize();
          YuvImage image =
              new YuvImage(data, parameters.getPreviewFormat(), size.width, size.height, null);
          File file = new File(mRecordingDir, mRecordingFrameIndex + ".jpg");
          mRecordingFrameIndex++;
          FileOutputStream fileOutputStream = new FileOutputStream(file);
          image.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 80,
              fileOutputStream);
        } catch (Exception e) {
          AppUtil.handleThrowable(e);
        }
      }
      // put the buffer back in the queue, so that it can be used again
      camera.addCallbackBuffer(data);
    }
  }
}