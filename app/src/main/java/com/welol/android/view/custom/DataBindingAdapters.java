package com.welol.android.view.custom;

import android.databinding.BindingAdapter;
import android.support.annotation.ColorRes;
import android.support.v4.content.res.ResourcesCompat;
import android.widget.TextView;

/**
 * Proudly created by ohad on 23/07/2017 for TrueThat.
 */

public class DataBindingAdapters {

  // --------------------------- Colors ----------------------------------
  @BindingAdapter("android:textColor") public static void setTextColor(TextView textView,
      @ColorRes int colorResId) {
    textView.setTextColor(ResourcesCompat.getColor(textView.getResources(), colorResId,
        textView.getContext().getTheme()));
  }
}