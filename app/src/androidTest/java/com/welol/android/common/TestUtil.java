package com.welol.android.common;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.support.test.espresso.PerformException;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.core.deps.guava.collect.Iterables;
import android.support.test.espresso.util.HumanReadables;
import android.support.test.espresso.util.TreeIterables;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import com.welol.android.R;
import com.welol.android.view.activity.BaseActivity;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import org.awaitility.Duration;
import org.awaitility.core.ThrowingRunnable;
import org.hamcrest.Matcher;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.welol.android.common.BaseInstrumentationTestSuite.TIMEOUT;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;

/**
 * Proudly created by ohad on 24/05/2017 for TrueThat.
 */
public class TestUtil {
  public static final String APPLICATION_PACKAGE_NAME = "com.welol.android.debug";
  public static final String INSTALLER_PACKAGE_NAME = "com.android.packageinstaller";

  /**
   * @param activityClass of {@link AppCompatActivity} to wait for to be displayed.
   */
  public static void waitForActivity(final Class<? extends AppCompatActivity> activityClass,
      Duration duration) {
    if (getCurrentActivity() == null) {
      throw new AssertionError("App has not started. Device locked?");
    }
    await().atMost(duration).untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(activityClass.getSimpleName(),
            getCurrentActivity().getClass().getSimpleName());
      }
    });
  }

  public static void waitForActivity(final Class<? extends AppCompatActivity> activityClass) {
    waitForActivity(activityClass, TIMEOUT);
  }

  public static void waitForBaseDialog() {
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        onView(withId(R.id.dialog_button)).check(matches(isDisplayed()));
      }
    });
  }

  public static void waitForDialogHidden() {
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        double random = Math.random();
        try {
          onView(withId(R.id.dialog_button)).check(matches(isDisplayed()));
          throw new Exception("" + random);
        } catch (Exception e) {
          if (Objects.equals(e.getMessage(), "" + random)) {
            throw new Exception("dialog is not hidden");
          }
        }
      }
    });
  }

  /**
   * Attempts to find the {@link View} described by {@code viewMatcher} for at
   * most {@link BaseInstrumentationTestSuite#TIMEOUT}.
   *
   * @param viewMatcher for find a specific view.
   */
  public static void waitMatcher(final Matcher viewMatcher) {
    onView(isRoot()).perform(new ViewAction() {
      @Override public Matcher<View> getConstraints() {
        return isRoot();
      }

      @Override public String getDescription() {
        return "waited for to for a specific view with matcher <"
            + viewMatcher
            + "> during "
            + TIMEOUT
            + ".";
      }

      @Override public void perform(final UiController uiController, final View view) {
        uiController.loopMainThreadUntilIdle();
        final long startTime = System.currentTimeMillis();
        final long endTime = startTime + TIMEOUT.getValueInMS();

        do {
          for (View child : TreeIterables.breadthFirstViewTraversal(view)) {
            // found view that satisfies viewMatcher
            if (viewMatcher.matches(child)) {
              return;
            }
          }

          uiController.loopMainThreadForAtLeast(50);
        } while (System.currentTimeMillis() < endTime);

        // timeout happens
        throw new PerformException.Builder().withActionDescription(this.getDescription())
            .withViewDescription(HumanReadables.describe(view))
            .withCause(new TimeoutException())
            .build();
      }
    });
  }

  /**
   * @return current foreground activity.
   */
  public static AppCompatActivity getCurrentActivity() {
    try {
      getInstrumentation().waitForIdleSync();
      final BaseActivity[] activity = new BaseActivity[1];
      getInstrumentation().runOnMainSync(new Runnable() {
        @Override public void run() {
          activity[0] = getCurrentActivityIgnoreThread();
        }
      });
      return activity[0];
    } catch (RuntimeException threadException) {
      return getCurrentActivityIgnoreThread();
    }
  }

  private static BaseActivity getCurrentActivityIgnoreThread() {
    java.util.Collection<Activity> activities =
        ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
    try {
      return (BaseActivity) Iterables.getOnlyElement(activities);
    } catch (Exception ignored) {
      return null;
    }
  }

  static boolean isDebugging() {
    return android.os.Debug.isDebuggerConnected() || 0 != (
        getInstrumentation().getContext().getApplicationInfo().flags &=
            ApplicationInfo.FLAG_DEBUGGABLE);
  }
}