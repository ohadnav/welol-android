package com.welol.android.viewmodel;

import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import com.welol.android.R;
import com.welol.android.app.App;
import com.welol.android.app.permissions.Permission;
import com.welol.android.model.Level;
import com.welol.android.model.Video;
import com.welol.android.viewmodel.viewinterface.MainViewInterface;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static com.welol.android.util.CommonUtil.killTimer;

/**
 * Proudly created by ohad on 03/12/2017 for TrueThat.
 */

public class MainViewModel extends BaseViewModel<MainViewInterface> {

  private final static String BUNDLE_STATE = "state";
  private final static String BUNDLE_LEVELS = "levels";
  private final static String BUNDLE_CURRENT_LEVEL = "currentLevel";
  private static final int PLAY_AGAIN_DELAY = 5000;
  public final ObservableInt mTitleTextResourceId = new ObservableInt(R.string.dont_laugh);
  public final ObservableField<String> mSubtitleText = new ObservableField<>();
  public final ObservableInt mTitleTextColor = new ObservableInt(R.color.secondary);
  public final ObservableInt mButtonTextResourceId = new ObservableInt(R.string.lets_go);
  public final ObservableInt mPlayAgainVisibility = new ObservableInt(View.INVISIBLE);
  public final ObservableInt mNoLaughImageVisibility = new ObservableInt(View.VISIBLE);
  public final ObservableInt mLoadingImageVisibility = new ObservableInt(View.GONE);
  private Timer mTimer;
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

  @Override public void onPause() {
    super.onPause();
    killTimer(mTimer);
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
      getView().share(mCurrentLevel);
    }
  }

  public void startGame() {
    Log.d(TAG, "startGame");
    if (getView() != null) {
      mState = State.PLAYING;
      mLevels = new ArrayList<>(getView().getLevelsProvider().provide());
      mCurrentLevel = 0;
      mSubtitleText.set(getView().getBaseActivity().getResources().getString(R.string.dont_smile));
      mButtonTextResourceId.set(R.string.lets_go);
      mTitleTextColor.set(R.color.secondary);
      mTitleTextResourceId.set(R.string.dont_laugh);
      mPlayAgainVisibility.set(View.INVISIBLE);
      mNoLaughImageVisibility.set(View.VISIBLE);
      mLoadingImageVisibility.set(View.GONE);
      getView().hideVideo();
      getView().loadAd();
    }
  }

  public void onLevelFinished(Level.Result result) {
    Log.d(TAG, "onLevelFinished with " + result);
    mTitleTextResourceId.set(result.getDescriptionResourceId());
    mTitleTextColor.set(result == Level.Result.WIN ? R.color.success : R.color.error);
    if (mCurrentLevel < mLevels.size() - 1 && result == Level.Result.WIN) {
      prepareNextLevel();
    } else {
      onGameFinished(result);
    }
  }

  public void onViewerRecordingReady(Video video) {
    Log.d(TAG, "onViewerRecordingReady at " + video.getUri());
    if (getView() != null) {
      mLoadingImageVisibility.set(View.GONE);
      getView().showVideo(video);
    }
  }

  private void onGameFinished(Level.Result result) {
    Log.d(TAG, "gameFinished");
    mState = State.GAME_FINISHED;
    mTimer = new Timer();
    mTimer.schedule(new TimerTask() {
      @Override public void run() {
        mPlayAgainVisibility.set(View.VISIBLE);
      }
    }, PLAY_AGAIN_DELAY);
    mNoLaughImageVisibility.set(View.INVISIBLE);
    mButtonTextResourceId.set(R.string.share);
    if (getView() != null) {
      mLoadingImageVisibility.set(View.VISIBLE);
      getView().showVideo(mLevels.get(mCurrentLevel).getVideo());
      mSubtitleText.set(getView().getBaseActivity()
          .getResources()
          .getString(R.string.game_finished, mCurrentLevel));
      getView().generateViewerRecordingOverlay();
      getView().showAd();
    }
    if (result == Level.Result.WIN) {
      mCurrentLevel++;
    }
  }

  private void prepareNextLevel() {
    Log.d(TAG, "prepareNextLevel");
    mButtonTextResourceId.set(R.string.next);
    mCurrentLevel++;
  }

  private enum State {
    LAUNCH, PLAYING, GAME_FINISHED
  }
}
