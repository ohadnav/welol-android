package com.welol.android.camera;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import com.welol.android.util.AppUtil;
import java.io.IOException;

/**
 * A wrapper class to enforce thread-safe access to the mCamera and its properties.
 */
@SuppressWarnings({ "deprecation", "SameParameterValue" }) public class SafeCamera {
  private String TAG = this.getClass().getSimpleName();
  private Camera mCamera;
  private volatile int cameraId = -1;
  @SuppressWarnings("unused") private boolean taken;

  /**
   * Attempts to open the specified mCamera.
   *
   * @param cameraToOpen one of {@link Camera.CameraInfo#CAMERA_FACING_BACK}
   *                     or {@link Camera.CameraInfo#CAMERA_FACING_FRONT}
   *
   * @throws IllegalStateException if the device does not have a mCamera of the requested type or
   *                               the mCamera is already locked by another process
   */
  SafeCamera(int cameraToOpen) throws IllegalStateException {

    int cnum = Camera.getNumberOfCameras();
    Camera.CameraInfo caminfo = new Camera.CameraInfo();

    for (int i = 0; i < cnum; i++) {
      Camera.getCameraInfo(i, caminfo);
      if (caminfo.facing == cameraToOpen) {
        cameraId = i;
        break;
      }
    }
    if (cameraId == -1) {
      throw new IllegalStateException("This device does not have a mCamera of the requested type");
    }
    try {
      mCamera = Camera.open(cameraId); // attempt to get a Camera instance.
    } catch (RuntimeException e) {
      AppUtil.handleThrowable(e);
    }
  }

  public Camera getCamera() {
    return mCamera;
  }

  synchronized Camera.Parameters getParameters() {
    checkTaken();
    return mCamera.getParameters();
  }

  synchronized void setParameters(Camera.Parameters parameters) {
    checkTaken();
    mCamera.setParameters(parameters);
  }

  synchronized void addCallbackBuffer(byte[] buffer) {
    checkTaken();
    mCamera.addCallbackBuffer(buffer);
  }

  synchronized void setPreviewCallbackWithBuffer(Camera.PreviewCallback callback) {
    checkTaken();
    mCamera.setPreviewCallbackWithBuffer(callback);
  }

  synchronized void setPreviewCallback(Camera.PreviewCallback callback) {
    checkTaken();
    mCamera.setPreviewCallback(callback);
  }

  synchronized void setPreviewTexture(SurfaceTexture texture) throws IOException {
    checkTaken();
    mCamera.setPreviewTexture(texture);
  }

  synchronized void setOneShotPreviewCallback(Camera.PreviewCallback callback) {
    checkTaken();
    mCamera.setOneShotPreviewCallback(callback);
  }

  synchronized void startPreview() {
    checkTaken();
    mCamera.startPreview();
  }

  synchronized void stopPreview() {
    checkTaken();
    mCamera.stopPreview();
  }

  synchronized void setDisplayOrientation(int degrees) {
    checkTaken();
    mCamera.setDisplayOrientation(degrees);
  }

  synchronized Camera.CameraInfo getCameraInfo() {
    checkTaken();
    Camera.CameraInfo result = new Camera.CameraInfo();
    Camera.getCameraInfo(cameraId, result);
    return result;
  }

  synchronized void release() {
    checkTaken();
    try {
      mCamera.release();
      mCamera = null;
    } catch (Exception e) {
      //do nothing, exception thrown because mCamera was already closed or mCamera was null (if it failed to open at all)
    }
  }

  private void checkTaken() throws IllegalStateException {
    if (taken) {
      throw new IllegalStateException(
          "cannot take or interact with mCamera while it has been taken");
    }
  }
}