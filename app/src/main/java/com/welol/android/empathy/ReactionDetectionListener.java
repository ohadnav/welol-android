package com.welol.android.empathy;

import com.affectiva.android.affdex.sdk.detector.Detector;
import com.welol.android.model.Emotion;
import com.welol.android.viewmodel.viewinterface.BaseListener;

/**
 * Proudly created by ohad on 21/11/2017 for TrueThat.
 */
public interface ReactionDetectionListener extends Detector.FaceListener, BaseListener {
  /**
   * @param reaction detected on our user's pretty face.
   */
  void onReactionDetected(Emotion reaction);
}
