package com.welol.android.viewmodel;

import com.welol.android.R;
import com.welol.android.app.App;
import com.welol.android.empathy.ReactionDetectionListener;
import com.welol.android.model.Emotion;
import com.welol.android.model.Level;
import com.welol.android.viewmodel.viewinterface.LevelViewInterface;

/**
 * Proudly created by ohad on 04/12/2017 for TrueThat.
 */

public class LevelViewModel extends BaseViewModel<LevelViewInterface>
    implements ReactionDetectionListener {

  private static final Emotion LOSING_REACTION = Emotion.HAPPY;
  private boolean mReplayed = false;

  public void onVideoFinished() {
    if (getView() != null) {
      if (mReplayed) {
        getView().finishLevel(Level.Result.WIN);
      } else {
        mReplayed = true;
        getView().replay();
      }
    }
  }

  public void onVideoPrepared() {
    if (getView() != null) {
      if (App.getReactionDetectionManager().hasAttention()) {
        getView().play();
      } else {
        getView().snackbar(R.string.lost_face);
      }
    }
  }

  public void onBufferingStart() {
    if (getView() != null) {
      getView().pause();
    }
  }

  public void onBufferingEnd() {
    if (getView() != null && App.getReactionDetectionManager().hasAttention()) {
      getView().play();
    }
  }

  @Override public void onResume() {
    super.onResume();
    if (getView() != null) {
      App.getReactionDetectionManager().subscribe(this);
    }
  }

  @Override public void onPause() {
    super.onPause();
    if (getView() != null) {
      getView().pause();
    }
  }

  @Override public void onReactionDetected(Emotion reaction) {
    if (reaction == LOSING_REACTION && getView() != null) {
      getView().finishLevel(Level.Result.LOSE);
    }
  }

  @Override public void onAttention() {
    if (getView() != null) {
      getView().play();
      getView().hideSnackbar();
    }
  }

  @Override public void onAttentionLost() {
    if (getView() != null) {
      getView().pause();
      getView().snackbar(R.string.lost_face);
    }
  }
}
