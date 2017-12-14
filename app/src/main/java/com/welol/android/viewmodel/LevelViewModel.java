package com.welol.android.viewmodel;

import android.util.Log;
import com.welol.android.R;
import com.welol.android.app.App;
import com.welol.android.empathy.ReactionDetectionListener;
import com.welol.android.model.Emotion;
import com.welol.android.model.Level;
import com.welol.android.view.fragment.VideoFragment;
import com.welol.android.viewmodel.viewinterface.LevelViewInterface;

/**
 * Proudly created by ohad on 04/12/2017 for TrueThat.
 */

public class LevelViewModel extends BaseViewModel<LevelViewInterface>
    implements ReactionDetectionListener, VideoFragment.VideoListener {

  private static final Emotion LOSING_REACTION = Emotion.HAPPY;
  private boolean mReplayed = false;
  private String mRecordingDir;
  private Level.Result mResult;

  @Override public void onBufferingStart() {
    pause();
  }

  @Override public void onBufferingEnd() {
    play();
  }

  @Override public void onFinished() {
    long durationMs = App.getCameraHelper().stopRecording();
    if (getView() != null) {
      if (mResult == Level.Result.LOSE || mReplayed) {
        // Finish level.
        if (mResult == null) {
          mResult = Level.Result.WIN;
        }
        getView().onLevelFinished(mResult, mRecordingDir, durationMs);
      } else {
        mReplayed = true;
        getView().skipToStart();
        play();
      }
    }
  }

  @Override public void onPrepared() {
    if (getView() != null) {
      if (!App.getReactionDetectionManager().hasAttention()) {
        getView().snackbar(R.string.lost_face);
      }
      play();
    }
  }

  @Override public void onResume() {
    super.onResume();
    if (getView() != null) {
      App.getReactionDetectionManager().subscribe(this);
      if (getView().isPrepared()) {
        play();
      }
    }
  }

  @Override public void onPause() {
    super.onPause();
    pause();
  }

  @Override public void onReactionDetected(Emotion reaction) {
    if (reaction == LOSING_REACTION && getView() != null && mResult != Level.Result.LOSE) {
      mResult = Level.Result.LOSE;
      getView().snackbar(R.string.smile_detected);
    }
  }

  @Override public void onAttention() {
    if (getView() != null) {
      getView().hideSnackbar();
    }
  }

  @Override public void onAttentionLost() {
    if (getView() != null) {
      getView().snackbar(R.string.lost_face);
    }
  }

  private void play() {
    Log.d(TAG, "play");
    if (getView() != null) {
      getView().play();
      mRecordingDir = App.getCameraHelper().startOrResumeRecording();
    }
  }

  private void pause() {
    Log.d(TAG, "pause");
    if (getView() != null) {
      getView().pause();
      App.getCameraHelper().pauseRecording();
    }
  }
}
