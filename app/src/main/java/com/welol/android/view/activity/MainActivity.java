package com.welol.android.view.activity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.ImageView;
import butterknife.BindView;
import com.crashlytics.android.Crashlytics;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.google.common.base.Strings;
import com.welol.android.R;
import com.welol.android.databinding.ActivityMainBinding;
import com.welol.android.model.Level;
import com.welol.android.model.LevelsProvider;
import com.welol.android.model.RandomLevelProvider;
import com.welol.android.model.Video;
import com.welol.android.util.FfmpegExecutor;
import com.welol.android.util.RequestCodes;
import com.welol.android.view.fragment.VideoFragment;
import com.welol.android.viewmodel.MainViewModel;
import com.welol.android.viewmodel.viewinterface.MainViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;
import io.fabric.sdk.android.Fabric;
import java.io.File;
import java.util.Date;

import static java.util.regex.Pattern.matches;

public class MainActivity
    extends BaseActivity<MainViewInterface, MainViewModel, ActivityMainBinding>
    implements MainViewInterface {
  private static final String BUNDLE_VIEWER_RECORDING = "viewerRecording";
  @BindView(R.id.loadingImage) ImageView mLoadingImage;
  private LevelsProvider mLevelsProvider;
  private VideoFragment mVideoFragment;
  private String mViewerRecording;
  private long mOverlayMakeStartTimestamp;
  private Intent mLevelIntent;

  @Override public ViewModelBindingConfig getViewModelBindingConfig() {
    return new ViewModelBindingConfig(R.layout.activity_main, this);
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Fabric.with(this, new Crashlytics());
    mLevelsProvider = new RandomLevelProvider();
  }

  @Override public void onStart() {
    super.onStart();
    ((AnimationDrawable) mLoadingImage.getDrawable()).start();
    mLoadingImage.bringToFront();
  }

  @Override public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    if (!Strings.isNullOrEmpty(mViewerRecording)) {
      outState.putString(BUNDLE_VIEWER_RECORDING, mViewerRecording);
    }
  }

  @Override
  public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
    super.onRestoreInstanceState(savedInstanceState, persistentState);
    if (savedInstanceState != null && savedInstanceState.get(BUNDLE_VIEWER_RECORDING) != null) {
      mViewerRecording = savedInstanceState.getString(BUNDLE_VIEWER_RECORDING);
    }
  }

  @Override public void playLevel(Level level) {
    Intent levelIntent = new Intent(this, LevelActivity.class);
    levelIntent.putExtra(LevelActivity.INTENT_LEVEL, level);
    startActivityForResult(levelIntent, RequestCodes.PLAY_LEVEL);
  }

  @Override public LevelsProvider getLevelsProvider() {
    return mLevelsProvider;
  }

  @Override public void share(int passedLevels) {
    Intent shareIntent = new Intent(Intent.ACTION_SEND);
    if (mViewerRecording != null) {
      shareIntent.setType("video/mp4");
      shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
      shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(mViewerRecording)));
    } else {
      shareIntent.setType("text/plain");
      shareIntent.putExtra(Intent.EXTRA_TEXT,
          getResources().getString(R.string.share_text, passedLevels));
    }
    startActivity(Intent.createChooser(shareIntent, "Share using"));
  }

  @Override public void hideVideo() {
    if (mVideoFragment != null) {
      FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
      fragmentTransaction.remove(mVideoFragment);
      fragmentTransaction.commit();
    }
  }

  @Override public void showVideo(Video video) {
    mVideoFragment = VideoFragment.newInstance(video);
    mVideoFragment.setAutoPlay(true);
    mVideoFragment.setMute(true);
    mVideoFragment.setLooping(true);
    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
    fragmentTransaction.replace(R.id.mediaLayout, mVideoFragment);
    fragmentTransaction.commit();
    mLoadingImage.bringToFront();
  }

  @Override public void generateViewerRecordingOverlay() {
    final Level level = mLevelIntent.getParcelableExtra(LevelActivity.INTENT_LEVEL);
    final String recordingDir = mLevelIntent.getStringExtra(LevelActivity.INTENT_RECORDING);
    final long recordingDurationMs =
        mLevelIntent.getLongExtra(LevelActivity.INTENT_DURATION_MS, 6000);
    if (!Strings.isNullOrEmpty(recordingDir)) {
      Log.d(TAG, "Starting to create viewer recording.");
      mOverlayMakeStartTimestamp = new Date().getTime();
      FfmpegExecutor.createVideoFromFrames(recordingDir, recordingDurationMs,
          new ExecuteBinaryResponseHandler() {
            @Override public void onSuccess(String s) {
              Log.d(TAG, "Starting to create overlay.");
              Log.d(TAG, "Viewer recording made in  "
                  + (new Date().getTime() - mOverlayMakeStartTimestamp)
                  + "ms");
              FfmpegExecutor.createOverlay(MainActivity.this, level.getVideo(), recordingDir,
                  new ExecuteBinaryResponseHandler() {
                    @Override public void onSuccess(String s) {
                      Log.d(TAG, "Overlay made in "
                          + (new Date().getTime() - mOverlayMakeStartTimestamp)
                          + "ms");
                      mViewerRecording = getFilesDir() + "/" + FfmpegExecutor.RESULT_MP4;
                      getViewModel().onViewRecordingReady(new Video(null, mViewerRecording, null));
                    }

                    @Override public void onFinish() {
                      super.onFinish();
                      // Delete recording dir
                      File dir = new File(recordingDir);
                      for (File file : dir.listFiles()) {
                        file.delete();
                      }
                      dir.delete();
                    }
                  });
            }

            @Override public void onFinish() {
              super.onFinish();
              // Delete frames from cache dir
              File dir = new File(recordingDir);
              for (File file : dir.listFiles()) {
                if (matches("[0-9]+\\.jpg", file.getName())) {
                  file.delete();
                }
              }
            }
          });
    }
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == RequestCodes.PLAY_LEVEL) {
      mLevelIntent = data;
      final Level level = mLevelIntent.getParcelableExtra(LevelActivity.INTENT_LEVEL);
      getViewModel().onLevelFinished(level.getResult());
    }
  }
}
