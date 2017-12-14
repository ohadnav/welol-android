package com.welol.android.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import com.welol.android.R;
import com.welol.android.databinding.ActivityLevelBinding;
import com.welol.android.model.Level;
import com.welol.android.util.RequestCodes;
import com.welol.android.view.fragment.VideoFragment;
import com.welol.android.viewmodel.LevelViewModel;
import com.welol.android.viewmodel.viewinterface.LevelViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;

public class LevelActivity
    extends BaseActivity<LevelViewInterface, LevelViewModel, ActivityLevelBinding>
    implements LevelViewInterface {

  public static final String INTENT_LEVEL = "level";
  public static final String INTENT_RECORDING = "recording";
  public static final String INTENT_DURATION_MS = "duration";
  public static final String BUNDLE_LEVEL = INTENT_LEVEL;
  private Level mLevel;
  private VideoFragment mVideoFragment;

  @Override public void play() {
    mVideoFragment.playOrResume();
  }

  @Override public void skipToStart() {
    mVideoFragment.skipToStart();
  }

  @Override public void pause() {
    mVideoFragment.pause();
  }

  @Override public boolean isPrepared() {
    return mVideoFragment.isMediaPlayerPrepared();
  }

  @Override
  public void onLevelFinished(Level.Result result, String viewerRecording, long durationMs) {
    Log.d(TAG, "Level finished with " + result);
    Intent resultIntent = new Intent();
    mLevel.setResult(result);
    resultIntent.putExtra(INTENT_LEVEL, mLevel);
    resultIntent.putExtra(INTENT_RECORDING, viewerRecording);
    resultIntent.putExtra(INTENT_DURATION_MS, durationMs);
    setResult(RequestCodes.PLAY_LEVEL, resultIntent);
    finish();
  }

  @Override
  public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
    super.onRestoreInstanceState(savedInstanceState, persistentState);
  }

  @Override public ViewModelBindingConfig getViewModelBindingConfig() {
    return new ViewModelBindingConfig(R.layout.activity_level, this);
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (savedInstanceState != null) {
      mLevel = savedInstanceState.getParcelable(BUNDLE_LEVEL);
    }
    Bundle extras = getIntent().getExtras();
    if (extras != null && extras.get(INTENT_LEVEL) != null) {
      mLevel = extras.getParcelable(INTENT_LEVEL);
    }
    if (mLevel == null) {
      throw new IllegalStateException("Created " + TAG + " without a level.");
    }
    mVideoFragment = VideoFragment.newInstance(mLevel.getVideo());
    mVideoFragment.setVideoListener(getViewModel());
    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
    fragmentTransaction.replace(android.R.id.content, mVideoFragment);
    fragmentTransaction.commit();
  }

  @Override public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putParcelable(BUNDLE_LEVEL, mLevel);
  }

  @Override protected void onPause() {
    super.onPause();
    mVideoFragment.pause();
  }
}
