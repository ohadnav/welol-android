package com.welol.android.app.permissions;

import android.content.pm.PackageManager;
import android.support.test.filters.MediumTest;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;
import android.support.v4.app.ActivityCompat;
import com.welol.android.app.App;
import com.welol.android.common.BaseInstrumentationTestSuite;
import com.welol.android.common.TestUtil;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 24/05/2017 for TrueThat.
 */
@RunWith(AndroidJUnit4.class) @MediumTest @Ignore
// Test fails since shell commands take time to take effect.
public class DevicePermissionsManagerTest extends BaseInstrumentationTestSuite {
  private static final Permission PERMISSION = Permission.CAMERA;
  private PermissionsManager mPermissionsManager;
  private UiDevice mDevice;

  @Before public void setUp() throws Exception {
    super.setUp();
    // Initialize UiDevice instance
    mDevice = UiDevice.getInstance(getInstrumentation());
    // Resets all permissions
    PermissionsTestUtil.revokeAllPermissions();// Set up real device permission module.
    App.setPermissionsManager(
        mPermissionsManager = new DevicePermissionsManager(mTestActivityRule.getActivity()));
  }

  @Test public void isPermissionGranted_shouldBeGranted() throws Exception {
    // Grant permission via shell
    PermissionsTestUtil.grantPermission(PERMISSION);
    // Assert permission check is correct
    assertTrue(mPermissionsManager.isPermissionGranted(PERMISSION));
  }

  @Test public void isPermissionGranted_shouldNotBeGranted() throws Exception {
    // Revokes permission via shell
    PermissionsTestUtil.revokePermission(PERMISSION);
    // Assert permission check is correct
    assertFalse(mPermissionsManager.isPermissionGranted(PERMISSION));
  }

  @Test public void requestIfNeeded_shouldRequest() throws Exception {
    // Revokes permission via shell
    PermissionsTestUtil.revokePermission(PERMISSION);
    mPermissionsManager.requestIfNeeded(mTestActivityRule.getActivity(), PERMISSION);
    // Assert permission dialogue is prompted
    UiObject2 denyButton = mDevice.wait(PermissionsTestUtil.DENY_SEARCH_CONDITION, 100);
    MatcherAssert.assertThat(denyButton.isEnabled(), Is.is(true));
    // Deny permission
    denyButton.click();
    // Wait for the click to register
    mDevice.wait(Until.hasObject(By.pkg(TestUtil.APPLICATION_PACKAGE_NAME).depth(0)), 100);
    // Assert that permissions wasn't granted
    assertNotEquals(PackageManager.PERMISSION_GRANTED,
        ActivityCompat.checkSelfPermission(mTestActivityRule.getActivity(),
            PERMISSION.getManifest()));
  }

  @Test public void requestIfNeeded_alreadyHadPermission() throws Exception {
    // Revokes permission via shell
    PermissionsTestUtil.grantPermission(PERMISSION);
    mPermissionsManager.requestIfNeeded(mTestActivityRule.getActivity(), PERMISSION);
    // Assert permission dialogue is not prompted
    assertFalse(mDevice.hasObject(PermissionsTestUtil.ALLOW_SELECTOR));
    // Assert that permissions was granted
    assertEquals(PackageManager.PERMISSION_GRANTED,
        ActivityCompat.checkSelfPermission(mTestActivityRule.getActivity(),
            PERMISSION.getManifest()));
  }
}