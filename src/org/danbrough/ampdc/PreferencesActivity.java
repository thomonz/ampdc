package org.danbrough.ampdc;

import org.danbrough.ampdc.mpd.MpdServiceImpl;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

public class PreferencesActivity extends SherlockPreferenceActivity {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(PreferencesActivity.class.getSimpleName());

  MpdService mpd;

  @Override
  protected void onCreate(android.os.Bundle savedInstanceState) {
    log.info("onCreate()");
    super.onCreate(savedInstanceState);
    mpd = MpdServiceImpl.getInstance();
    addPreferencesFromResource(R.xml.preferences);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

}
