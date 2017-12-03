package com.welol.android.app.permissions;

import android.os.ParcelFileDescriptor;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.SearchCondition;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;
import android.support.v4.app.ActivityCompat;
import com.welol.android.common.TestUtil;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static com.welol.android.common.TestUtil.getCurrentActivity;

/**
 * Proudly created by ohad on 24/05/2017 for TrueThat.
 */

@SuppressWarnings("WeakerAccess") public class PermissionsTestUtil {
  static final BySelector ALLOW_SELECTOR =
      By.res(TestUtil.INSTALLER_PACKAGE_NAME, "permission_allow_button");
  static final SearchCondition<UiObject2> ALLOW_SEARCH_CONDITION = Until.findObject(ALLOW_SELECTOR);
  static final SearchCondition<UiObject2> DENY_SEARCH_CONDITION =
      Until.findObject(By.res(TestUtil.INSTALLER_PACKAGE_NAME, "permission_deny_button"));
  private static final int SLEEP_TIME = 1000;

  public static void revokeAllPermissions() throws Exception {
    ParcelFileDescriptor res =
        getInstrumentation().getUiAutomation().executeShellCommand("pm reset-permissions");
    res.close();
    Thread.sleep(SLEEP_TIME);
  }

  public static void revokePermission(Permission permission) throws Exception {
    ParcelFileDescriptor res = getInstrumentation().getUiAutomation()
        .executeShellCommand(
            "pm revoke " + TestUtil.APPLICATION_PACKAGE_NAME + " " + permission.getManifest());
    res.close();
    Thread.sleep(SLEEP_TIME);
  }

  public static void grantPermission(Permission permission) throws Exception {
    ParcelFileDescriptor res = getInstrumentation().getUiAutomation()
        .executeShellCommand(
            "pm grant " + TestUtil.APPLICATION_PACKAGE_NAME + " " + permission.getManifest());
    res.close();
    // Attempts to hit the allow button.
    ActivityCompat.requestPermissions(getCurrentActivity(),
        new String[] { permission.getManifest() }, permission.getRequestCode());
    UiObject2 allowButton = UiDevice.getInstance(getInstrumentation())
        .wait(PermissionsTestUtil.ALLOW_SEARCH_CONDITION, 100);
    MatcherAssert.assertThat(allowButton.isEnabled(), Is.is(true));
    allowButton.click();

    Thread.sleep(SLEEP_TIME);
  }
}
