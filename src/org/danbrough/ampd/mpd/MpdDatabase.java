package org.danbrough.ampd.mpd;

import java.util.List;

import org.danbrough.ampd.Ampd;

public interface MpdDatabase {

  MpdServer getServer();

  void setServer(MpdServer server);

  List<MpdServer> getServers();

  Ampd.MpdView getLastView();

  Ampd.LibraryMode getLastLibraryMode();

  void setLastLibraryMode(Ampd.LibraryMode libraryMode);

  void setLastView(Ampd.MpdView view);

  void setAttribute(Class context, String name, String value);

  String getAttribute(Class context, String name, String defaultValue);

  boolean getDownloadLyrics();

  void load();

  void saveAll();
}
