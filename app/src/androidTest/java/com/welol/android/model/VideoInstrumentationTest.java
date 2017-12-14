package com.welol.android.model;

import org.junit.Test;

import static com.welol.android.util.ParcelableTestUtil.testParcelability;

/**
 * Proudly created by ohad on 04/12/2017 for TrueThat.
 */
public class VideoInstrumentationTest {
  @Test public void testParcelable() throws Exception {
    testParcelability(new Video("1", "2", 3), Level.CREATOR);
  }
}