package com.welol.android.model;

import org.junit.Test;

import static com.welol.android.util.ParcelableTestUtil.testParcelability;

/**
 * Proudly created by ohad on 04/12/2017 for TrueThat.
 */
public class LevelInstrumentationTest {
  @Test public void testParcelable() throws Exception {
    testParcelability(new Level(1), Level.CREATOR);
    Level levelWithResult = new Level(1);
    levelWithResult.setResult(Level.Result.WIN);
    testParcelability(levelWithResult, Level.CREATOR);
  }
}