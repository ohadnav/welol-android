package com.welol.android.view.activity;

import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import com.welol.android.R;
import com.welol.android.databinding.ActivityTestBinding;
import com.welol.android.viewmodel.BaseViewModel;
import com.welol.android.viewmodel.viewinterface.BaseViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;

@VisibleForTesting public class TestActivity
    extends BaseActivity<BaseViewInterface, BaseViewModel<BaseViewInterface>, ActivityTestBinding> {

  @Nullable @Override public ViewModelBindingConfig getViewModelBindingConfig() {
    return new ViewModelBindingConfig(R.layout.activity_test, this);
  }
}
