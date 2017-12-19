package com.welol.android.app;

import android.annotation.SuppressLint;
import android.app.Application;
import android.util.Log;
import com.crashlytics.android.Crashlytics;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.google.android.gms.ads.MobileAds;
import com.welol.android.BuildConfig;
import com.welol.android.app.permissions.DevicePermissionsManager;
import com.welol.android.app.permissions.PermissionsManager;
import com.welol.android.camera.CameraHelper;
import com.welol.android.empathy.AffectivaReactionDetectionManager;
import com.welol.android.empathy.ReactionDetectionManager;
import com.welol.android.util.AppUtil;
import io.fabric.sdk.android.Fabric;

/**
 * Proudly created by ohad on 14/07/2017 for TrueThat.
 */

public class App extends Application {
  public static final String FILE_PROVIDER_AUTHORITY = "com.welol.android";
  private static final String TAG = App.class.getSimpleName();
  public static String sPackageName;
  private static ReactionDetectionManager sReactionDetectionManager;
  private static PermissionsManager sPermissionsManager;
  @SuppressLint("StaticFieldLeak") private static CameraHelper sCameraHelper;
  @SuppressLint("StaticFieldLeak") private static FFmpeg sFFmpeg;

  public static CameraHelper getCameraHelper() {
    if (sCameraHelper == null) {
      Log.w(TAG, "Retrieving a null camera helper.");
    }
    return sCameraHelper;
  }

  public static void setCameraHelper(CameraHelper cameraHelper) {
    sCameraHelper = cameraHelper;
  }

  public static ReactionDetectionManager getReactionDetectionManager() {
    return sReactionDetectionManager;
  }

  public static void setReactionDetectionManager(
      ReactionDetectionManager reactionDetectionManager) {
    sReactionDetectionManager = reactionDetectionManager;
  }

  public static PermissionsManager getPermissionsManager() {
    return sPermissionsManager;
  }

  public static void setPermissionsManager(PermissionsManager permissionsManager) {
    sPermissionsManager = permissionsManager;
  }

  public static FFmpeg getFFmpeg() {
    return sFFmpeg;
  }

  @Override public void onCreate() {
    for (int i = 0; i < 10; i++) {
      Log.e(TAG,
          "************************ !!!!! LAUNCHED !!!!! ************************************************ !!!!! LAUNCHED !!!!! ************************");
    }
    sReactionDetectionManager = new AffectivaReactionDetectionManager();
    sPermissionsManager = new DevicePermissionsManager(this);
    sPackageName = getPackageName();
    super.onCreate();
    if (!BuildConfig.DEBUG) {
      Fabric.with(this, new Crashlytics());
    }
    loadFFMpegBinary();
    MobileAds.initialize(this, BuildConfig.ADMOB_ID);
  }

  private void loadFFMpegBinary() {
    try {
      if (sFFmpeg == null) {
        sFFmpeg = FFmpeg.getInstance(this);
      }
      sFFmpeg.loadBinary(new LoadBinaryResponseHandler() {
        @Override public void onFailure() {
          Log.e(TAG, "ffmpeg not supported");
          Crashlytics.log("ffmpeg not supported");
        }

        @Override public void onSuccess() {
          Log.d(TAG, "ffmpeg : correct Loaded");
        }
      });
    } catch (FFmpegNotSupportedException e) {
      Log.e(TAG, "ffmpeg not supported.");
      AppUtil.handleThrowable(e);
    } catch (Exception e) {
      AppUtil.handleThrowable(e);
    }
  }
}
