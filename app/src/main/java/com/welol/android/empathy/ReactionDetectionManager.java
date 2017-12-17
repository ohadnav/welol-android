package com.welol.android.empathy;

import android.content.Context;

/**
 * Proudly created by ohad on 08/06/2017 for TrueThat.
 */
public interface ReactionDetectionManager {
  /**
   * Initiates an emotional reaction detection task, that publishes detection reaction to
   * subscribers.
   *
   * @param context for the detection thread.
   */
  void start(Context context);

  /**
   * Subscribes a listener so that detected reactions will be published to it. Multiple subscribers
   * are allowed.
   *
   * @param reactionDetectionListener to subscribe.
   */
  void subscribe(ReactionDetectionListener reactionDetectionListener);

  /**
   * Removes a listener from the subscribers collections, so that it is no longer notified of
   * detected reactions.
   *
   * @param reactionDetectionListener to unsubscribe.
   */
  void unsubscribe(ReactionDetectionListener reactionDetectionListener);

  /**
   * Stops the current detection task.
   */
  void stop();

  /**
   * @return whether the user looks at the phone.
   */
  boolean hasAttention();

  void pause();

  void resume();

  boolean isPaused();
}
