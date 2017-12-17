package com.welol.android.empathy;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;
import com.welol.android.model.Emotion;
import java.util.HashSet;
import java.util.Set;

/**
 * Proudly created by ohad on 31/07/2017 for TrueThat.
 */

public class BaseReactionDetectionManager
    implements ReactionDetectionManager, ReactionDetectionListener {
  Set<ReactionDetectionListener> mReactionDetectionListeners = new HashSet<>();
  /**
   * For logging.
   */
  String TAG = this.getClass().getSimpleName();
  private State mState = State.IDLE;

  private boolean mHasAttention = false;
  private boolean paused = true;

  @Override public void start(@Nullable Context context) {
    Log.d(TAG, "Starting detection.");
    mState = State.DETECTING;
    mReactionDetectionListeners = new HashSet<>();
    mHasAttention = false;
    paused = false;
  }

  @Override public void subscribe(ReactionDetectionListener reactionDetectionListener) {
    if (isDetecting()) {
      Log.d(TAG, "Subscribing "
          + reactionDetectionListener.getClass().getSimpleName()
          + "("
          + reactionDetectionListener.hashCode()
          + ")");
      mReactionDetectionListeners.add(reactionDetectionListener);
      if (mHasAttention) {
        reactionDetectionListener.onAttention();
      }
    } else {
      Log.e(TAG, "Trying to subscribe to an idle manager.");
    }
  }

  @Override public void unsubscribe(ReactionDetectionListener reactionDetectionListener) {
    if (mReactionDetectionListeners.contains(reactionDetectionListener)) {
      Log.d(TAG, "Unsubscribing "
          + reactionDetectionListener.getClass().getSimpleName()
          + "("
          + reactionDetectionListener.hashCode()
          + ")");
      mReactionDetectionListeners.remove(reactionDetectionListener);
      Log.d(TAG, mReactionDetectionListeners.size() + " listeners left.");
    }
  }

  @Override public void stop() {
    Log.d(TAG, "Stopping detection.");
    mState = State.IDLE;
    if (!mReactionDetectionListeners.isEmpty()) {
      Log.w(TAG, "Stopped with "
          + mReactionDetectionListeners.size()
          + " listeners left (such as "
          + mReactionDetectionListeners.iterator().next().getClass().getSimpleName()
          + ")");
    }
    // Reset attention.
    mHasAttention = false;
  }

  @Override public boolean hasAttention() {
    return mHasAttention;
  }

  @Override public void pause() {
    if (!paused) {
      Log.d(TAG, "paused");
    }
    paused = true;
  }

  @Override public void resume() {
    if (paused) {
      Log.d(TAG, "resumed");
    }
    paused = false;
  }

  @Override public boolean isPaused() {
    return paused;
  }

  @Override public void onReactionDetected(Emotion reaction) {
    for (ReactionDetectionListener reactionDetectionListener : mReactionDetectionListeners) {
      reactionDetectionListener.onReactionDetected(reaction);
    }
  }

  @Override public void onAttention() {
    if (!mHasAttention) {
      Log.d(TAG, "onAttention");
      mHasAttention = true;
      for (ReactionDetectionListener reactionDetectionListener : mReactionDetectionListeners) {
        reactionDetectionListener.onAttention();
      }
    }
  }

  @Override public void onAttentionLost() {
    if (mHasAttention) {
      Log.d(TAG, "onAttentionLost");
      mHasAttention = false;
      for (ReactionDetectionListener reactionDetectionListener : mReactionDetectionListeners) {
        reactionDetectionListener.onAttentionLost();
      }
    }
  }

  @Override public String getTAG() {
    return TAG;
  }

  /**
   * @return Whether a detection is currently ongoing.
   */
  boolean isDetecting() {
    return mState == State.DETECTING;
  }

  private enum State {
    DETECTING, IDLE
  }
}
