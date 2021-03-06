package com.welol.android.view.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.CallSuper;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;
import butterknife.ButterKnife;
import com.crashlytics.android.Crashlytics;
import com.welol.android.BuildConfig;
import com.welol.android.app.App;
import com.welol.android.app.LoggingKey;
import com.welol.android.app.permissions.Permission;
import com.welol.android.external.viewmodel.ProxyViewHelper;
import com.welol.android.external.viewmodel.ViewModelHelper;
import com.welol.android.external.viewmodel.ViewModelProvider;
import com.welol.android.util.AppUtil;
import com.welol.android.viewmodel.BaseViewModel;
import com.welol.android.viewmodel.viewinterface.BaseListener;
import com.welol.android.viewmodel.viewinterface.BaseViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Proudly created by ohad on 13/06/2017 for TrueThat.
 */

public abstract class BaseActivity<ViewInterface extends BaseViewInterface, ViewModel extends BaseViewModel<ViewInterface>, DataBinding extends ViewDataBinding>
    extends AppCompatActivity implements BaseViewInterface, BaseListener {
  /**
   * {@link BaseViewModel} manager of this activity.
   */
  @NonNull private final ViewModelHelper<ViewInterface, ViewModel> mViewModelHelper =
      new ViewModelHelper<>();
  /**
   * Logging tag. Assigned per implementing class in {@link #onCreate(Bundle)}.
   */
  String TAG = this.getClass().getSimpleName();

  private Snackbar mSnackbar;
  private Stage mLifecycleStage = Stage.DESTROYED;
  @Nullable private ViewModelProvider mViewModelProvider;

  public Stage getLifecycleStage() {
    return mLifecycleStage;
  }

  @Nullable public ViewModelProvider getViewModelProvider() {
    return mViewModelProvider;
  }

  /**
   * Permission granted callback.
   *
   * @param permission that was just granted.
   */
  @MainThread public void onPermissionGranted(Permission permission) {
    Log.d(TAG, "Permission " + permission + " granted.");
  }

  /**
   * Permission not granted callback.
   *
   * @param permission that was just rejected.
   */
  @MainThread public void onPermissionRejected(Permission permission) {
    Log.w(TAG, "Permission " + permission + " rejected.");
    startAskForPermissionActivity(permission);
  }

  @CallSuper @Override public void onCreate(@Nullable Bundle savedInstanceState,
      @Nullable PersistableBundle persistentState) {
    Log.d(TAG, "onCreate(Bundle, PersistableBundle)");
    super.onCreate(savedInstanceState, persistentState);
    initializeViewModel();
  }

  @Override protected void onRestoreInstanceState(Bundle savedInstanceState) {
    Log.d(TAG, "onRestoreInstanceState");
    super.onRestoreInstanceState(savedInstanceState);
    getViewModel().onRestoreInstanceState(savedInstanceState);
  }

  /**
   * Call this after your view is ready - usually on the end of {@link
   * android.app.Activity#onCreate(Bundle)}
   *
   * @param view view
   */
  @SuppressWarnings("unused") public void setModelView(@NonNull final ViewInterface view) {
    mViewModelHelper.setView(view);
  }

  /**
   * @see eu.inloop.viewmodel.ViewModelHelper#getViewModel()
   */
  @SuppressWarnings("unused") @NonNull public ViewModel getViewModel() {
    return mViewModelHelper.getViewModel();
  }

  @Override public abstract ViewModelBindingConfig getViewModelBindingConfig();

  @Override public void removeViewModel() {
    mViewModelHelper.removeViewModel(this);
  }

  @SuppressWarnings({ "unused", "ConstantConditions", "unchecked" }) @NonNull
  public DataBinding getBinding() {
    try {
      return (DataBinding) mViewModelHelper.getBinding();
    } catch (ClassCastException e) {
      AppUtil.handleThrowable(e);
      throw new IllegalStateException("Method getViewModelBindingConfig() has to return same "
          + "ViewDataBinding type as it is set to base Fragment");
    }
  }

  public App getApp() {
    return (App) getApplication();
  }

  @SuppressWarnings("unchecked") @Override @CallSuper
  public void onCreate(@Nullable Bundle savedInstanceState) {
    Log.d(TAG, "onCreate(Bundle)");
    //This code must be execute prior to super.onCreate()
    mViewModelProvider = ViewModelProvider.newInstance(this);
    // Initializes activity class.
    super.onCreate(savedInstanceState);
    // Disable landscape orientation.
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    // Initializes view model
    Class<ViewModel> viewModelClass =
        (Class<ViewModel>) ProxyViewHelper.getGenericType(getClass(), BaseViewModel.class);
    mViewModelHelper.onCreate(this, savedInstanceState, viewModelClass, getIntent().getExtras());
    // Bind the activity to its view model.
    initializeViewModel();
    // Bind views references.
    ButterKnife.bind(this);
    mLifecycleStage = Stage.CREATED;
  }

  @CallSuper @Override public void onStart() {
    Log.d(TAG, "onStart");
    super.onStart();
    mViewModelHelper.onStart();
    mLifecycleStage = Stage.STARTED;
  }

  @CallSuper @Override public void onStop() {
    Log.d(TAG, "onStop");
    super.onStop();
    if (isFinishing()) {
      if (null == mViewModelProvider) {
        throw new IllegalStateException(
            "ViewModelProvider for activity " + this + " was null."); //NON-NLS
      }
      mViewModelProvider.removeAllViewModels();
    }
    mViewModelHelper.onStop();
    mLifecycleStage = Stage.STOPPED;
  }

  @CallSuper @Override public void onDestroy() {
    Log.d(TAG, "onDestroy");
    mViewModelHelper.onDestroy(this);
    super.onDestroy();
    mLifecycleStage = Stage.DESTROYED;
  }

  @CallSuper @Override public void onSaveInstanceState(@NonNull final Bundle outState) {
    Log.d(TAG, "onSaveInstanceState");
    super.onSaveInstanceState(outState);
    mViewModelHelper.onSaveInstanceState(outState);
  }

  @Override public void toast(String text) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
  }

  @Override public void snackbar(@StringRes int stringResourceId, int duration) {
    if (mSnackbar != null) {
      mSnackbar.dismiss();
    }
    mSnackbar = Snackbar.make(findViewById(android.R.id.content), stringResourceId, duration);
    mSnackbar.show();
  }

  @Override public void hideSnackbar() {
    if (mSnackbar != null && mSnackbar.isShown()) {
      mSnackbar.dismiss();
    }
  }

  @Override public BaseActivity getBaseActivity() {
    return this;
  }

  @Override public String getTAG() {
    return TAG;
  }

  @Override public String toString() {
    return TAG;
  }

  /**
   * Starts an ask for permission activity, where the user must enable the permission or he cannot
   * continue use the app.
   *
   * @param permission to grant.
   */
  public void startAskForPermissionActivity(Permission permission) {
    Intent askForPermission = new Intent(this, AskForPermissionActivity.class);
    askForPermission.putExtra(AskForPermissionActivity.EXTRA_PERMISSION, permission);
    startActivityForResult(askForPermission, permission.getRequestCode());
  }

  @Override protected void onPause() {
    Log.d(TAG, "onPause");
    super.onPause();
    getViewModel().onPause();
    mLifecycleStage = Stage.PAUSED;
    if (mSnackbar != null && mSnackbar.isShown()) {
      mSnackbar.dismiss();
    }
  }

  @CallSuper @Override public void onResume() {
    Log.d(TAG, "onResume");
    super.onResume();
    if (!BuildConfig.DEBUG) {
      Crashlytics.setString(LoggingKey.ACTIVITY.name(), TAG);
    }
    getViewModel().onResume();
    mLifecycleStage = Stage.RESUMED;
  }

  @Override @Nullable public Object onRetainCustomNonConfigurationInstance() {
    return mViewModelProvider;
  }

  @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    Log.d(TAG, "onRequestPermissionsResult");
    for (int i = 0; i < permissions.length; i++) {
      Permission permission = Permission.fromManifest(permissions[i]);
      if (grantResults[i] != PERMISSION_GRANTED) {
        onPermissionRejected(permission);
      } else if (grantResults[i] == PERMISSION_GRANTED) {
        onPermissionGranted(permission);
      }
    }
  }

  /**
   * Initialize data-view binding for this activity, and injects dependencies to the view model.
   */
  @SuppressWarnings("unchecked") private void initializeViewModel() {
    mViewModelHelper.performBinding(this);
    mViewModelHelper.setView((ViewInterface) this);
    // Ensures data binding was made.
    if (mViewModelHelper.getBinding() == null) {
      throw new IllegalStateException("Binding cannot be null.");
    }
  }

  enum Stage {
    /**
     * Indicates that onCreate has been called.
     */
    CREATED, /**
     * Indicates that onStart has been called.
     */
    STARTED, /**
     * Indicates that onResume has been called - activity is now visible to user.
     */
    RESUMED, /**
     * Indicates that onPause has been called - activity is no longer in the foreground.
     */
    PAUSED, /**
     * Indicates that onStop has been called - activity is no longer visible to the user.
     */
    STOPPED, /**
     * Indicates that onDestroy has been called - system is shutting down the activity.
     */
    DESTROYED
  }
}
