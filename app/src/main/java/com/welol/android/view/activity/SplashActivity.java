package com.welol.android.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Proudly created by ohad on 22/10/2017 for TrueThat.
 */

public class SplashActivity extends AppCompatActivity {
  /**
   * Logging tag.
   */
  String TAG = this.getClass().getSimpleName();

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Start main activity
    startActivity(new Intent(SplashActivity.this, LaunchActivity.class));
  }
}