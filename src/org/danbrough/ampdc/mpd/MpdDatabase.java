package org.danbrough.ampdc.mpd;

import java.util.List;

import org.danbrough.ampdc.Ampdc;

public interface MpdDatabase {

  MpdServer getServer();

  void setServer(MpdServer server);

  List<MpdServer> getServers();

  Ampdc.MpdView getLastView();

  Ampdc.LibraryMode getLastLibraryMode();

  void setLastLibraryMode(Ampdc.LibraryMode libraryMode);

  void setLastView(Ampdc.MpdView view);

  void setAttribute(Class context, String name, String value);

  String getAttribute(Class context, String name, String defaultValue);

  boolean getDownloadLyrics();

  void load();

  void saveAll();
}
