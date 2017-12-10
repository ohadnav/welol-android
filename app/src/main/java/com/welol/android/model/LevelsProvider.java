package com.welol.android.model;

import java.util.List;

/**
 * Proudly created by ohad on 05/12/2017 for TrueThat.
 */

public interface LevelsProvider {
  /**
   * @return provides levels for our happy users.
   */
  List<Level> provide();
}
