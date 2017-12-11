package com.welol.android.viewmodel.viewinterface;

import com.welol.android.model.Level;
import com.welol.android.model.LevelsProvider;
import com.welol.android.view.activity.LevelActivity;

/**
 * Proudly created by ohad on 03/12/2017 for TrueThat.
 */

public interface MainViewInterface extends BaseViewInterface {
  /**
   * Starts {@link LevelActivity} with {@code level}. Good luck ;)
   *
   * @param level to play.
   */
  void playLevel(Level level);

  /**
   * @return levels provider to provide levels to play.
   */
  LevelsProvider getLevelsProvider();

  /**
   * Share this amazing piece of art app.
   */
  void share();
}