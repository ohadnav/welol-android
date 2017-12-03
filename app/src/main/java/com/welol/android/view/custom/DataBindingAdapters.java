package com.welol.android.view.custom;

import android.content.res.ColorStateList;
import android.databinding.BindingAdapter;
import android.support.annotation.ColorRes;
import android.support.v4.content.res.ResourcesCompat;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Proudly created by ohad on 23/07/2017 for TrueThat.
 */

public class DataBindingAdapters {
  // --------------------------- ImageView -----------------------------
  @BindingAdapter("android:src") public static void setImageResource(ImageView imageView,
      int resource) {
    imageView.setImageResource(resource);
  }

  // --------------------------- EditText -----------------------------

  /**
   * @param inputType taken from {@link InputType}.
   */
  @BindingAdapter("android:inputType") public static void setInputType(EditText view,
      int inputType) {
    view.setInputType(inputType);
  }

  // --------------------------- Colors ----------------------------------
  @BindingAdapter("android:textColor") public static void setTextColor(TextView textView,
      @ColorRes int colorResId) {
    textView.setTextColor(ResourcesCompat.getColor(textView.getResources(), colorResId,
        textView.getContext().getTheme()));
  }

  @BindingAdapter("android:backgroundTint")
  public static void setBackgroundTint(View view, @ColorRes int colorResId) {
    view.setBackgroundTintList(ColorStateList.valueOf(
        view.getResources().getColor(colorResId, view.getContext().getTheme())));
  }
}