package org.danbrough.ampd;

import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.danbrough.ampd.mpd.MpdFile;
import org.danbrough.ampd.mpd.MpdListener;
import org.danbrough.ampd.mpd.MpdListenerAdapter;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class ButtonsFragment extends SherlockFragment {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(ButtonsFragment.class.getSimpleName());

  MpdService mpdService;
  View view;
  SeekBar seekProgress;
  TextView textProgress;
  int progress = 0;
  int progressSetAt = 0;
  Timer timer;
  boolean dragginSeek = false;

  MpdListener mpdListener = new MpdListenerAdapter() {

    @Override
    public void onChangedSong() {
      setSong(mpdService.getCurrentSong());
    }

    @Override
    public void onChangedPlayer() {
      view.post(new Runnable() {

        @Override
        public void run() {
          boolean playing = mpdService.getStatus().isPlaying();
          controlPlay.setImageResource(playing ? R.drawable.player_stop
              : R.drawable.player_play);
        }
      });
    }

    @Override
    public void onChangedProgress(final int secs, final int length) {
      log.trace("onChangedProgress: {}:{}", secs, length);
      view.post(new Runnable() {

        @Override
        public void run() {
          progress = secs;
          progressSetAt = (int) (System.currentTimeMillis() / 1000);
          seekProgress.setMax(length);
          seekProgress.setProgress(secs);

          MpdFile currentSong = mpdService.getCurrentSong();
          if (currentSong != null) {
            setProgressText(secs, currentSong.getTime());
          } else {
            textProgress.setText("");
          }
        }
      });
    }
  };

  ImageButton controlPrevious, controlPlay, controlNext;

  public ButtonsFragment() {
    super();
  }

  private static String formatTime(int time) {
    int hours = time / 3600;
    time -= (hours * 3600);
    int minutes = time / 60;
    int secs = time % 60;

    if (time > 3600)
      return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, secs);
    else
      return String.format(Locale.US, "%02d:%02d", minutes, secs);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    log.info("onCreateView()");

    view = inflater.inflate(R.layout.buttons, container, false);

    mpdService = ((Ampd) getActivity()).getMpdService();

    seekProgress = (SeekBar) view.findViewById(R.id.seekProgress);
    textProgress = (TextView) view.findViewById(R.id.textProgress);

    configureListeners();

    return view;
  }

  private void configureListeners() {
    controlPrevious = (ImageButton) view.findViewById(R.id.controlPrevious);
    controlPrevious.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        mpdService.cmdPrevious();
      }
    });
    controlPlay = (ImageButton) view.findViewById(R.id.controlPlay);
    controlPlay.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        if (mpdService.getStatus().isPlaying())
          mpdService.cmdStop();
        else
          mpdService.cmdPlay();
      }
    });
    view.findViewById(R.id.controlPause).setOnClickListener(
        new View.OnClickListener() {

          @Override
          public void onClick(View v) {
            mpdService.cmdPause();
          }
        });
    controlNext = (ImageButton) view.findViewById(R.id.controlNext);
    controlNext.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        mpdService.cmdNext();
      }
    });

    seekProgress.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        dragginSeek = false;
        MpdFile song = mpdService.getCurrentSong();

        if (song != null) {
          mpdService.seekTo(song.getPosition(), seekBar.getProgress());
          textProgress.setText(formatTime(seekBar.getProgress()));
          setProgressText(seekBar.getProgress(), song.getTime());
        }
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
        dragginSeek = true;
      }

      @Override
      public void onProgressChanged(SeekBar seekBar, int progress,
          boolean fromUser) {
        if (fromUser) {
          MpdFile song = mpdService.getCurrentSong();
          if (song != null)
            setProgressText(progress, song.getTime());
        }
      }
    });
  }

  private void setSong(MpdFile song) {
    log.debug("setSong() {}", song);
  }

  @Override
  public void onAttach(Activity activity) {
    log.warn("onAttach() {}", activity);
    super.onAttach(activity);
  }

  @Override
  public void onResume() {
    log.warn("onResume()");
    timer = new Timer();

    timer.scheduleAtFixedRate(new TimerTask() {

      @Override
      public void run() {
        if (mpdService.getStatus().isPlaying()) {

          view.post(new Runnable() {
            @Override
            public void run() {
              // log.trace("progressSetAt: " + progressSetAt);
              int now = (int) (System.currentTimeMillis() / 1000);

              if (!dragginSeek) {
                MpdFile song = mpdService.getCurrentSong();
                if (song != null) {
                  seekProgress.setProgress(now - progressSetAt + progress);
                  setProgressText(now - progressSetAt + progress,
                      song.getTime());
                } else {
                  seekProgress.setProgress(0);
                  textProgress.setText("");
                }
              }

            }
          });
        }
      }
    }, new Date(), 1000);

    setSong(mpdService.getCurrentSong());
    mpdService.addMpdListener(mpdListener);

    super.onResume();
  }

  private void setProgressText(int progress, int length) {
    textProgress.setText(Html.fromHtml(formatTime(progress) + " of<br/>"
        + formatTime(length)));
  }

  @Override
  public void onPause() {
    log.warn("onPause()");
    timer.cancel();
    timer = null;
    mpdService.removeMpdListener(mpdListener);
    super.onPause();
  }

}
