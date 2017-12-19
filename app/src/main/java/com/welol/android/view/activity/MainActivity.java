package com.welol.android.view.activity;

import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.ProgressBar;
import butterknife.BindView;
import com.crashlytics.android.Crashlytics;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.common.base.Strings;
import com.welol.android.BuildConfig;
import com.welol.android.R;
import com.welol.android.databinding.ActivityMainBinding;
import com.welol.android.model.Level;
import com.welol.android.model.LevelsProvider;
import com.welol.android.model.RandomLevelProvider;
import com.welol.android.model.Video;
import com.welol.android.util.FfmpegExecutor;
import com.welol.android.util.RequestCodes;
import com.welol.android.view.custom.StyledButton;
import com.welol.android.view.fragment.VideoFragment;
import com.welol.android.viewmodel.MainViewModel;
import com.welol.android.viewmodel.viewinterface.MainViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;
import io.fabric.sdk.android.Fabric;
import java.io.File;
import java.util.Date;

import static com.welol.android.app.App.FILE_PROVIDER_AUTHORITY;
import static com.welol.android.util.FfmpegExecutor.getResultPath;
import static java.util.regex.Pattern.matches;

public class MainActivity
    extends BaseActivity<MainViewInterface, MainViewModel, ActivityMainBinding>
    implements MainViewInterface {
  private static final String BUNDLE_VIEWER_RECORDING = "viewerRecording";
  @BindView(R.id.progressBar) ProgressBar mProgressBar;
  @BindView(R.id.button) StyledButton mButton;
  private LevelsProvider mLevelsProvider;
  private VideoFragment mVideoFragment;
  private String mViewerRecording;
  private long mOverlayMakeStartTimestamp;
  private Intent mLevelIntent;
  private InterstitialAd mInterstitialAd;

  @Override public ViewModelBindingConfig getViewModelBindingConfig() {
    return new ViewModelBindingConfig(R.layout.activity_main, this);
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Fabric.with(this, new Crashlytics());
    mLevelsProvider = new RandomLevelProvider();
    mViewerRecording = getResultPath(this);
  }

  @Override public void onStart() {
    super.onStart();
    mProgressBar.bringToFront();
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
    //Target WhatsApp:
    shareIntent.setPackage("com.whatsapp");
    //Add text and then Image URI
    shareIntent.putExtra(Intent.EXTRA_TEXT,
        getResources().getString(R.string.share_text, passedLevels));
    if (mViewerRecording != null) {
      shareIntent.setType("video/*");
      shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
      shareIntent.putExtra(Intent.EXTRA_STREAM,
          FileProvider.getUriForFile(this, FILE_PROVIDER_AUTHORITY, new File(mViewerRecording)));
    } else {
      shareIntent.setType("text/plain");
    }
    try {
      startActivity(shareIntent);
    } catch (Exception e) {
      Snackbar.make(findViewById(android.R.id.content), getString(R.string.share_error),
          Snackbar.LENGTH_SHORT).show();
    }
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
    mProgressBar.bringToFront();
  }

  @Override public void generateViewerRecordingOverlay() {
    final Level level = mLevelIntent.getParcelableExtra(LevelActivity.INTENT_LEVEL);
    final String recordingDir = mLevelIntent.getStringExtra(LevelActivity.INTENT_RECORDING);
    final long recordingDurationMs =
        mLevelIntent.getLongExtra(LevelActivity.INTENT_DURATION_MS, 6000);
    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
    retriever.setDataSource(this, level.getVideo().getUri());
    final long levelDurationMs =
        Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
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
                      mViewerRecording = getResultPath(MainActivity.this);
                      getViewModel().onViewerRecordingReady(
                          new Video(null, mViewerRecording, null));
                    }

                    @SuppressWarnings("ResultOfMethodCallIgnored") @Override
                    public void onFinish() {
                      super.onFinish();
                      // Delete recording dir
                      File dir = new File(recordingDir);
                      for (File file : dir.listFiles()) {
                        file.delete();
                      }
                      dir.delete();
                    }

                    @Override public void onProgress(String message) {
                      super.onProgress(message);
                      getViewModel().onViewerRecordingProgress(message, false, levelDurationMs);
                    }

                    @Override public void onFailure(String message) {
                      super.onFailure(message);
                      getViewModel().onViewerRecordingFailed();
                    }
                  });
            }

            @SuppressWarnings("ResultOfMethodCallIgnored") @Override public void onFinish() {
              super.onFinish();
              // Delete frames from cache dir
              File dir = new File(recordingDir);
              for (File file : dir.listFiles()) {
                if (matches("[0-9]+\\.jpg", file.getName())) {
                  file.delete();
                }
              }
            }

            @Override public void onProgress(String message) {
              super.onProgress(message);
              getViewModel().onViewerRecordingProgress(message, true, recordingDurationMs);
            }

            @Override public void onFailure(String message) {
              super.onFailure(message);
              getViewModel().onViewerRecordingFailed();
            }
          });
    }
  }

  @Override public void showAd() {
    runOnUiThread(new Runnable() {
      @Override public void run() {
        if (mInterstitialAd.isLoaded()) {
          mInterstitialAd.show();
        } else {
          mInterstitialAd.setAdListener(new AdListener() {
            @Override public void onAdLoaded() {
              super.onAdLoaded();
              mInterstitialAd.show();
            }
          });
        }
      }
    });
  }

  @Override public void loadAd() {
    runOnUiThread(new Runnable() {
      @Override public void run() {
        mInterstitialAd = new InterstitialAd(MainActivity.this);
        mInterstitialAd.setAdUnitId(BuildConfig.AD_UNIT_ID);
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
      }
    });
  }

  @Override public void onViewerRecordingFailed() {
    Snackbar.make(findViewById(android.R.id.content), getString(R.string.overlay_error),
        Snackbar.LENGTH_SHORT).show();
    mViewerRecording = null;
  }

  @Override public void inactivateShare() {
    mButton.setActive(false);
  }

  @Override public void activateShare() {
    mButton.setActive(true);
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
