package com.welol.android.viewmodel;

import android.os.Bundle;
import android.support.annotation.Nullable;
import com.welol.android.app.App;
import com.welol.android.empathy.FakeReactionDetectionManager;
import com.welol.android.view.activity.BaseActivity;
import com.welol.android.viewmodel.viewinterface.BaseFragmentViewInterface;
import com.welol.android.viewmodel.viewinterface.BaseViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.junit.Before;

/**
 * Proudly created by ohad on 20/07/2017 for TrueThat.
 */

@SuppressWarnings("WeakerAccess") public class ViewModelTestSuite {
  public static final Duration DEFAULT_TIMEOUT = Duration.ONE_HUNDRED_MILLISECONDS;
  protected FakeReactionDetectionManager mFakeReactionDetectionManager;
  Date mNow;

  @SuppressWarnings("unchecked") @Before public void setUp() throws Exception {
    mNow = new Date();
    // Initialize Awaitility
    Awaitility.reset();
    Awaitility.setDefaultTimeout(DEFAULT_TIMEOUT);
    Awaitility.setDefaultPollDelay(new Duration(10, TimeUnit.MILLISECONDS));
    Awaitility.setDefaultPollInterval(new Duration(10, TimeUnit.MILLISECONDS));
    mFakeReactionDetectionManager = new FakeReactionDetectionManager();
    App.setReactionDetectionManager(mFakeReactionDetectionManager);
  }

  @SuppressWarnings("unchecked")
  <ViewInterface extends BaseViewInterface, ViewModel extends BaseViewModel<ViewInterface>> ViewModel createViewModel(
      Class<ViewModel> viewModelTypeClass, ViewInterface viewInterface,
      @Nullable Bundle savedInstanceState) throws Exception {
    ViewModel viewModel = viewModelTypeClass.newInstance();
    viewModel.onCreate(null, savedInstanceState);
    viewModel.onBindView(viewInterface);
    return viewModel;
  }

  @SuppressWarnings("unused") class UnitTestViewInterface implements BaseFragmentViewInterface {
    private String mToastText;
    private boolean mIsVisible = true;

    public String getToastText() {
      return mToastText;
    }

    @Override public void toast(String text) {
      mToastText = text;
    }

    @Override public BaseActivity getBaseActivity() {
      return null;
    }

    @Override public String getTAG() {
      return this.getClass().getSimpleName();
    }

    @Nullable @Override public ViewModelBindingConfig getViewModelBindingConfig() {
      return null;
    }

    @Override public void removeViewModel() {

    }
  }
}
