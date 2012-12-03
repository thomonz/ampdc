package org.danbrough.ampdc.mpd;

import java.util.LinkedList;
import java.util.List;

public class PlaylistInfo extends MpdCommand {

  List<PlaylistItem> playlist;
  PlaylistItem item;

  public PlaylistInfo() {
    super("playlistinfo");
  }

  @Override
  protected final void onBeforeRequest() {
    playlist = new LinkedList<PlaylistItem>();
  }

  @Override
  protected final void processLine(String line) {
    int n = line.indexOf(": ");
    String name = line.substring(0, n);
    String value = line.substring(n + 2);

    if (name.equals(MpdFile.FILE)) {
      if (item != null)
        playlist.add(item);
      item = new PlaylistItem();
      int i = value.lastIndexOf('/');
      if (i != -1)
        value = value.substring(i + 1);
      i = value.lastIndexOf('.');
      if (i != -1)
        value = value.substring(0, i);
      item.setTitle(value);

    } else if (name.equals(MpdFile.ALBUM)) {
      item.setAlbum(value);
    } else if (name.equals(MpdFile.ARTIST)) {
      item.setArtist(value);
    } else if (name.equals(MpdFile.TITLE)) {
      item.setTitle(value);
    }

  }

  @Override
  protected final void onAfterResponse(String lastLine) {
    if (item != null)
      playlist.add(item);
    onPlaylistComplete(playlist);
  }

  protected void onPlaylistComplete(List<PlaylistItem> playlist) {
  }

}
