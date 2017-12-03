package com.welol.android.app.permissions;

import com.welol.android.R;
import com.welol.android.common.BaseInstrumentationTestSuite;
import com.welol.android.view.activity.AskForPermissionActivity;
import com.welol.android.view.activity.TestActivity;
import org.junit.Before;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.welol.android.common.TestUtil.waitForActivity;
import static org.hamcrest.core.AllOf.allOf;

/**
 * Proudly created by ohad on 13/06/2017 for TrueThat.
 */
public class AskForPermissionActivityTest extends BaseInstrumentationTestSuite {
  private static final Permission PERMISSION = Permission.CAMERA;

  @Before public void setUp() throws Exception {
    super.setUp();
    // Revoke permission on launch.
    mFakePermissionsManager.forbid(PERMISSION);
  }

  @Test public void onRequestPermissionsFailed() throws Exception {
    mTestActivityRule.getActivity().onPermissionRejected(PERMISSION);
    // Wait until we navigate to ask for permission activity.
    waitForActivity(AskForPermissionActivity.class);
    // Assert that no camera permission fragment is displayed.
    onView(withId(R.id.rationaleText)).check(
        matches(allOf(isDisplayed(), withText(R.string.camera_permission_rationale))));
  }

  @Test public void finishIfPermissionIsAlreadyGranted() throws Exception {
    mFakePermissionsManager.grant(PERMISSION);
    mTestActivityRule.getActivity().onPermissionRejected(PERMISSION);
    // Wait for possible navigation out of test activity, and assert the current activity remains test activity.
    waitForActivity(TestActivity.class);
  }

  @Test public void finishAfterPermissionGranted() throws Exception {
    // Invoke request callback, to finish activity.
    mFakePermissionsManager.invokeRequestCallback();
    mTestActivityRule.getActivity().onPermissionRejected(PERMISSION);
    // Wait until we navigate to ask for permission activity.
    waitForActivity(AskForPermissionActivity.class);
    // Grant permission, to mock the scenario where the user allowed the permission.
    mFakePermissionsManager.reset(PERMISSION);
    // Ask for permission again.
    onView(withId(R.id.askPermissionButton)).perform(click());
    // Wait until we return to test activity.
    waitForActivity(TestActivity.class);
  }

  @Test public void askAgainAndDenyDoesNotFinish() throws Exception {
    mTestActivityRule.getActivity().onPermissionRejected(PERMISSION);
    // Wait until we navigate to ask for permission activity.
    waitForActivity(AskForPermissionActivity.class);
    // Ask for permission again.
    onView(withId(R.id.askPermissionButton)).perform(click());
    // Wait for possible navigation back to test activity, and assert no navigation was performed.
    Thread.sleep(100);
    waitForActivity(AskForPermissionActivity.class);
  }
}