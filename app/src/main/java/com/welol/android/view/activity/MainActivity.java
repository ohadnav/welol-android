package com.welol.android.view.activity;

import android.content.Intent;
import android.os.Bundle;
import com.crashlytics.android.Crashlytics;
import com.welol.android.R;
import com.welol.android.databinding.ActivityMainBinding;
import com.welol.android.model.Level;
import com.welol.android.model.LevelsProvider;
import com.welol.android.model.RandomLevelProvider;
import com.welol.android.util.RequestCodes;
import com.welol.android.viewmodel.MainViewModel;
import com.welol.android.viewmodel.viewinterface.MainViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;
import io.fabric.sdk.android.Fabric;

public class MainActivity
    extends BaseActivity<MainViewInterface, MainViewModel, ActivityMainBinding>
    implements MainViewInterface {

  private LevelsProvider mLevelsProvider;
  @Override public ViewModelBindingConfig getViewModelBindingConfig() {
    return new ViewModelBindingConfig(R.layout.activity_main, this);
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Fabric.with(this, new Crashlytics());
    mLevelsProvider = new RandomLevelProvider();
  }

  @Override public void playLevel(Level level) {
    Intent levelIntent = new Intent(this, LevelActivity.class);
    levelIntent.putExtra(LevelActivity.INTENT_LEVEL, level);
    startActivityForResult(levelIntent, RequestCodes.PLAY_LEVEL);
  }

  @Override public LevelsProvider getLevelsProvider() {
    return mLevelsProvider;
  }

  @Override public void share() {
    Intent shareIntent = new Intent(Intent.ACTION_SEND);
    shareIntent.setType("text/plain");
    shareIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share_text));
    startActivity(Intent.createChooser(shareIntent, "Share app using"));
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == RequestCodes.PLAY_LEVEL) {
      Level.Result result = (Level.Result) data.getSerializableExtra(LevelActivity.INTENT_RESULT);
      getViewModel().onLevelFinished(result);
    }
  }
}
