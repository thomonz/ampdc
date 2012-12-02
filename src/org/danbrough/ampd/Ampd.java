package org.danbrough.ampd;

import org.danbrough.ampd.covers.CoverService;
import org.danbrough.ampd.lyrics.LyricsService;

public interface Ampd {
  enum MpdView {
    INFO, LIBRARY, CONTROLS, PLAYLIST, SERVERS;
  }

  enum LibraryMode {
    DIRECTORIES, ARTISTS, PLAYLISTS;
  }

  MpdService getMpdService();

  CoverService getCoverService();

  LyricsService getLyricsService();

  void openView(MpdView view);

}
