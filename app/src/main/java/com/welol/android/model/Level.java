package com.welol.android.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.RawRes;
import android.support.annotation.StringRes;
import com.welol.android.R;

/**
 * Proudly created by ohad on 04/12/2017 for TrueThat.
 */

public class Level implements Parcelable {
  static final Parcelable.Creator<Level> CREATOR = new Parcelable.Creator<Level>() {
    @Override public Level createFromParcel(Parcel source) {
      return new Level(source);
    }

    @Override public Level[] newArray(int size) {
      return new Level[size];
    }
  };
  private String mVideoUrl;
  @RawRes private Integer mVideoResourceId;

  public Level(String videoUrl) {
    mVideoUrl = videoUrl;
  }

  public Level(Integer videoResourceId) {
    mVideoResourceId = videoResourceId;
  }

  private Level(Parcel in) {
    mVideoUrl = (String) in.readValue(String.class.getClassLoader());
    mVideoResourceId = (Integer) in.readValue(Integer.class.getClassLoader());
  }

  public Integer getVideoResourceId() {
    return mVideoResourceId;
  }

  public String getVideoUrl() {
    return mVideoUrl;
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeValue(mVideoUrl);
    dest.writeValue(mVideoResourceId);
  }

  public enum Result {
    WIN(R.string.result_win), LOSE(R.string.result_lose);

    @StringRes private int descriptionResourceId;

    Result(int descriptionResourceId) {
      this.descriptionResourceId = descriptionResourceId;
    }

    public int getDescriptionResourceId() {
      return descriptionResourceId;
    }
  }
}
