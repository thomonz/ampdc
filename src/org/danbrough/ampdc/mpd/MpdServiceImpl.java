package org.danbrough.ampdc.mpd;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.danbrough.ampdc.Callback;
import org.danbrough.ampdc.MpdService;

public class MpdServiceImpl implements MpdService {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(MpdServiceImpl.class.getSimpleName());

  private static MpdServiceImpl INSTANCE;

  public static MpdServiceImpl getInstance() {
    return INSTANCE;
  }

  final MpdDatabase db;

  boolean running = false;
  int progress = 0;
  int progressSetAt = 0;

  final HashSet<MpdListener> listeners = new HashSet<MpdListener>();
  final MpdStatus status = new MpdStatus();

  MpdFile currentSong;
  MpdConnection conn;
  MpdCommand currentCmd;
  List<PlaylistItem> playlist = new LinkedList<PlaylistItem>();

  final MpdCommand CMD_STATUS = new MpdCommand("status") {

    @Override
    protected void onBeforeRequest() {
      status.getChanges().clear();
    }

    @Override
    protected void processLine(String line) {
      status.processLine(line);
    }

    @Override
    protected void onAfterResponse(String lastLine) {
      for (String name : status.getChanges()) {
        if (name.equals(MpdProtocol.ATTR_STATE)) {
          fireOnChangedPlayer();
        } else if (name.equals(MpdProtocol.ATTR_SONGID)) {
          onSongChanged();
        } else if (name.equals(MpdProtocol.ATTR_TIME)) {
          String time[] = status.getTime().split(":");
          progress = Integer.parseInt(time[0]);
          progressSetAt = (int) (System.currentTimeMillis() / 1000);
          fireOnChangedProgress(progress, Integer.parseInt(time[1]));
        } else if (name.equals(MpdProtocol.ATTR_PLAYLIST)) {
          loadPlaylist();
        } else {

        }
        log.warn("changed: " + name);
      }
    }
  };

  public MpdServiceImpl(final MpdDatabase db) {
    super();
    this.db = db;
    INSTANCE = this;
  }

  private void fireOnChangedPlayer() {
    synchronized (listeners) {
      for (MpdListener listener : listeners)
        listener.onChangedPlayer();
    }
  }

  private void fireOnConnect() {
    synchronized (listeners) {
      for (MpdListener listener : listeners)
        listener.onConnect();
    }
  }

  private void fireOnDisconnect() {
    synchronized (listeners) {
      for (MpdListener listener : listeners)
        listener.onDisconnect();
    }
  }

  private void fireOnChangedPlaylist() {
    synchronized (listeners) {
      for (MpdListener listener : listeners)
        listener.onChangedPlaylist();
    }
  }

  private void fireOnChangedSong() {
    synchronized (listeners) {
      for (MpdListener listener : listeners)
        listener.onChangedSong();
    }
  }

  private void fireOnChangedProgress(int secs, int length) {
    synchronized (listeners) {
      for (MpdListener listener : listeners)
        listener.onChangedProgress(secs, length);
    }
  }

  @Override
  public final boolean isRunning() {
    return running;
  }

  protected MpdCommand invoke(MpdCommand cmd) {
    conn.invoke(cmd);
    return cmd;
  }

  @Override
  public synchronized void start() {
    log.info("start()");
    if (running) {
      log.warn("already running");
      return;
    }
    running = true;
    status.clear();
    db.load();

    MpdServer server = db.getServer();
    conn = new MpdConnection(server.getHost(), server.getPort()) {

      @Override
      protected void onConnect(String serverVersion) {
        log.info("onConnect() [{}]", serverVersion);

        db.setServer(db.getServer());
        cmdStatus();
        fireOnConnect();
      }

      @Override
      protected void onChanged(String changed) {
        log.info("onChanged() [{}]", changed);

        if (changed.equals(MpdProtocol.IDLE_PLAYER)) {
          cmdStatus();
        } else if (changed.equals(MpdProtocol.IDLE_PLAYLIST)) {
          cmdStatus();
        } else if (changed.equals(MpdProtocol.IDLE_DATABASE)) {
          fireOnDatabaseChanged();
        } else if (changed.equals(MpdProtocol.IDLE_OPTIONS)) {
          cmdStatus();
        } else if (changed.equals(MpdProtocol.IDLE_MIXER)) {
          cmdStatus();
        } else {
          log.warn("idle_change: " + changed);
        }
      }

      @Override
      protected void onStop() {
        fireOnDisconnect();
      }
    };
    conn.start();

  }

  @Override
  public synchronized void stop() {
    if (!running)
      return;
    log.info("stop()");
    running = false;
    if (conn != null) {
      conn.stop();
      conn = null;
    }

    db.saveAll();
  }

  @Override
  public final MpdDatabase getDatabase() {
    return db;
  }

  @Override
  public final MpdStatus getStatus() {
    return this.status;
  }

  @Override
  public void seekTo(int songPos, int progress) {
    invoke(new MpdCommand("seek " + songPos + " " + progress));
  }

  @Override
  public final void addMpdListener(MpdListener listener) {
    synchronized (listeners) {
      listeners.add(listener);
    }
    log.trace("listener count: {}", listeners.size());
  }

  @Override
  public final void removeMpdListener(MpdListener listener) {
    synchronized (listeners) {
      listeners.remove(listener);
    }
  }

  @Override
  public void cmdPause() {
    log.trace("cmdPause()");
    invoke(MpdProtocol.CMD_PAUSE);
  }

  @Override
  public void cmdPlay() {
    log.trace("cmdPlay");
    invoke(MpdProtocol.CMD_PLAY);
  }

  @Override
  public void cmdPrevious() {
    log.trace("cmdPrevious()");
    invoke(MpdProtocol.CMD_PREVIOUS);
  }

  @Override
  public void cmdNext() {
    log.trace("cmdNext()");
    invoke(MpdProtocol.CMD_NEXT);
  }

  @Override
  public void cmdStop() {
    log.trace("cmdStop()");
    invoke(MpdProtocol.CMD_STOP);
  }

  @Override
  public void cmdStatus() {
    log.trace("cmdStatus()");
    invoke(CMD_STATUS);
  }

  @Override
  public MpdFile getCurrentSong() {
    return currentSong;
  }

  @Override
  public List<PlaylistItem> getPlaylist() {
    return playlist;
  }

  @Override
  public int getProgress() {
    if (!status.isPlaying())
      return 0;
    int time = (int) (System.currentTimeMillis() / 1000);
    return time - progressSetAt + progress;
  }

  @Override
  public void listDirectory(final MpdDirectory dir,
      final Callback<Void> callback) {
    log.trace("listDirectory(): " + dir.getPath());
    invoke(new ListDirectoryCommand(dir) {
      @Override
      protected void onAfterResponse(String lastLine) {
        super.onAfterResponse(lastLine);
        callback.callback(null);
      }
    });
  }

  @Override
  public void listAlbums(MpdDirectory dir, final Callback<Void> callback) {
    log.trace("listDirectory(): " + dir.getPath());
    invoke(new ListAlbumsCommand() {
      @Override
      protected void onAfterResponse(String lastLine) {
        super.onAfterResponse(lastLine);
        callback.callback(null);
      }
    });
  }

  @Override
  public void listArtists(final Callback<List<String>> callback) {
    final LinkedList<String> artists = new LinkedList<String>();
    log.trace("listArtists()");
    invoke(new MpdCommand("list ARTIST") {

      @Override
      protected void processLine(String line) {
        if (line.startsWith("Artist: ")) {
          artists.add(line.substring(8));
        }
      }

      @Override
      protected void onAfterResponse(String lastLine) {
        super.onAfterResponse(lastLine);
        Collections.sort(artists);
        callback.callback(artists);
      }
    });
  }

  private void fireOnDatabaseChanged() {
    synchronized (listeners) {
      for (MpdListener listener : listeners)
        listener.onChangedDatabase();
    }
  }

  @Override
  public void addToPlaylist(MpdNode node, final boolean play,
      final boolean replace) {
    log.debug("addToPlaylist() path: " + node.getPath() + " play: " + play
        + " replace: " + replace);
    if (replace) {
      invoke(MpdProtocol.CMD_CLEAR);
    }

    final int playlistSize = playlist.size();

    invoke(new MpdCommand("add \"" + node.getPath() + "\""));

    if (play) {
      if (replace) {
        log.debug("seeking to 0");
        seekTo(0, 0);
      } else {
        log.debug("seeking to: " + playlistSize);
        seekTo(playlistSize, 0);
        cmdPlay();
      }
    }

  }

  protected void loadPlaylist() {
    log.debug("loadPlaylist()");

    invoke(new PlaylistInfo() {
      @Override
      protected void onPlaylistComplete(List<PlaylistItem> playlist) {
        MpdServiceImpl.this.playlist = playlist;
        fireOnChangedPlaylist();
      }
    });
  }

  protected void onSongChanged() {

    invoke(new MpdCommand("currentsong") {
      MpdFile file;

      @Override
      protected void processLine(String line) {
        int i = line.indexOf(": ");
        String key = line.substring(0, i);
        String value = line.substring(i + 2);
        if (key.equals(MpdFile.FILE)) {
          file = new MpdFile(value);
        } else {
          file.setAttribute(key, value);
        }
      }

      @Override
      protected void onAfterResponse(String lastLine) {
        currentSong = file;
        fireOnChangedSong();
      }
    });

  }

  @Override
  public void clearPlaylist() {
    invoke(new MpdCommand("clear"));
  }

  @Override
  public void setVolume(int vol) {
    invoke(new MpdCommand("setvol " + vol));
  }

  @Override
  public void connectTo(MpdServer server) {
    log.info("connectTo() {}", server);
    db.setServer(server);
    stop();
    start();
  }

}
