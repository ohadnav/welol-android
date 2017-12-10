package com.welol.android.empathy;

import com.affectiva.android.affdex.sdk.Frame;
import com.affectiva.android.affdex.sdk.detector.Detector;
import com.affectiva.android.affdex.sdk.detector.Face;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Proudly created by ohad on 04/12/2017 for TrueThat.
 */

public class AffectivaImageListener implements Detector.ImageListener {
  private static final double SUM_THRESHOLD = 100;
  private static final double REACTION_ITERATION_THRESHOLD = 40;
  private static final double ATTENTION_ITERATION_THRESHOLD = 70;
  private static final int LOST_ATTENTION_FRAMES_COUNT = 5;
  private String TAG = AffectivaImageListener.class.getSimpleName();
  private Map<AffectivaEmotion, Float> emotionToLikelihood;
  private BaseReactionDetectionManager mReactionDetectionManager;
  private int mFramesWithoutAttention = 0;

  AffectivaImageListener(BaseReactionDetectionManager reactionDetectionManager) {
    mReactionDetectionManager = reactionDetectionManager;
    resetLikelihoodMap();
  }

  @Override public void onImageResults(List<Face> faces, Frame frame, float v) {
    if (!faces.isEmpty()) {
      Face face = faces.get(0);
      // Calculates attention
      if (face.expressions.getAttention() > ATTENTION_ITERATION_THRESHOLD) {
        // If current frame has attention
        mReactionDetectionManager.onAttention();
        // Reset no attention frames count
        mFramesWithoutAttention = 0;
      } else {
        mFramesWithoutAttention++;
      }
      // Determines most likely reaction
      Map<AffectivaEmotion, Float> currentLikelihood;
      currentLikelihood = new HashMap<>();
      currentLikelihood.put(AffectivaEmotion.JOY, face.emotions.getJoy());
      //currentLikelihood.put(AffectivaEmotion.SURPRISE, face.emotions.getSurprise());
      //currentLikelihood.put(AffectivaEmotion.ANGER, face.emotions.getAnger());
      //// Fear is harder to detect, and so it is amplified
      //currentLikelihood.put(AffectivaEmotion.FEAR, face.emotions.getFear() * 3);
      //// Negative emotions are too easy to detect, and so they are decreased
      //currentLikelihood.put(AffectivaEmotion.DISGUST, face.emotions.getDisgust() / 2);
      //currentLikelihood.put(AffectivaEmotion.SADNESS, face.emotions.getSadness() / 2);
      for (Map.Entry<AffectivaEmotion, Float> likelihoodEntry : currentLikelihood.entrySet()) {
        if (likelihoodEntry.getValue() > REACTION_ITERATION_THRESHOLD) {
          emotionToLikelihood.put(likelihoodEntry.getKey(),
              emotionToLikelihood.get(likelihoodEntry.getKey()) + likelihoodEntry.getValue());
        }
      }
      boolean detected = false;
      for (Map.Entry<AffectivaEmotion, Float> emotionLikelihoodEntry : emotionToLikelihood.entrySet()) {
        if (emotionLikelihoodEntry.getValue() > SUM_THRESHOLD) {
          detected = true;
          mReactionDetectionManager.onReactionDetected(
              emotionLikelihoodEntry.getKey().getEmotion());
        }
      }
      if (detected) {
        resetLikelihoodMap();
      }
    } else {
      // No face in current frame, and so no attention.
      resetLikelihoodMap();
      mFramesWithoutAttention++;
    }
    if (mFramesWithoutAttention > LOST_ATTENTION_FRAMES_COUNT) {
      // If there enough frames without attention, then attention is lost.
      mReactionDetectionManager.onAttentionLost();
    }
  }

  private void resetLikelihoodMap() {
    emotionToLikelihood = new HashMap<>();
    emotionToLikelihood.put(AffectivaEmotion.JOY, 0F);
    emotionToLikelihood.put(AffectivaEmotion.SURPRISE, 0F);
    emotionToLikelihood.put(AffectivaEmotion.ANGER, 0F);
    emotionToLikelihood.put(AffectivaEmotion.FEAR, 0F);
    emotionToLikelihood.put(AffectivaEmotion.DISGUST, 0F);
    emotionToLikelihood.put(AffectivaEmotion.SADNESS, 0F);
  }
}
