package com.welol.android.util;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.Size;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import javax.annotation.Nullable;

/**
 * Proudly created by ohad on 25/06/2017 for TrueThat.
 */

public class AppUtil {
  /**
   * @param view within the display.
   *
   * @return available (for the app) dimensions of the device's display.
   */
  @Nullable public static Size availableDisplaySize(View view) {
    Rect windowRect = new Rect();
    view.getWindowVisibleDisplayFrame(windowRect);
    Size availableSize =
        new Size(windowRect.bottom - windowRect.top, windowRect.right - windowRect.left);
    // Sizes are naturally inverted, and so inverse size if we are in a portrait orientation.
    Display display;
    WindowManager windowManager =
        (WindowManager) view.getContext().getSystemService(Context.WINDOW_SERVICE);
    if (windowManager != null) {
      display = windowManager.getDefaultDisplay();
    } else {
      return null;
    }
    if (display == null) {
      return null;
    }
    if (display.getRotation() == Surface.ROTATION_0
        || display.getRotation() == Surface.ROTATION_180) {
      availableSize = new Size(availableSize.getHeight(), availableSize.getWidth());
    }
    return availableSize;
  }

  /**
   * @param context of the application.
   *
   * @return the hardware dimensions of the device (normally these are the answer to "what's the
   * screen resolution?"
   */
  @Nullable public static Point realDisplaySize(Context context) {
    Display display;
    WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    if (windowManager != null) {
      display = windowManager.getDefaultDisplay();
    } else {
      return null;
    }
    if (display == null) {
      return null;
    }
    display = windowManager.getDefaultDisplay();
    Point size = new Point();
    display.getRealSize(size);
    return size;
  }

  /**
   * @return whether the app is currently run within an emulator.
   */
  @SuppressWarnings("unused") public static boolean isEmulator() {
    return Build.FINGERPRINT.startsWith("generic")
        || Build.FINGERPRINT.startsWith("unknown")
        || Build.MODEL.contains("google_sdk")
        || Build.MODEL.contains("Emulator")
        || Build.MODEL.contains("Android SDK built for x86")
        || Build.MANUFACTURER.contains("Genymotion")
        || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
        || "google_sdk".equals(Build.PRODUCT);
  }
}
