package com.welol.android.view.fragment;

import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import butterknife.BindView;
import com.welol.android.R;
import com.welol.android.databinding.FragmentVideoBinding;
import com.welol.android.model.Video;
import com.welol.android.util.AppUtil;
import com.welol.android.viewmodel.BaseFragmentViewModel;
import com.welol.android.viewmodel.viewinterface.BaseFragmentViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Proudly created by ohad on 11/12/2017 for TrueThat.
 */

public class VideoFragment extends
    BaseFragment<BaseFragmentViewInterface, BaseFragmentViewModel<BaseFragmentViewInterface>, FragmentVideoBinding>
    implements TextureView.SurfaceTextureListener {
  public static final String BUNDLE_VIDEO = "video";
  public static final String BUNDLE_MUTE = "mute";
  public static final String BUNDLE_AUTOPLAY = "autoPlay";
  public static final String BUNDLE_LOOPING = "looping";
  public static final String ARG_VIDEO = BUNDLE_VIDEO;
  @BindView(R.id.videoTextureView) TextureView mTextureView;
  @BindView(R.id.loadingImage) ImageView mLoadingImage;
  private MediaPlayer mMediaPlayer;
  private boolean mIsMediaPlayerPrepared = false;
  private Size mVideoSize;
  private Video mVideo;
  private VideoListener mVideoListener;
  private boolean mMute = false;
  private boolean mAutoPlay = false;
  private boolean mLooping = false;

  public static VideoFragment newInstance(Video video) {
    Bundle args = new Bundle();
    args.putParcelable(ARG_VIDEO, video);
    VideoFragment fragment = new VideoFragment();
    fragment.setArguments(args);
    return fragment;
  }

  public boolean isMediaPlayerPrepared() {
    return mIsMediaPlayerPrepared;
  }

  public void setVideoListener(VideoListener videoListener) {
    mVideoListener = videoListener;
    if (mIsMediaPlayerPrepared) {
      videoListener.onPrepared();
    }
  }

  public void setAutoPlay(boolean autoPlay) {
    mAutoPlay = autoPlay;
    if (autoPlay && mIsMediaPlayerPrepared) {
      playOrResume();
    }
  }

  public void setLooping(boolean looping) {
    mLooping = looping;
  }

  public void setMute(boolean mute) {
    mMute = mute;
  }

  @Nullable @Override public ViewModelBindingConfig getViewModelBindingConfig() {
    if (getContext() == null) {
      return null;
    }
    return new ViewModelBindingConfig(R.layout.fragment_video, getContext());
  }

  public void playOrResume() {
    if (mIsMediaPlayerPrepared) {
      mMediaPlayer.start();
    }
  }

  public void skipToStart() {
    if (mIsMediaPlayerPrepared) {
      mMediaPlayer.seekTo(0);
    }
  }

  public void pause() {
    if (mMediaPlayer != null) {
      mMediaPlayer.pause();
    }
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      if (getArguments().get(ARG_VIDEO) != null) {
        mVideo = getArguments().getParcelable(ARG_VIDEO);
      }
    }
  }

  @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mTextureView.setSurfaceTextureListener(this);
    // onSurfaceTextureAvailable does not get called if it is already available.
    if (mTextureView.isAvailable()) {
      Size availableDisplaySize = AppUtil.availableDisplaySize(view);
      if (availableDisplaySize != null) {
        onSurfaceTextureAvailable(mTextureView.getSurfaceTexture(), availableDisplaySize.getWidth(),
            availableDisplaySize.getHeight());
      }
    }
  }

  @Override public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
    super.onViewStateRestored(savedInstanceState);
    if (savedInstanceState != null) {
      if (savedInstanceState.get(BUNDLE_VIDEO) != null) {
        mVideo = savedInstanceState.getParcelable(BUNDLE_VIDEO);
      }
      if (savedInstanceState.get(BUNDLE_MUTE) != null) {
        mMute = savedInstanceState.getBoolean(BUNDLE_MUTE);
      }
      if (savedInstanceState.get(BUNDLE_AUTOPLAY) != null) {
        mAutoPlay = savedInstanceState.getBoolean(BUNDLE_AUTOPLAY);
      }
      if (savedInstanceState.get(BUNDLE_LOOPING) != null) {
        mLooping = savedInstanceState.getBoolean(BUNDLE_LOOPING);
      }
    }
  }

  @Override public void onStart() {
    super.onStart();
    ((AnimationDrawable) mLoadingImage.getDrawable()).start();
    mLoadingImage.bringToFront();
  }

  @Override public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putParcelable(BUNDLE_VIDEO, mVideo);
    outState.putBoolean(BUNDLE_MUTE, mMute);
    outState.putBoolean(BUNDLE_AUTOPLAY, mAutoPlay);
    outState.putBoolean(BUNDLE_LOOPING, mLooping);
  }

  @Override public void onDestroy() {
    super.onDestroy();
    killMediaPlayer();
  }

  /**
   * Displays the video and adds a cute loading animation until it is loaded.
   */
  @Override public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width,
      int height) {
    mMediaPlayer = mVideo.createMediaPlayer(getContext());
    if (mMediaPlayer == null) {
      throw new IllegalStateException("Failed to create media player for " + mVideo);
    }
    if (getContext() == null) {
      throw new IllegalStateException("Fragment has no context.");
    }
    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
    retriever.setDataSource(getContext(), mVideo.getUri());
    mVideoSize = new Size(Integer.parseInt(
        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)),
        Integer.parseInt(
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)));
    retriever.release();
    updateTextureViewSize(width, height);
    // Media player properties
    mMediaPlayer.setSurface(new Surface(surfaceTexture));
    mMediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
    // A callback that is invoked once the player is ready to start streaming.
    mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
      @Override public void onPrepared(MediaPlayer mp) {
        Log.d(TAG, "Video is prepared.");
        mLoadingImage.setVisibility(GONE);
        mIsMediaPlayerPrepared = true;
        if (mMute) {
          mp.setVolume(0, 0);
        }
        mp.start();
        if (!mAutoPlay) {
          mp.pause();
        }
        if (mVideoListener != null) {
          mVideoListener.onPrepared();
        }
      }
    });
    // Buffering callbacks to show loading indication while buffering.
    mMediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
      @Override public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
          case MediaPlayer.MEDIA_INFO_BUFFERING_START:
            Log.d(TAG, "Video is buffering...");
            mLoadingImage.setVisibility(VISIBLE);
            if (mVideoListener != null) {
              mVideoListener.onBufferingStart();
            }
            break;
          case MediaPlayer.MEDIA_INFO_BUFFERING_END:
            Log.d(TAG, "Video buffering completed.");
            mLoadingImage.setVisibility(GONE);
            if (mVideoListener != null) {
              mVideoListener.onBufferingEnd();
            }
            break;
        }
        return false;
      }
    });
    // Completion callbacks, that allows video looping and informing the listener.
    mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
      @Override public void onCompletion(MediaPlayer mediaPlayer) {
        if (mVideoListener != null) {
          mVideoListener.onFinished();
        }
        if (mLooping) {
          skipToStart();
          playOrResume();
        }
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

    float maxScale = Math.max(scaleX, scaleY);
    scaleX /= maxScale;
    scaleY /= maxScale;

    Matrix matrix = new Matrix();
    matrix.setScale(scaleX, scaleY, pivotPointX, pivotPointY);
    mTextureView.setTransform(matrix);
  }

  private void killMediaPlayer() {
    if (mMediaPlayer != null) {
      mMediaPlayer.stop();
      mMediaPlayer.release();
      mMediaPlayer = null;
    }
  }

  public interface VideoListener {
    void onBufferingStart();

    void onBufferingEnd();

    void onFinished();

    void onPrepared();
  }
}
