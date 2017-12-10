package com.welol.android.empathy;

import com.welol.android.model.Emotion;
import com.welol.android.viewmodel.viewinterface.BaseListener;

/**
 * Proudly created by ohad on 21/11/2017 for TrueThat.
 */
public interface ReactionDetectionListener extends BaseListener {
  /**
   * @param reaction detected on our user's pretty face.
   */
  void onReactionDetected(Emotion reaction);

  /**
   * Triggered when the user looks at the phone. Of course, this is limited to the detector ability
   * to actually detect the face.
   */
  void onAttention();

  /**
   * Triggered when the user has stopped looking at the phone, or when the detector lost his face.
   */
  void onAttentionLost();
}
