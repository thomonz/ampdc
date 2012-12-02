package org.danbrough.ampd;

import org.danbrough.ampd.covers.CoverManager;
import org.danbrough.ampd.covers.CoverService;
import org.danbrough.ampd.lyrics.LyricsManager;
import org.danbrough.ampd.lyrics.LyricsService;
import org.danbrough.ampd.mpd.MpdFile;
import org.danbrough.ampd.mpd.MpdListener;
import org.danbrough.ampd.mpd.MpdListenerAdapter;
import org.danbrough.ampd.mpd.MpdServiceImpl;
import org.danbrough.job.JobQueue;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;

public class MainActivity extends SherlockFragmentActivity implements Ampd {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(MainActivity.class.getSimpleName());

  private MpdServiceImpl mpd;
  private CoverService coverService;
  private LyricsService lyricsService;
  private JobQueue<Runnable> jobQueue;

  TextView textTitle = null, textSubTitle = null;

  android.support.v4.app.Fragment currentFragment;

  AndroidUtil util;

  MpdListener listener = new MpdListenerAdapter() {

    @Override
    public void onConnect() {
      runOnUiThread(new Runnable() {

        @Override
        public void run() {
          log.error("onConndect() {}", mpd.getDatabase().getServer());
          openView(mpd.getDatabase().getLastView());
        }
      });

    }

    @Override
    public void onDisconnect() {
      log.info("onDisonnect() {}", mpd.getDatabase().getServer());
    }

    @Override
    public void onChangedSong() {
      final MpdFile currentSong = mpd.getCurrentSong();
      if (currentSong != null) {
        if (textTitle != null) {
          runOnUiThread(new Runnable() {

            @Override
            public void run() {
              textTitle.setText(currentSong.getTitle());
              if (textSubTitle != null) {
                textSubTitle.setText("from " + currentSong.getAlbum() + " by "
                    + currentSong.getArtist());
              }
            }
          });
        }
      }
    }
  };

  @Override
  public void onCreate(Bundle savedInstanceState) {
    log.info("onCreate()");

    super.onCreate(savedInstanceState);
    util = AndroidUtil.getInstance(this);

    log.error("screen layout: " + util.getScreenLayout());

    mpd = new MpdServiceImpl(new AmpdDatabase(getApplicationContext()));

    jobQueue = new JobQueue<Runnable>();
    coverService = new CoverManager(this, jobQueue);
    lyricsService = new LyricsManager(this, jobQueue);

    setContentView(R.layout.main);

    textTitle = (TextView) findViewById(R.id.textTitle);
    textSubTitle = (TextView) findViewById(R.id.textSubTitle);

    //
    // ActionBar actionBar = getSupportActionBar();
  }

  @Override
  protected void onStart() {
    mpd.addMpdListener(listener);

    super.onStart();
  }

  @Override
  protected void onStop() {
    mpd.removeMpdListener(listener);
    super.onStop();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {

    MenuItem playlistMenu = menu.add(R.string.playlist);
    playlistMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    playlistMenu.setIcon(R.drawable.playlist);
    playlistMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {

      @Override
      public boolean onMenuItemClick(MenuItem item) {
        openView(MpdView.PLAYLIST);
        return true;
      }
    });

    MenuItem infoMenu = menu.add(R.string.info);
    infoMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    infoMenu.setIcon(R.drawable.info);
    infoMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {

      @Override
      public boolean onMenuItemClick(MenuItem item) {
        openView(MpdView.INFO);
        return true;
      }
    });

    MenuItem libraryMenu = menu.add(R.string.library);
    libraryMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    libraryMenu.setIcon(R.drawable.library);
    libraryMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {

      @Override
      public boolean onMenuItemClick(MenuItem item) {
        openView(MpdView.LIBRARY);
        return true;
      }
    });

    MenuItem controlsMenu = menu.add(R.string.controls);
    controlsMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    controlsMenu.setIcon(R.drawable.controls);
    controlsMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {

      @Override
      public boolean onMenuItemClick(MenuItem item) {
        openView(MpdView.CONTROLS);
        return true;
      }
    });

    MenuItem serverMenu = menu.add(R.string.server);
    serverMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    serverMenu.setIcon(R.drawable.server);
    serverMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {

      @Override
      public boolean onMenuItemClick(MenuItem item) {
        openView(MpdView.SERVERS);
        return true;
      }
    });

    MenuItem prefsMenu = menu.add(R.string.settings);
    prefsMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    prefsMenu.setIcon(R.drawable.settings);
    prefsMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {

      @Override
      public boolean onMenuItemClick(MenuItem item) {
        showSettings();
        return true;
      }
    });
    return true;
  }

  private void showFragment(android.support.v4.app.Fragment fragment) {
    log.info("showFragment() {}", fragment);
    this.currentFragment = fragment;
    android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager()
        .beginTransaction();
    ft.replace(R.id.fragmentContainer, fragment).addToBackStack(null).commit();
  }

  @Override
  protected void onPause() {
    log.info("onPause()");
    super.onPause();

    coverService.stop();
    lyricsService.stop();
    jobQueue.stop();
    mpd.stop();
  }

  @Override
  protected void onResume() {
    log.info("onResume()");
    super.onResume();
    jobQueue.start();
    lyricsService.start();
    coverService.start();
    mpd.start();

    // SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
    // mpdView = MpdView.valueOf(prefs.getString("mpdView",
    // MpdView.PLAYLIST.name()));
    //
    // if (mpdView == MpdView.LIBRARY) {
    // showLibrary();
    // } else if (mpdView == MpdView.PLAYLIST) {
    // showPlaylist();
    // } else if (mpdView == MpdView.INFO) {
    // showInfo();
    // } else if (mpdView == MpdView.CONTROLS) {
    // showControls();
    // }

    MpdView view = mpd.getDatabase().getLastView();
    log.error("LAST VEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEIEW: " + view);

  }

  protected void showSettings() {
    log.info("showSettings()");
    Intent intent = new Intent(MainActivity.this, PreferencesActivity.class);
    startActivityForResult(intent, 0);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    log.error("onActivityResult() requestCode: " + requestCode
        + " resultCode: " + resultCode + " data: " + data);
    if (requestCode == 0) {
      log.error("onActivityResult: server: " + mpd.getDatabase().getServer());
      mpd.getDatabase().load();

      log.error("onActivityResult: server: " + mpd.getDatabase().getServer());
    }
  }

  @Override
  public MpdService getMpdService() {
    return mpd;
  }

  @Override
  public CoverService getCoverService() {
    return coverService;
  }

  @Override
  public LyricsService getLyricsService() {
    return lyricsService;
  }

  @Override
  public void openView(MpdView view) {
    log.info("openView() {}", view);
    mpd.getDatabase().setLastView(view);

    if (view == MpdView.INFO) {
      showFragment(new InfoFragment());
    } else if (view == MpdView.CONTROLS) {
      showFragment(new ControlsFragment());
    } else if (view == MpdView.SERVERS) {
      showFragment(new ServersFragment());
    } else if (view == MpdView.PLAYLIST) {
      showFragment(new PlaylistFragment());
    } else if (view == MpdView.LIBRARY) {
      showFragment(new LibraryFragment());
    } else {
      log.error("dont know how to openView: " + view);
    }
  }
}
