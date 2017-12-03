package com.welol.android.view.activity;

import android.os.Bundle;
import com.crashlytics.android.Crashlytics;
import com.welol.android.R;
import com.welol.android.databinding.ActivityMainBinding;
import com.welol.android.viewmodel.BaseViewModel;
import com.welol.android.viewmodel.viewinterface.BaseViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;
import io.fabric.sdk.android.Fabric;

public class MainActivity
    extends BaseActivity<BaseViewInterface, BaseViewModel<BaseViewInterface>, ActivityMainBinding> {

  @Override public ViewModelBindingConfig getViewModelBindingConfig() {
    return new ViewModelBindingConfig(R.layout.activity_main, this);
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Fabric.with(this, new Crashlytics());
  }
}
