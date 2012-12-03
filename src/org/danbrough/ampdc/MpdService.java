package org.danbrough.ampdc;

import java.util.List;

import org.danbrough.ampdc.mpd.MpdDatabase;
import org.danbrough.ampdc.mpd.MpdDirectory;
import org.danbrough.ampdc.mpd.MpdFile;
import org.danbrough.ampdc.mpd.MpdListener;
import org.danbrough.ampdc.mpd.MpdNode;
import org.danbrough.ampdc.mpd.MpdServer;
import org.danbrough.ampdc.mpd.MpdStatus;
import org.danbrough.ampdc.mpd.PlaylistItem;

public interface MpdService {

  interface MpdFileCallback {
    void onFile(MpdFile file);
  }

  void addMpdListener(MpdListener listener);

  void removeMpdListener(MpdListener listener);

  void start();

  void stop();

  boolean isRunning();

  void cmdPause();

  void cmdPlay();

  void cmdPrevious();

  void cmdNext();

  void cmdStop();

  MpdStatus getStatus();

  MpdFile getCurrentSong();

  MpdDatabase getDatabase();

  List<PlaylistItem> getPlaylist();

  void seekTo(int songPos, int progress);

  void cmdStatus();

  int getProgress();

  void listDirectory(MpdDirectory dir, Callback<Void> callback);

  void listAlbums(MpdDirectory dir, Callback<Void> callback);

  void listArtists(Callback<List<String>> callback);

  void addToPlaylist(MpdNode node, boolean play, boolean replace);

  void setVolume(int vol);

  void clearPlaylist();

  void connectTo(MpdServer server);

}
