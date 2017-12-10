package com.welol.android.empathy;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import com.affectiva.android.affdex.sdk.Frame;
import com.affectiva.android.affdex.sdk.detector.FrameDetector;
import com.crashlytics.android.Crashlytics;
import com.welol.android.BuildConfig;
import java.util.Date;

/**
 * Proudly created by ohad on 31/07/2017 for TrueThat.
 * <p>
 * A handler for the DetectionThread.
 */
public class DetectionHandler extends Handler {
  //Incoming message codes
  private static final int START = 0;
  private static final int STOP = 1;
  private String TAG = this.getClass().getSimpleName();
  private CameraHelper mCameraHelper;
  private FrameDetector mFrameDetector;
  private SurfaceTexture mSurfaceTexture;

  DetectionHandler(Context context, HandlerThread detectionThread,
      BaseReactionDetectionManager reactionDetectionManager) {
    // note: getLooper will block until the the thread's looper has been prepared
    super(detectionThread.getLooper());
    Display display =
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
    mCameraHelper = new CameraHelper(context, display, new DetectionHandler.CameraHelperListener());
    mSurfaceTexture = new SurfaceTexture(0); // a dummy texture

    // Set up the FrameDetector.  For the purposes of this sample app, we'll just request
    // listen for face events and request valence scores.
    mFrameDetector = new FrameDetector(context);
    mFrameDetector.setDetectAllEmotions(true);
    mFrameDetector.setDetectAllExpressions(true);
    mFrameDetector.setImageListener(new AffectivaImageListener(reactionDetectionManager));
  }

  /**
   * Process incoming messages
   *
   * @param msg message to handle
   */
  @Override public void handleMessage(Message msg) {
    switch (msg.what) {
      case START:
        Log.d(TAG, "starting background processing of frames");
        try {
          mFrameDetector.start();
          //noinspection deprecation
          mCameraHelper.acquire(Camera.CameraInfo.CAMERA_FACING_FRONT);
          mCameraHelper.start(mSurfaceTexture); // initiates previewing
        } catch (IllegalStateException e) {
          if (!BuildConfig.DEBUG) {
            Crashlytics.logException(e);
          }
          e.printStackTrace();
          Log.d(TAG, "couldn't open camera: " + e.getMessage());
          // TODO(ohad): Let user know via UI
          return;
        }
        break;
      case STOP:
        Log.d(TAG, "stopping background processing of frames");
        mCameraHelper.stop(); // stops previewing
        mCameraHelper.release();
        mFrameDetector.stop();

        Log.d(TAG, "quitting detection thread");
        ((HandlerThread) getLooper().getThread()).quit();
        break;

      default:
        break;
    }
  }

  /**
   * asynchronously start processing on the background thread
   */
  void sendStartMessage() {
    sendMessage(obtainMessage(START));
  }

  /**
   * asynchronously stop processing on the background thread
   */
  void sendStopMessage() {
    sendMessage(obtainMessage(STOP));
  }

  /**
   * A mListener for CameraHelper callbacks
   */
  private class CameraHelperListener implements CameraHelper.Listener {
    private static final long TIMESTAMP_DELTA_MILLIS = 100;
    private Date lastTimestamp = new Date();
    private float detectionTimestamp = 0;

    @Override
    public void onFrameAvailable(byte[] frame, int width, int height, Frame.ROTATE rotation) {
      Date timeStamp = new Date();
      if (timeStamp.getTime() > lastTimestamp.getTime() + TIMESTAMP_DELTA_MILLIS) {
        lastTimestamp = timeStamp;
        detectionTimestamp += 0.1;
        mFrameDetector.process(createFrameFromData(frame, width, height, rotation),
            detectionTimestamp);
      }
    }

    @Override public void onFrameSizeSelected(int width, int height, Frame.ROTATE rotation) {
    }

    private Frame createFrameFromData(byte[] frameData, int width, int height,
        Frame.ROTATE rotation) {
      Frame.ByteArrayFrame frame =
          new Frame.ByteArrayFrame(frameData, width, height, Frame.COLOR_FORMAT.YUV_NV21);
      frame.setTargetRotation(rotation);
      return frame;
    }
  }
}
