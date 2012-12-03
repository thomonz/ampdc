package org.danbrough.ampdc;

import org.danbrough.ampdc.mpd.MpdListener;
import org.danbrough.ampdc.mpd.MpdListenerAdapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.actionbarsherlock.app.SherlockFragment;

public class ControlsFragment extends SherlockFragment {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(ControlsFragment.class);

  MpdService mpd;
  SeekBar seekVolume;

  MpdListener mpdListener = new MpdListenerAdapter() {
    @Override
    public void onChangedMixer() {
      log.warn("mixer changed");
      getActivity().runOnUiThread(new Runnable() {

        @Override
        public void run() {
          setVolume(mpd.getStatus().getVolume());
        }
      });
    }
  };

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.controls, container, false);
    seekVolume = (SeekBar) view.findViewById(R.id.seekVolume);
    seekVolume.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onProgressChanged(SeekBar seekBar, int progress,
          boolean fromUser) {
        if (fromUser) {
          mpd.setVolume(progress);
        }
      }
    });

    return view;
  }

  @Override
  public void onResume() {
    log.debug("onResume()");
    super.onResume();
    Ampdc ampd = (Ampdc) getActivity();
    mpd = ampd.getMpdService();
    int volume = mpd.getStatus().getVolume();
    setVolume(volume);

    mpd.addMpdListener(mpdListener);
  }

  @Override
  public void onPause() {
    super.onPause();
    mpd.removeMpdListener(mpdListener);
  }

  private void setVolume(int volume) {
    seekVolume.setProgress(volume);
  }

}
