package org.danbrough.ampd.mpd;

public interface MpdListener {

  void onChangedSong();

  void onChangedPlayer();

  void onChangedDatabase();

  void onChangedOptions();

  void onChangedUpdate();

  void onChangedMixer();

  void onChangedPlaylist();

  void onChangedProgress(int secs, int length);

  void onConnect();

  void onDisconnect();

}
