package com.welol.android.view.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import com.welol.android.R;
import com.welol.android.app.App;
import com.welol.android.app.permissions.Permission;
import com.welol.android.databinding.ActivityAskForPermissionBinding;
import com.welol.android.viewmodel.BaseViewModel;
import com.welol.android.viewmodel.viewinterface.BaseViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class AskForPermissionActivity extends
    BaseActivity<BaseViewInterface, BaseViewModel<BaseViewInterface>, ActivityAskForPermissionBinding> {
  public static final String EXTRA_PERMISSION = "permission";
  @BindView(R.id.rationaleText) TextView mRationaleText;
  private Permission mPermission;
  private Button mAskPermissionButton;

  /**
   * Asks for permission again.
   */
  @OnClick(R.id.askPermissionButton) public void askForPermission() {
    Log.d(TAG, "Asking for " + mPermission.name() + " again.");
    App.getPermissionsManager().requestIfNeeded(AskForPermissionActivity.this, mPermission);
  }

  @Nullable @Override public ViewModelBindingConfig getViewModelBindingConfig() {
    return new ViewModelBindingConfig(R.layout.activity_ask_for_permission, this);
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    // Should not authenticate when asking for device permissions.
    super.onCreate(savedInstanceState);
    mAskPermissionButton = findViewById(R.id.askPermissionButton);
  }

  @Override public void onResume() {
    super.onResume();
    // Obtaining the permission.
    Bundle extras = getIntent().getExtras();
    if (extras != null) {
      mPermission = (Permission) extras.get(EXTRA_PERMISSION);
    }
    if (mPermission == null) {
      throw new AssertionError("Permission must be set, but is null.");
    }
    // Displays correct rationale
    mRationaleText.setText(mPermission.getRationaleText());
    // Ensure the button is revealed.
    mAskPermissionButton.bringToFront();
    // Check if permission is granted, and if so, finishes activity.
    if (App.getPermissionsManager().isPermissionGranted(mPermission)) {
      finish();
    }
  }

  /**
   * Overridden to invoke {@link #finishActivity(int)}
   */
  @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    boolean allPermissionsGranted = true;
    for (int grantResult : grantResults) {
      if (grantResult != PERMISSION_GRANTED) allPermissionsGranted = false;
    }
    if (allPermissionsGranted) {
      finish();
    }
  }
}

