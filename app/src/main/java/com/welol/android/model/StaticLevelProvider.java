package com.welol.android.model;

import com.welol.android.R;
import java.util.Arrays;
import java.util.List;

/**
 * Proudly created by ohad on 05/12/2017 for TrueThat.
 */

public class StaticLevelProvider implements LevelsProvider {
  @Override public List<Level> provide() {
    return Arrays.asList(new Level(R.raw.level0), new Level(R.raw.level1), new Level(R.raw.level2),
        new Level(R.raw.level3));
  }
}
