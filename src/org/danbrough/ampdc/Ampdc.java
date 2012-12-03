package org.danbrough.ampdc;

import org.danbrough.ampd.covers.CoverService;
import org.danbrough.ampd.lyrics.LyricsService;

/**
 * 
 * @author dan
 * aMPDy
 * 
 */
public interface Ampdc {
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
