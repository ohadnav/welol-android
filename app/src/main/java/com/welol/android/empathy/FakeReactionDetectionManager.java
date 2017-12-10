package com.welol.android.empathy;

import android.util.Log;
import com.welol.android.model.Emotion;

/**
 * Proudly created by ohad on 18/06/2017 for TrueThat.
 */

public class FakeReactionDetectionManager extends BaseReactionDetectionManager {

  /**
   * Mocks a reaction detection.
   */
  @Override public void onReactionDetected(Emotion reaction) {
    Log.d(TAG, "FAKE detection of " + reaction.name());
    super.onReactionDetected(reaction);
  }

  /**
   * @param listener to check for
   *
   * @return whether to given listener is subscribed to this detection manager.
   */
  public boolean isSubscribed(ReactionDetectionListener listener) {
    return isDetecting() && mReactionDetectionListeners.contains(listener);
  }
}
