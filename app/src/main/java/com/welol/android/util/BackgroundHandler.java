package com.welol.android.util;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * Proudly created by ohad on 18/06/2017 for TrueThat.
 * <p>
 * Background thread wrapper.
 */

public class BackgroundHandler {
  /**
   * Thread from which the handler manages tasks
   */
  private HandlerThread mThread;
  /**
   * Manages tasks to run within a background thread.
   */
  private Handler mHandler;
  private String mName;

  public BackgroundHandler(String name) {
    mName = name;
  }

  public Handler getHandler() {
    return mHandler;
  }

  /**
   * Must be called in order to use {@link #mHandler}.
   */
  public void start() {
    if (mThread == null) {
      mThread = new HandlerThread(mName);
      mThread.start();
      mHandler = new Handler(mThread.getLooper());
    }
  }

  /**
   * Stops the handler in a peaceful manner.
   */
  public void stop() {
    if (mThread != null) {
      mThread.quitSafely();
      try {
        mThread.join();
        mThread = null;
        mHandler = null;
      } catch (InterruptedException e) {
        AppUtil.handleThrowable(e);
      }
    }
  }
}
