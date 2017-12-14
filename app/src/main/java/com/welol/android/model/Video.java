package com.welol.android.model;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import com.welol.android.app.App;
import java.io.File;

/**
 * Proudly created by ohad on 11/12/2017 for TrueThat.
 */

public class Video implements Parcelable {
  static final Parcelable.Creator<Video> CREATOR = new Parcelable.Creator<Video>() {
    @Override public Video createFromParcel(Parcel source) {
      return new Video(source);
    }

    @Override public Video[] newArray(int size) {
      return new Video[size];
    }
  };
  private String mExternalUrl;
  private String mInternalUrl;
  @RawRes private Integer mResourceId;

  public Video(@Nullable String externalUrl, @Nullable String internalUrl,
      @Nullable Integer resourceId) {
    mExternalUrl = externalUrl;
    mInternalUrl = internalUrl;
    mResourceId = resourceId;
  }

  private Video(Parcel in) {
    mExternalUrl = (String) in.readValue(String.class.getClassLoader());
    mInternalUrl = (String) in.readValue(String.class.getClassLoader());
    mResourceId = (Integer) in.readValue(Integer.class.getClassLoader());
  }

  public MediaPlayer createMediaPlayer(Context context) {
    if (mExternalUrl != null || mInternalUrl != null) {
      return MediaPlayer.create(context, getUri());
    } else if (mResourceId != null) {
      return MediaPlayer.create(context, mResourceId);
    }
    return null;
  }

  public Uri getUri() {
    if (mExternalUrl != null) {
      Uri.parse(mExternalUrl);
    } else if (mInternalUrl != null) {
      return Uri.fromFile(new File(mInternalUrl));
    } else if (mResourceId != null) {
      return Uri.parse("android.resource://" + App.sPackageName + "/raw/" + mResourceId);
    }
    return null;
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeValue(mExternalUrl);
    dest.writeValue(mInternalUrl);
    dest.writeValue(mResourceId);
  }

  @Override public int hashCode() {
    int result = mExternalUrl != null ? mExternalUrl.hashCode() : 0;
    result = 31 * result + (mInternalUrl != null ? mInternalUrl.hashCode() : 0);
    result = 31 * result + (mResourceId != null ? mResourceId.hashCode() : 0);
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Video)) return false;

    Video video = (Video) o;

    if (mExternalUrl != null ? !mExternalUrl.equals(video.mExternalUrl)
        : video.mExternalUrl != null) {
      return false;
    }
    if (mInternalUrl != null ? !mInternalUrl.equals(video.mInternalUrl)
        : video.mInternalUrl != null) {
      return false;
    }
    return mResourceId != null ? mResourceId.equals(video.mResourceId) : video.mResourceId == null;
  }
}

