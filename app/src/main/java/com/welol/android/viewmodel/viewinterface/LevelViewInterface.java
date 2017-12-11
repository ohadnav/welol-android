package com.welol.android.viewmodel.viewinterface;

import com.welol.android.model.Level;

/**
 * Proudly created by ohad on 04/12/2017 for TrueThat.
 */

public interface LevelViewInterface extends BaseViewInterface {

  /**
   * Resumes the level.
   */
  void play();

  /**
   * Replays the level's video.
   */
  void replay();

  /**
   * Pauses the level, usually due to loading or lost face by the detector.
   */
  void pause();

  /**
   * Finished the level activity.
   */
  void finishLevel(Level.Result result);
}
