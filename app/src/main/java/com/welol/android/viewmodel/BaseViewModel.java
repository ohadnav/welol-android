package com.welol.android.viewmodel;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import com.welol.android.viewmodel.viewinterface.BaseListener;
import com.welol.android.viewmodel.viewinterface.BaseViewInterface;

/**
 * Proudly created by ohad on 19/07/2017 for TrueThat.
 */

public class BaseViewModel<ViewInterface extends BaseViewInterface> implements BaseListener {
  /**
   * Logging tag. Assigned per implementing class in {@link #onCreate(Bundle, Bundle)}.
   */
  String TAG = this.getClass().getSimpleName();
  private LifecycleStage mLifecycleStage = LifecycleStage.STOPPED;
  @Nullable private String mUniqueIdentifier;
  @Nullable private ViewInterface mView;
  private boolean mBindViewWasCalled;
  private Context mContext;

  /**
   * @return An app unique identifier for the current viewmodel instance (will be kept during
   * orientation
   * change). This identifier will be reset in case the corresponding activity is killed.
   */
  @SuppressWarnings("unused") @Nullable public String getUniqueIdentifier() {
    return mUniqueIdentifier;
  }

  public void setUniqueIdentifier(@NonNull final String uniqueIdentifier) {
    mUniqueIdentifier = uniqueIdentifier;
  }

  /**
   * This method is an equivalent of {@link Fragment#onViewCreated(View, Bundle)} or {@link
   * Activity#onCreate(Bundle)}.
   * At this point, the View is ready and you can initialise it.
   *
   * @param view - View assigned to this ViewModel
   */
  @CallSuper public void onBindView(@NonNull ViewInterface view) {
    mBindViewWasCalled = true;
    mView = view;
    TAG = this.getClass().getSimpleName();
    if (getView() != null) {
      TAG += "(" + getView().getTAG() + ")";
    }
    Log.d(TAG, "onBindView");
  }

  @SuppressWarnings("unused") @CallSuper
  public void onCreate(@Nullable Bundle arguments, @Nullable Bundle savedInstanceState) {
    Log.d(TAG, "onCreate");
  }

  @Nullable public ViewInterface getView() {
    return mView;
  }

  @CallSuper public void onStop() {
    Log.d(TAG, "onStop");
    mLifecycleStage = LifecycleStage.STOPPED;
  }

  @CallSuper public void onStart() {
    Log.d(TAG, "onStart");
    if (mView == null && !mBindViewWasCalled) {
      Log.e(TAG,
          "No view associated. You probably did not call setModelView() in your Fragment or Activity.");
    }
    mLifecycleStage = LifecycleStage.STARTED;
  }

  @CallSuper public void onResume() {
    Log.d(TAG, "onResume");
    mLifecycleStage = LifecycleStage.RESUMED;
  }

  @CallSuper public void onPause() {
    Log.d(TAG, "onPause");
    mLifecycleStage = LifecycleStage.PAUSED;
  }

  @CallSuper public void onSaveInstanceState(@NonNull Bundle outState) {
    Log.d(TAG, "onSaveInstanceState");
  }

  public void onRestoreInstanceState(Bundle savedInstanceState) {
    Log.d(TAG, "onRestoreInstanceState");
  }

  @CallSuper public void onDestroy() {
    Log.d(TAG, "onDestroy");
  }

  @Override public String getTAG() {
    return TAG;
  }

  @Override public String toString() {
    return TAG;
  }

  @CallSuper public void clearView() {
    mView = null;
  }

  Context getContext() {
    return mContext;
  }

  public void setContext(Context context) {
    mContext = context;
  }

  /**
   * The
   */
  enum LifecycleStage {
    STOPPED, PAUSED, STARTED, RESUMED
  }
}
