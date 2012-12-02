package org.danbrough.ampd.mpd;

public interface MpdProtocol {

  MpdCommand CMD_PAUSE = new MpdCommand("pause");
  MpdCommand CMD_PLAY = new MpdCommand("play");
  MpdCommand CMD_STOP = new MpdCommand("stop");
  MpdCommand CMD_NEXT = new MpdCommand("next");
  MpdCommand CMD_PREVIOUS = new MpdCommand("previous");
  MpdCommand CMD_CLEAR = new MpdCommand("clear");

  String ATTR_STATE = "state";
  String ATTR_SONGID = "songid";
  String ATTR_SONG = "song";
  String ATTR_PLAYLIST = "playlist";
  String ATTR_TIME = "time";
  String ATTR_VOLUME = "volume";
  String ATTR_ELAPSED = "elapsed";

  String STATE_PLAY = "play";
  String STATE_PAUSE = "pause";
  String STATE_STOP = "stop";

  /*
   * update: a database update has started or finished. If the database was
   * modified during the update, the database event is also emitted.
   */
  String IDLE_UPDATE = "update";

  /*
   * database: the song database has been modified after update.
   */
  String IDLE_DATABASE = "database";

  /*
   * stored_playlist: a stored playlist has been modified, renamed, created or
   * deleted
   */
  String IDLE_STORE_PLAYLIST = "stored_playlist";

  /* playlist: the current playlist has been modified */
  String IDLE_PLAYLIST = "playlist";

  // player: the player has been started, stopped or seeked
  String IDLE_PLAYER = "player";

  /* mixer: the volume has been changed */
  String IDLE_MIXER = "mixer";

  /* output: an audio output has been enabled or disabled */
  String IDLE_OUTPUT = "output";

  /* options: options like repeat, random, crossfade, replay gain */
  String IDLE_OPTIONS = "options";

  /* sticker: the sticker database has been modified. */
  String IDLE_STICKER = "sticker";

  /* subscription: a client has subscribed or unsubscribed to a channel */
  String IDLE_SUBSCRIPTION = "subscription";

  /*
   * message: a message was received on a channel this client is subscribed to;
   * this event is only emitted when the queue is empty
   */
  String IDLE_MESSAGE = "message";

}
