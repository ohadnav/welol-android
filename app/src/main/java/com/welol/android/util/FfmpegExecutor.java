package com.welol.android.util;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Size;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.welol.android.app.App;
import com.welol.android.model.Video;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Proudly created by ohad on 13/12/2017 for TrueThat.
 */

public class FfmpegExecutor {
  public static final String RESULT_MP4 = "result.mp4";
  private static final String VIEWER_RECORDING_MP4 = "viewerRecording.mp4";
  private static final String VIDEO_MP4 = "video.mp4";
  private static final String TAG = FfmpegExecutor.class.getSimpleName();
  private static final float MAX_OVERLAY_DIMENSION = 240F;
  private static final float MAX_RECORDING_DIMENSION = 120F;

  private static void execCommand(final String[] command,
      @Nullable final ExecuteBinaryResponseHandler responseHandler) {
    ExecuteBinaryResponseHandler actual =
        responseHandler == null ? new DefaultExecuteBinaryResponseHandler(command)
            : new DefaultExecuteBinaryResponseHandler(command) {
              @Override public void onSuccess(String s) {
                super.onSuccess(s);
                responseHandler.onSuccess(s);
              }

              @Override public void onProgress(String s) {
                super.onProgress(s);
                responseHandler.onProgress(s);
              }

              @Override public void onFailure(String s) {
                super.onFailure(s);
                responseHandler.onFailure(s);
              }

              @Override public void onStart() {
                super.onStart();
                responseHandler.onStart();
              }

              @Override public void onFinish() {
                super.onFinish();
                responseHandler.onFinish();
              }
            };
    try {
      App.getFFmpeg().execute(command, actual);
    } catch (FFmpegCommandAlreadyRunningException e) {
      AppUtil.handleThrowable(e);
    }
  }

  public static void createVideoFromFrames(String framesDir, float durationMs,
      @Nullable ExecuteBinaryResponseHandler responseHandler) {
    execCommand(generateVideoFromFramesCommand(framesDir, durationMs), responseHandler);
  }

  public static void createOverlay(Context context, Video video, String dirPath,
      @Nullable ExecuteBinaryResponseHandler responseHandler) {
    Size videoSize = getVideoSize(context, video.getUri());
    String videoPath = getAbsolutePath(context, video.getUri(), dirPath);
    if (videoPath != null) {
      execCommand(generateOverlayCommand(context, dirPath, videoSize), responseHandler);
    } else if (responseHandler != null) {
      responseHandler.onFailure("Could not create video file.");
    }
  }

  private static Size getVideoSize(Context context, Uri uri) {
    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
    retriever.setDataSource(context, uri);
    Size videoSize = new Size(Integer.parseInt(
        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)),
        Integer.parseInt(
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)));
    retriever.release();
    return videoSize;
  }

  @Nullable private static String getAbsolutePath(Context context, Uri uri, String dirPath) {
    try {
      InputStream inputStream = context.getContentResolver().openInputStream(uri);
      File video = new File(dirPath, VIDEO_MP4);
      int size = inputStream.available();
      byte[] buffer = new byte[size];
      inputStream.read(buffer);
      inputStream.close();

      FileOutputStream outputStream = new FileOutputStream(video);
      outputStream.write(buffer);
      outputStream.close();
      return video.getAbsolutePath();
    } catch (Exception e) {
      AppUtil.handleThrowable(e);
      return null;
    }
  }

  private static String[] generateOverlayCommand(Context context, String dirPath, Size videoSize) {
    String filter = "[0] setpts=PTS-STARTPTS, scale="
        + getScaleString(videoSize, MAX_OVERLAY_DIMENSION)
        + " [video];"
        + "[video][1] overlay=shortest=1:x=W-w-5:y=H-h-5 [result]";
    return new String[] {
        "-y", "-i", dirPath + "/" + VIDEO_MP4, "-i", dirPath + "/" + VIEWER_RECORDING_MP4,
        "-filter_complex", filter, "-map", "[result]", "-vcodec", "libx264", "-acodec", "copy",
        "-map", "0:a", "-shortest", new File(context.getFilesDir(), RESULT_MP4).getAbsolutePath()
    };
  }

  private static String getScaleString(Size size, float maxDimension) {
    float scale = (size.getWidth() > size.getHeight()) ? maxDimension / (float) size.getWidth()
        : maxDimension / (float) size.getHeight();

    return Math.round(scale * size.getWidth()) + "x" + Math.round(scale * size.getHeight());
  }

  private static String[] generateVideoFromFramesCommand(String framesDir, float durationMs) {
    File dir = new File(framesDir);
    String frameRate = "" + dir.listFiles().length * 1000 / durationMs;
    String scale;
    // Inverse dimensions, as we are about to transpose.
    int width = App.getCameraHelper().getFrameSize().getHeight();
    int height = App.getCameraHelper().getFrameSize().getWidth();
    if (width > height) {
      scale = MAX_RECORDING_DIMENSION + "x" + Math.round(
          height * MAX_RECORDING_DIMENSION / (float) width);
    } else {
      scale = Math.round(width * MAX_RECORDING_DIMENSION / (float) height)
          + "x"
          + MAX_RECORDING_DIMENSION;
    }
    return new String[] {
        "-y", "-r", frameRate, "-i", framesDir + "/%d.jpg", "-pix_fmt", "yuv420p", "-vcodec",
        "libx264", "-r", frameRate, "-vf", "transpose=2", "-s", scale,
        new File(framesDir, VIEWER_RECORDING_MP4).getAbsolutePath()
    };
  }

  public static class DefaultExecuteBinaryResponseHandler extends ExecuteBinaryResponseHandler {
    private String[] mCommand;

    DefaultExecuteBinaryResponseHandler(String[] command) {
      mCommand = command;
    }

    @Override public void onSuccess(String s) {
      Log.d(TAG, "SUCCESS with output : " + s);
      //Perform action on success
    }

    @Override public void onProgress(String s) {
      Log.v(TAG, "progress : " + s);
    }

    @Override public void onFailure(String s) {
      Log.e(TAG, "FAILED with output : " + s);
    }

    @Override public void onStart() {
      Log.d(TAG, "Started command : ffmpeg " + Arrays.toString(mCommand));
    }

    @Override public void onFinish() {
      Log.d(TAG, "Finished command : ffmpeg " + Arrays.toString(mCommand));
    }
  }
}
