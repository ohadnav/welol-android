package com.welol.android.viewmodel;

import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import com.welol.android.R;
import com.welol.android.app.App;
import com.welol.android.app.permissions.Permission;
import com.welol.android.model.Level;
import com.welol.android.viewmodel.viewinterface.MainViewInterface;
import java.util.ArrayList;

/**
 * Proudly created by ohad on 03/12/2017 for TrueThat.
 */

public class MainViewModel extends BaseViewModel<MainViewInterface> {

  private final static String BUNDLE_STATE = "state";
  private final static String BUNDLE_LEVELS = "levels";
  private final static String BUNDLE_CURRENT_LEVEL = "currentLevel";
  public final ObservableInt mTitleTextResourceId = new ObservableInt(R.string.dont_laugh);
  public final ObservableField<String> mSubtitleText = new ObservableField<>();
  public final ObservableInt mTitleTextColor = new ObservableInt(R.color.secondary);
  public final ObservableInt mButtonTextResourceId = new ObservableInt(R.string.lets_go);
  public final ObservableInt mPlayAgainVisibility = new ObservableInt(View.INVISIBLE);
  private State mState = State.LAUNCH;
  private ArrayList<Level> mLevels;
  private Integer mCurrentLevel;

  @Override public void onBindView(@NonNull MainViewInterface view) {
    super.onBindView(view);
    mSubtitleText.set(view.getBaseActivity().getResources().getString(R.string.dont_smile));
  }

  @Override public void onResume() {
    super.onResume();
    if (getView() != null) {
      // Request camera permission
      if (App.getPermissionsManager()
          .requestIfNeeded(getView().getBaseActivity(), Permission.CAMERA)) {
        // Start detection
        App.getReactionDetectionManager().start(getView().getBaseActivity());
      }
    }
  }

  @Override public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    if (mState != null) {
      outState.putSerializable(BUNDLE_STATE, mState);
    }
    if (mLevels != null) {
      outState.putParcelableArrayList(BUNDLE_LEVELS, mLevels);
    }
    if (mCurrentLevel != null) {
      outState.putInt(BUNDLE_CURRENT_LEVEL, mCurrentLevel);
    }
  }

  @Override public void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    if (savedInstanceState != null) {
      if (savedInstanceState.get(BUNDLE_STATE) != null) {
        mState = (State) savedInstanceState.getSerializable(BUNDLE_STATE);
      }
      if (savedInstanceState.get(BUNDLE_LEVELS) != null) {
        mLevels = savedInstanceState.getParcelableArrayList(BUNDLE_LEVELS);
      }
      if (savedInstanceState.get(BUNDLE_CURRENT_LEVEL) != null) {
        mCurrentLevel = savedInstanceState.getInt(BUNDLE_CURRENT_LEVEL);
      }
    }
  }

  @Override public void onDestroy() {
    super.onDestroy();
    // Stop detection
    App.getReactionDetectionManager().stop();
  }

  public void onButtonClicked() {
    if (getView() == null) {
      return;
    }
    if (mState.ordinal() < State.PLAYING.ordinal()) {
      startGame();
    }
    if (mState == State.PLAYING) {
      getView().playLevel(mLevels.get(mCurrentLevel));
    } else if (mState == State.GAME_FINISHED) {
      getView().share();
    }
  }

  public void startGame() {
    if (getView() != null) {
      mState = State.PLAYING;
      mLevels = new ArrayList<>(getView().getLevelsProvider().provide());
      mCurrentLevel = 0;
      mSubtitleText.set(getView().getBaseActivity().getResources().getString(R.string.dont_smile));
      mButtonTextResourceId.set(R.string.lets_go);
      mTitleTextColor.set(R.color.secondary);
      mTitleTextResourceId.set(R.string.dont_laugh);
      mPlayAgainVisibility.set(View.INVISIBLE);
    }
  }

  public void onLevelFinished(Level.Result result) {
    mTitleTextResourceId.set(result.getDescriptionResourceId());
    mTitleTextColor.set(result == Level.Result.WIN ? R.color.success : R.color.error);
    if (mCurrentLevel < mLevels.size() - 1 && result == Level.Result.WIN) {
      prepareNextLevel();
    } else {
      mState = State.GAME_FINISHED;
      mPlayAgainVisibility.set(View.VISIBLE);
      mButtonTextResourceId.set(R.string.share);
      if (getView() != null) {
        mSubtitleText.set(getView().getBaseActivity()
            .getResources()
            .getString(R.string.game_finished, mCurrentLevel));
      }
    }
  }

  private void prepareNextLevel() {
    mButtonTextResourceId.set(R.string.next);
    mCurrentLevel++;
  }

  private enum State {
    LAUNCH, PLAYING, GAME_FINISHED
  }
}
