package com.welol.android.viewmodel.viewinterface;

import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.widget.Toast;
import com.welol.android.view.activity.BaseActivity;
import eu.inloop.viewmodel.IView;

/**
 * Proudly created by ohad on 20/07/2017 for TrueThat.
 */

public interface BaseViewInterface extends IView {
  /**
   * Displays a {@link Toast}.
   *
   * @param text to show within the toast.
   */
  void toast(String text);

  /**
   * @param stringResourceId to show in {@link Snackbar}
   * @param duration of the snackbar.
   */
  void snackbar(@StringRes int stringResourceId, int duration);

  /**
   * Hides the snackbar, if visible.
   */
  void hideSnackbar();

  /**
   * @return the activity associated with this view model.
   */
  BaseActivity getBaseActivity();

  /**
   * @return the view TAG to be used for logging.
   */
  String getTAG();
}
