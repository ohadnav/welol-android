package com.welol.android.common;

import android.support.test.rule.ActivityTestRule;
import com.welol.android.app.App;
import com.welol.android.app.permissions.FakePermissionsManager;
import com.welol.android.empathy.FakeReactionDetectionManager;
import com.welol.android.view.activity.LaunchActivity;
import com.welol.android.view.activity.TestActivity;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.junit.Before;
import org.junit.Rule;

/**
 * Proudly created by ohad on 15/06/2017 for TrueThat.
 * <p>
 * Base testing suite for instrumentation testing. Initializes mock application modules, and more.
 */

@SuppressWarnings({ "FieldCanBeLocal", "WeakerAccess" }) public class BaseInstrumentationTestSuite {
  /**
   * Default duration to wait for. When waiting for activities to change for example.
   */
  public static Duration TIMEOUT =
      TestUtil.isDebugging() ? Duration.ONE_MINUTE : Duration.TWO_SECONDS;
  @Rule public ActivityTestRule<LaunchActivity> mMainActivityRule =
      new ActivityTestRule<>(LaunchActivity.class, true, false);
  @Rule public ActivityTestRule<TestActivity> mTestActivityRule =
      new ActivityTestRule<>(TestActivity.class, true, false);
  protected FakePermissionsManager mFakePermissionsManager;
  protected FakeReactionDetectionManager mFakeReactionDetectionManager;
  protected String TAG = this.getClass().getSimpleName();

  @Before public void setUp() throws Exception {
    // Initialize Awaitility
    Awaitility.reset();
    Awaitility.setDefaultTimeout(TIMEOUT);
    // Set up mocks
    mFakePermissionsManager = new FakePermissionsManager();
    App.setPermissionsManager(mFakePermissionsManager);
    mFakeReactionDetectionManager = new FakeReactionDetectionManager();
    App.setReactionDetectionManager(mFakeReactionDetectionManager);
    // Launches activity
    mTestActivityRule.launchActivity(null);
  }
}
