package org.danbrough.ampd;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.danbrough.ampd.Ampd.LibraryMode;
import org.danbrough.ampd.Ampd.MpdView;
import org.danbrough.ampd.mpd.MpdDatabase;
import org.danbrough.ampd.mpd.MpdServer;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class AmpdDatabase implements MpdDatabase {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(AmpdDatabase.class);

  LinkedList<MpdServer> servers = null;
  boolean downloadLyrics = false;
  Context context;
  MpdServer newServer;
  String defaultHost;
  int defaultPort;
  private LibraryMode lastLibraryMode;

  private MpdView lastView;
  private final HashMap<String, String> attrs = new HashMap<String, String>();

  private static final String DOWNLOAD_LYRICS = "downloadLyrics";
  private static final boolean DOWNLOAD_LYRICS_DEFAULT = false;

  private static final String SERVER_HOST = "serverHost";
  private static final String SERVER_HOST_DEFAULT = "192.168.0.1";
  private static final String SERVER_PORT = "serverPort";
  private static final int SERVER_PORT_DEFAULT = 6600;
  private static final String SERVER_LAST_ACCESSED = "serverLastAccessed";
  private static final int SERVER_LAST_ACCESSED_DEFAULT = 0;

  private static final String SERVER_HASH = "serverHash";
  private static final String SERVER_HASH_DEFAULT = "";

  private static final String LAST_VIEW = "lastView";
  private static final String LAST_VIEW_DEFAULT = MpdView.PLAYLIST.name();
  private static final String LAST_LIBRARY_MODE = "lastLibraryMode";
  private static final String LAST_LIBRARY_MODE_DEFAULT = LibraryMode.PLAYLISTS
      .name();

  private static final String ATTR_KEYS = "attrKeys";
  private static final String ATTR_PREFIX = "attr_";

  public AmpdDatabase(Context context) {
    super();
    this.context = context;
    // getPreferences().registerOnSharedPreferenceChangeListener(
    // new OnSharedPreferenceChangeListener() {
    //
    // @Override
    // public void onSharedPreferenceChanged(SharedPreferences prefs,
    // String key) {
    // log.error("PREF CHANGED: {}", key);
    // for (String s : prefs.getAll().keySet()) {
    // try {
    // log.debug("{}:{}", s, prefs.getString(s, ""));
    // } catch (ClassCastException e) {
    // try {
    // log.debug("{}:{}", s, prefs.getInt(s, 0));
    // } catch (ClassCastException e2) {
    //
    // }
    // }
    // }
    // if (key.equals(SERVER_HOST + "_0")) {
    // log.error("host changed");
    // }
    //
    // }
    // });
  }

  private SharedPreferences getPreferences() {
    return PreferenceManager.getDefaultSharedPreferences(context);
  }

  @Override
  public MpdServer getServer() {
    return servers.get(0);
  }

  @Override
  public List<MpdServer> getServers() {
    return servers;
  }

  @Override
  public synchronized void setServer(MpdServer server) {
    log.error("setServer() :{}", server);
    defaultHost = server.getHost();
    defaultPort = server.getPort();
    server.touchLastAccessed();
    if (!servers.contains(server))
      servers.add(server);
  }

  @Override
  public boolean getDownloadLyrics() {
    return downloadLyrics;
  }

  @Override
  public void saveAll() {
    log.error("saveAll()");
    SharedPreferences prefs = getPreferences();
    Editor editor = prefs.edit();
    editor.putBoolean(DOWNLOAD_LYRICS, downloadLyrics);
    editor.putString(SERVER_HOST, defaultHost);
    editor.putString(SERVER_PORT, defaultPort + "");
    saveServers(editor);

    editor.putString(LAST_VIEW, lastView.name());
    editor.putString(LAST_LIBRARY_MODE, lastLibraryMode.name());

    String keys = "";
    for (String key : attrs.keySet()) {
      keys += keys.equals("") ? key : "," + key;
      editor.putString(ATTR_PREFIX + key, attrs.get(key));
    }
    editor.putString(ATTR_KEYS, keys);
    log.error("KEYS [{}]", keys);

    editor.commit();
  }

  @Override
  public synchronized void load() {
    log.error("load()");

    SharedPreferences prefs = getPreferences();

    downloadLyrics = prefs.getBoolean(DOWNLOAD_LYRICS, DOWNLOAD_LYRICS_DEFAULT);
    loadServers(prefs);

    defaultHost = prefs.getString(SERVER_HOST, SERVER_HOST_DEFAULT);

    defaultPort = Integer.parseInt(prefs.getString(SERVER_PORT,
        SERVER_PORT_DEFAULT + ""));
    MpdServer server = new MpdServer(defaultHost, defaultPort);
    servers.remove(server);
    servers.addFirst(server);

    lastView = MpdView.valueOf(prefs.getString(LAST_VIEW, LAST_VIEW_DEFAULT));
    lastLibraryMode = LibraryMode.valueOf(prefs.getString(LAST_LIBRARY_MODE,
        LAST_LIBRARY_MODE_DEFAULT));

    String attrKeys = prefs.getString(ATTR_KEYS, "");
    for (String key : attrKeys.split(",")) {
      attrs.put(key, prefs.getString(ATTR_PREFIX + key, null));
    }

  }

  private synchronized void loadServers(SharedPreferences prefs) {
    servers = new LinkedList<MpdServer>();
    int n = 0;
    while (true) {
      String host = prefs.getString(SERVER_HOST + "_" + n, null);
      if (host == null)
        break;
      String port = prefs.getString(SERVER_PORT + "_" + n, SERVER_PORT_DEFAULT
          + "");
      log.error("found server: " + host);
      int lastAccessed = prefs.getInt(SERVER_LAST_ACCESSED + "_" + n,
          SERVER_LAST_ACCESSED_DEFAULT);
      String hash = prefs.getString(SERVER_HASH + "_" + n, SERVER_HASH_DEFAULT);
      MpdServer server = new MpdServer(host, Integer.parseInt(port));
      server.setLastAccessed(lastAccessed);
      server.setHash(hash);
      servers.add(server);
      n++;

    }

    log.error("loaded : " + servers.size() + " servers");
  }

  private synchronized void saveServers(Editor editor) {
    int n = 0;
    for (n = 0; n < servers.size(); n++) {
      MpdServer server = servers.get(n);
      editor.putString(SERVER_HOST + "_" + n, server.getHost());
      editor.putString(SERVER_PORT + "_" + n, server.getPort() + "");
      editor.putInt(SERVER_LAST_ACCESSED + "_" + n, server.getLastAccessed());
      editor.putString(SERVER_HASH + "_" + n, server.getHash());
    }
    SharedPreferences prefs = getPreferences();
    while (true) {
      String host = prefs.getString(SERVER_HOST + "_" + n, null);
      if (host == null)
        break;
      editor.remove(SERVER_HOST + "_" + n);
      editor.remove(SERVER_PORT + "_" + n);
      editor.remove(SERVER_HASH + "_" + n);
      editor.remove(SERVER_LAST_ACCESSED + "_" + n);
      n++;
    }
  }

  @Override
  public MpdView getLastView() {
    return lastView;
  }

  @Override
  public void setLastView(MpdView view) {
    this.lastView = view;
  }

  @Override
  public LibraryMode getLastLibraryMode() {
    return lastLibraryMode;
  }

  @Override
  public void setLastLibraryMode(LibraryMode libraryMode) {
    this.lastLibraryMode = libraryMode;
  }

  @Override
  public String getAttribute(Class context, String name, String defaultValue) {
    String o = attrs.get(context.getSimpleName() + ":" + name);
    return o == null ? defaultValue : o;
  }

  @Override
  public void setAttribute(Class context, String name, String value) {
    attrs.put(context.getSimpleName() + ":" + name, value);
  }
}
