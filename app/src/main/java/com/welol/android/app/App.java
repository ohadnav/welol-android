package com.welol.android.app;

import android.app.Application;
import android.util.Log;
import com.crashlytics.android.Crashlytics;
import com.welol.android.BuildConfig;
import com.welol.android.app.permissions.DevicePermissionsManager;
import com.welol.android.app.permissions.PermissionsManager;
import com.welol.android.empathy.AffectivaReactionDetectionManager;
import com.welol.android.empathy.ReactionDetectionManager;
import io.fabric.sdk.android.Fabric;

/**
 * Proudly created by ohad on 14/07/2017 for TrueThat.
 */

public class App extends Application {
  private static final String TAG = App.class.getSimpleName();
  private static ReactionDetectionManager sReactionDetectionManager;
  private static PermissionsManager sPermissionsManager;

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

  @Override public void onCreate() {
    for (int i = 0; i < 10; i++) {
      Log.e(TAG,
          "************************ !!!!! LAUNCHED !!!!! ************************************************ !!!!! LAUNCHED !!!!! ************************");
    }
    sReactionDetectionManager = new AffectivaReactionDetectionManager();
    sPermissionsManager = new DevicePermissionsManager(this);
    super.onCreate();
    if (!BuildConfig.DEBUG) {
      Fabric.with(this, new Crashlytics());
    }
  }
}
