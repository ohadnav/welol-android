package com.welol.android.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.StringRes;
import com.welol.android.R;

/**
 * Proudly created by ohad on 04/12/2017 for TrueThat.
 */

public class Level implements Parcelable {
  @SuppressWarnings("WeakerAccess") public static final Parcelable.Creator<Level> CREATOR =
      new Parcelable.Creator<Level>() {
    @Override public Level createFromParcel(Parcel source) {
      return new Level(source);
    }

    @Override public Level[] newArray(int size) {
      return new Level[size];
    }
  };
  private Video mVideo;
  private Result mResult;

  public Level(Integer videoResourceId) {
    mVideo = new Video(null, null, videoResourceId);
  }

  private Level(Parcel in) {
    mVideo = (Video) in.readValue(Video.class.getClassLoader());
    mResult = (Result) in.readValue(Result.class.getClassLoader());
  }

  public Video getVideo() {
    return mVideo;
  }

  public Result getResult() {
    return mResult;
  }

  public void setResult(Result result) {
    mResult = result;
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeValue(mVideo);
    dest.writeValue(mResult);
  }

  @Override public int hashCode() {
    int result = mVideo != null ? mVideo.hashCode() : 0;
    result = 31 * result + (mResult != null ? mResult.hashCode() : 0);
    return result;
  }

  @SuppressWarnings("SimplifiableIfStatement") @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Level)) return false;

    Level level = (Level) o;

    if (mVideo != null ? !mVideo.equals(level.mVideo) : level.mVideo != null) return false;
    return mResult == level.mResult;
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
