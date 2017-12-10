package com.welol.android.empathy;

import android.content.Context;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Proudly created by ohad on 08/06/2017 for TrueThat.
 * <p>
 * A wrapper for Affectiva emotion detection engine.
 */
public class AffectivaReactionDetectionManager extends BaseReactionDetectionManager {
  private static final String TAG = AffectivaReactionDetectionManager.class.getSimpleName();
  private HandlerThread detectionThread;
  private DetectionHandler detectionHandler;

  @Override public void start(@Nullable Context context) {
    super.start(context);
    if (detectionThread == null) {
      // fire up the background thread
      detectionThread = new DetectionThread();
      detectionThread.start();
      detectionHandler = new DetectionHandler(context, detectionThread, this);
      detectionHandler.sendStartMessage();
    }
  }

  public void stop() {
    Log.d(TAG, "stop");
    if (detectionHandler != null) {
      detectionHandler.sendStopMessage();
      try {
        detectionThread.join();
        detectionThread = null;
        detectionHandler = null;
      } catch (InterruptedException ignored) {
      }
    }
  }

  private static class DetectionThread extends HandlerThread {
    private DetectionThread() {
      super("ReactionDetectionThread");
    }
  }
}
