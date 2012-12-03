package org.danbrough.ampdc;

import java.util.Timer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.danbrough.ampd.covers.CoverService;
import org.danbrough.ampd.lyrics.LyricsService;
import org.danbrough.ampdc.mpd.MpdFile;
import org.danbrough.ampdc.mpd.MpdListener;
import org.danbrough.ampdc.mpd.MpdListenerAdapter;
import org.danbrough.job.JobQueue.JobCallback;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class InfoFragment extends SherlockFragment {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(InfoFragment.class.getSimpleName());

  MpdService mpdService;
  CoverService coverService;
  LyricsService lyricsService;
  View view;
  ImageView imageCover;
  TextView textLyrics;
  ScrollView scrollLyrics;
  int scrollInterval = 5000;
  int scrollRate = 0;
  Timer timer;
  MpdFile file;
  MpdListener mpdListener = new MpdListenerAdapter() {
    @Override
    public void onChangedSong() {
      setMpdFile(mpdService.getCurrentSong());
    }
  };

  private void setMpdFile(MpdFile newFile) {

    if (newFile == null) {
      if (this.file != null) {
        view.post(new Runnable() {

          @Override
          public void run() {
            imageCover.setVisibility(View.GONE);
          }
        });
      }
      this.file = null;
      return;
    }

    lyricsService.getLyrics(newFile, new JobCallback<String>() {

      @Override
      public void onError(Exception e) {
        log.error(e.getMessage(), e);
        getActivity().runOnUiThread(new Runnable() {
          @Override
          public void run() {
            textLyrics.setText("");

          }
        });

      }

      @Override
      public void onComplete(final String lyrics) {

        getActivity().runOnUiThread(new Runnable() {

          @Override
          public void run() {
            textLyrics.setText(convertLyrics(lyrics));
            if (timer == null) {
              scrollLyrics.scrollTo(0, 0);
              log.warn("scrollLyricsHeight: " + scrollLyrics.getHeight()
                  + " songLength: " + mpdService.getCurrentSong().getTime());
              // timer = new Timer();
              // timer.schedule(new TimerTask() {
              //
              // @Override
              // public void run() {
              //
              // getActivity().runOnUiThread(new Runnable() {
              //
              // @Override
              // public void run() {
              // scrollLyrics();
              // }
              // });
              // }
              // }, 0, 5000);
            }
          }
        });
      }
    });

    if (this.file == null
        || (!this.file.getAlbum().equals(newFile.getAlbum()) || !this.file
            .getArtist().equals(newFile.getArtist()))) {
      this.file = newFile;
      log.info("looking for cover for {}:{}", file.getArtist(), file.getAlbum());

      view.post(new Runnable() {
        @Override
        public void run() {
          imageCover.setVisibility(View.INVISIBLE);

          coverService.getCover(file, new JobCallback<Uri>() {

            @Override
            public void onComplete(final Uri result) {
              log.trace("found cover :{}", result);
              if (!isResumed()) {
                log.error("NOT RESUMED");
                return;
              }
              getView().post(new Runnable() {

                @Override
                public void run() {
                  imageCover.setVisibility(View.VISIBLE);
                  imageCover.setImageURI(result);
                }
              });

            }

            @Override
            public void onError(Exception e) {
              if (!isResumed()) {
                log.error("NOT RESUMED");
                return;
              }
              getView().post(new Runnable() {

                @Override
                public void run() {
                  imageCover.setVisibility(View.GONE);
                }
              });
              log.error(e.getMessage(), e);
            }
          });
        }
      });

    }
  }

  private String convertLyrics(String lyrics) {
    lyrics = lyrics.replaceAll("<p>", "").replaceAll("</p>", "")
        .replaceAll("<br/>", "\n").replaceAll("&quot;", "\"")
        .replaceAll("&acute;", "'").replaceAll("&gt;", ">")
        .replaceAll("&lt;", "<");

    String regex = "&#\\d*;";
    Pattern p = Pattern.compile(regex);
    Matcher matcher = p.matcher(lyrics);

    while (matcher.find()) {
      String field = lyrics.substring(matcher.start() + 2, matcher.end() - 1);
      char c = (char) Integer.parseInt(field);
      lyrics = lyrics.substring(0, matcher.start()) + c
          + lyrics.substring(matcher.end());
      matcher = p.matcher(lyrics);
    }
    return lyrics;
  }

  // private void scrollLyrics() {
  //
  // int progress = mpdService.getProgress();
  //
  // int maxScroll = textLyrics.getHeight() - scrollLyrics.getHeight();
  //
  // int songLength = mpdService.getCurrentSong().getTime();
  // log.trace("maxScroll: " + maxScroll + " " + progress + ":" + songLength);
  // if (songLength != 0) {
  // int scrollY = (int) (maxScroll * ((float) progress / songLength));
  // scrollLyrics.scrollTo(0, scrollY);
  // }
  // }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {

    log.info("onCreateView() {}", this);

    view = inflater.inflate(R.layout.info, container, false);

    imageCover = (ImageView) view.findViewById(R.id.imageCover);
    textLyrics = (TextView) view.findViewById(R.id.textLyrics);
    scrollLyrics = (ScrollView) view.findViewById(R.id.scrollLyrics);

    Ampdc ampd = (Ampdc) getActivity();
    mpdService = ampd.getMpdService();
    coverService = ampd.getCoverService();
    lyricsService = ampd.getLyricsService();

    return view;
  }

  @Override
  public void onResume() {
    log.debug("onResume()");
    super.onResume();
    setMpdFile(mpdService.getCurrentSong());
    mpdService.addMpdListener(mpdListener);
  }

  @Override
  public void onPause() {
    log.debug("onPause()");
    super.onPause();
    mpdService.removeMpdListener(mpdListener);
    if (timer != null) {
      timer.cancel();
      timer = null;
    }

  }

}
