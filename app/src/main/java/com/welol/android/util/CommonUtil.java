package com.welol.android.util;

import java.util.Timer;

/**
 * Proudly created by ohad on 18/12/2017 for TrueThat.
 */

public class CommonUtil {
  public static void killTimer(Timer timer) {
    if (timer != null) {
      timer.cancel();
    }
  }
}
