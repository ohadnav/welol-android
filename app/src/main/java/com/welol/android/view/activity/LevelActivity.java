package com.welol.android.view.activity;

import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.ImageView;
import butterknife.BindView;
import com.welol.android.R;
import com.welol.android.databinding.ActivityLevelBinding;
import com.welol.android.model.Level;
import com.welol.android.util.AppUtil;
import com.welol.android.util.RequestCodes;
import com.welol.android.viewmodel.LevelViewModel;
import com.welol.android.viewmodel.viewinterface.LevelViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class LevelActivity
    extends BaseActivity<LevelViewInterface, LevelViewModel, ActivityLevelBinding>
    implements LevelViewInterface, TextureView.SurfaceTextureListener {

  public static final String INTENT_LEVEL = "level";
  public static final String INTENT_RESULT = "result";
  public static final String BUNDLE_LEVEL = INTENT_LEVEL;
  @BindView(R.id.videoTextureView) TextureView mVideoTextureView;
  @BindView(R.id.loadingImage) ImageView mLoadingImage;
  private Level mLevel;
  private MediaPlayer mMediaPlayer;
  private boolean mIsMediaPlayerPrepared = false;
  private Size mVideoSize;

  @Override public void play() {
    if (getLifecycleStage() == Stage.RESUMED && mIsMediaPlayerPrepared) {
      mMediaPlayer.start();
    }
  }

  @Override public void replay() {
    if (getLifecycleStage() == Stage.RESUMED && mIsMediaPlayerPrepared) {
      mMediaPlayer.seekTo(0);
      mMediaPlayer.start();
    }
  }

  @Override public void pause() {
    if (mMediaPlayer != null) {
      mMediaPlayer.pause();
    }
  }

  @Override public void finishLevel(Level.Result result) {
    Log.d(TAG, "Level finished with " + result);
    Intent resultIntent = new Intent();
    resultIntent.putExtra(INTENT_RESULT, result);
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
    mVideoTextureView.setSurfaceTextureListener(this);
    // onSurfaceTextureAvailable does not get called if it is already available.
    if (mVideoTextureView.isAvailable()) {
      Size availableDisplaySize = AppUtil.availableDisplaySize(findViewById(android.R.id.content));
      if (availableDisplaySize != null) {
        onSurfaceTextureAvailable(mVideoTextureView.getSurfaceTexture(),
            availableDisplaySize.getWidth(), availableDisplaySize.getHeight());
      }
    }
  }

  @Override public void onStart() {
    super.onStart();
    ((AnimationDrawable) mLoadingImage.getDrawable()).start();
    mLoadingImage.bringToFront();
  }

  @Override public void onDestroy() {
    super.onDestroy();
    killMediaPlayer();
  }

  @Override public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putParcelable(BUNDLE_LEVEL, mLevel);
  }

  /**
   * Displays the video from {@link Level#mVideoUrl} and adds a cute loading animation until it is
   * loaded.
   */
  @Override public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width,
      int height) {
    Uri uri = null;
    if (mLevel.getVideoUrl() != null) {
      uri = Uri.parse(mLevel.getVideoUrl());
      // Initialize media player and retriever from external URI, i.e. from other users videos.
      mMediaPlayer = MediaPlayer.create(this, uri);
    } else if (mLevel.getVideoResourceId() != null) {
      mMediaPlayer = MediaPlayer.create(this, mLevel.getVideoResourceId());
      uri = Uri.parse(
          "android.resource://" + getPackageName() + "/raw/" + mLevel.getVideoResourceId());
    }
    if (mMediaPlayer == null) {
      throw new IllegalStateException("Failed to create media player for " + mLevel.getVideoUrl());
    }
    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
    retriever.setDataSource(this, uri);
    mVideoSize = new Size(Integer.parseInt(
        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)),
        Integer.parseInt(
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)));
    updateTextureViewSize(width, height);
    // Media player properties
    mMediaPlayer.setSurface(new Surface(surfaceTexture));
    mMediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
    // A callback that is invoked once the player is ready to start streaming.
    mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
      @Override public void onPrepared(MediaPlayer mp) {
        Log.d(TAG, "Video is prepared.");
        mLoadingImage.setVisibility(GONE);
        mIsMediaPlayerPrepared = true;
        getViewModel().onVideoPrepared();
        mp.start();
        mp.pause();
      }
    });
    // Buffering callbacks to show loading indication while buffering.
    mMediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
      @Override public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
          case MediaPlayer.MEDIA_INFO_BUFFERING_START:
            Log.d(TAG, "Video is buffering.");
            mLoadingImage.setVisibility(VISIBLE);
            getViewModel().onBufferingStart();
            break;
          case MediaPlayer.MEDIA_INFO_BUFFERING_END:
            Log.d(TAG, "Video buffering completed.");
            mLoadingImage.setVisibility(GONE);
            getViewModel().onBufferingEnd();
            break;
        }
        return false;
      }
    });
    // Completion callbacks, that allows video looping and informing the listener.
    mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
      @Override public void onCompletion(MediaPlayer mediaPlayer) {
        getViewModel().onVideoFinished();
      }
    });
  }

  @Override public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

  }

  @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
    killMediaPlayer();
    return false;
  }

  @Override public void onSurfaceTextureUpdated(SurfaceTexture surface) {

  }

  private void updateTextureViewSize(int viewWidth, int viewHeight) {
    float scaleX = 1.0f;
    float scaleY = 1.0f;

    if (mVideoSize.getWidth() > viewWidth && mVideoSize.getHeight() > viewHeight) {
      scaleX = mVideoSize.getWidth() / (float) viewWidth;
      scaleY = mVideoSize.getHeight() / (float) viewHeight;
    } else if (mVideoSize.getWidth() < viewWidth && mVideoSize.getHeight() < viewHeight) {
      scaleY = viewWidth / (float) mVideoSize.getWidth();
      scaleX = viewHeight / (float) mVideoSize.getHeight();
    } else if (viewWidth > mVideoSize.getWidth()) {
      scaleY = (viewWidth / (float) mVideoSize.getWidth()) / (viewHeight
          / (float) mVideoSize.getHeight());
    } else if (viewHeight > mVideoSize.getHeight()) {
      scaleX = (viewHeight / (float) mVideoSize.getHeight()) / (viewWidth
          / (float) mVideoSize.getWidth());
    }

    // Calculate pivot points, in our case crop from center
    int pivotPointX = viewWidth / 2;
    int pivotPointY = viewHeight / 2;

    float minScale = Math.min(scaleX, scaleY);
    scaleX /= minScale;
    scaleY /= minScale;

    Matrix matrix = new Matrix();
    matrix.setScale(scaleX, scaleY, pivotPointX, pivotPointY);
    mVideoTextureView.setTransform(matrix);
  }

  private void killMediaPlayer() {
    if (mMediaPlayer != null) {
      mMediaPlayer.stop();
      mMediaPlayer.release();
      mMediaPlayer = null;
    }
  }
}
