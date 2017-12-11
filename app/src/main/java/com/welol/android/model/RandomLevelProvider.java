package com.welol.android.model;

import com.welol.android.R;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Proudly created by ohad on 05/12/2017 for TrueThat.
 */

public class RandomLevelProvider implements LevelsProvider {
  private static final List<Level> LEVELS =
      Arrays.asList(new Level(R.raw.level0), new Level(R.raw.level1), new Level(R.raw.level2),
          new Level(R.raw.level3), new Level(R.raw.level4), new Level(R.raw.level5),
          new Level(R.raw.level6), new Level(R.raw.level7), new Level(R.raw.level8),
          new Level(R.raw.level9), new Level(R.raw.level10), new Level(R.raw.level11),
          new Level(R.raw.level12), new Level(R.raw.level13), new Level(R.raw.level14),
          new Level(R.raw.level15), new Level(R.raw.level16), new Level(R.raw.level17),
          new Level(R.raw.level18), new Level(R.raw.level19), new Level(R.raw.level20));

  private static <T> List<T> pickRandom(List<T> original) {
    List<T> copy = new LinkedList<>(original);
    Collections.shuffle(copy);
    return copy.subList(0, 3);
  }

  @Override public List<Level> provide() {
    return pickRandom(LEVELS);
  }
}
