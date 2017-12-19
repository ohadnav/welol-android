package com.welol.android.viewmodel;

import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
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
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
  public final ObservableInt mProgressBarVisibility = new ObservableInt(View.GONE);
  public final ObservableInt mProgressBarProgress = new ObservableInt(0);
  private boolean mViewerRecordingBuilding = false;
  private Timer mTimer;
  private State mState = State.LAUNCH;
  private ArrayList<Level> mLevels;
  private Integer mCurrentLevel;
  private Pattern mProgressPattern = Pattern.compile("time=([0-9:.]+)");

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
      if (mViewerRecordingBuilding) {
        getView().share(mCurrentLevel);
      } else {
        getView().snackbar(R.string.making_selfie, Snackbar.LENGTH_SHORT);
      }
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
      mProgressBarVisibility.set(View.GONE);
      mProgressBarProgress.set(0);
      getView().hideVideo();
      getView().loadAd();
      mViewerRecordingBuilding = false;
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
    mViewerRecordingBuilding = true;
    if (getView() != null) {
      mProgressBarVisibility.set(View.GONE);
      getView().showVideo(video);
      getView().activateShare();
    }
  }

  public void onViewerRecordingProgress(String progressDescription, boolean isFramesProgress,
      long durationMs) {
    Long currentProgress = descriptionToPercent(progressDescription, durationMs);
    if (currentProgress != null) {
      if (isFramesProgress) {
        mProgressBarProgress.set(currentProgress.intValue() / 5);
      } else {
        mProgressBarProgress.set(20 + (4 * currentProgress.intValue() / 5));
      }
    }
  }

  public void onViewerRecordingFailed() {
    mProgressBarVisibility.set(View.GONE);
    if (getView() != null) {
      getView().onViewerRecordingFailed();
      getView().activateShare();
      mViewerRecordingBuilding = false;
    }
  }

  private Long descriptionToPercent(String description, long durationMs) {
    Matcher matcher = mProgressPattern.matcher(description);
    if (matcher.find()) {
      String separatedTime = matcher.group(1);
      String[] units = separatedTime.split(":");
      // Assume seconds only.
      long currentMs =
          TimeUnit.MINUTES.toMillis(Long.parseLong(units[1])) + TimeUnit.SECONDS.toMillis(
              Long.parseLong(units[2].split("\\.")[0])) + TimeUnit.MILLISECONDS.toMillis(
              Long.parseLong(units[2].split("\\.")[1]) * 10);
      return 100 * currentMs / durationMs;
    }
    return null;
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
      mProgressBarVisibility.set(View.VISIBLE);
      getView().showVideo(mLevels.get(mCurrentLevel).getVideo());
      mSubtitleText.set(getView().getBaseActivity()
          .getResources()
          .getString(R.string.game_finished, mCurrentLevel));
      getView().generateViewerRecordingOverlay();
      getView().showAd();
      getView().inactivateShare();
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
