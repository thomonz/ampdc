package org.danbrough.ampd.mpd;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class MpdStatus implements MpdProtocol {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(MpdStatus.class.getSimpleName());

  private final HashMap<String, String> attrs = new HashMap<String, String>();
  private final HashSet<String> changes = new HashSet<String>();

  public MpdStatus() {
    super();
  }

  /**
   * volume: 100 repeat: 0 random: 0 single: 0 consume: 0 playlist: 40
   * playlistlength: 37 xfade: 0 mixrampdb: 0.000000 mixrampdelay: nan state:
   * play song: 3 songid: 4 time: 56:214 elapsed: 55.577 bitrate: 192 audio:
   * 44100:24:2 nextsong: 4 nextsongid: 5
   */
  protected void processLine(String line) {
    int i = line.indexOf(": ");
    String name = line.substring(0, i);
    String value = line.substring(i + 2);
    String oldValue = attrs.get(name);

    if (!value.equals(oldValue)) {
      log.warn(name + " changed from " + oldValue + " to: " + value);
      changes.add(name);
    }

    attrs.put(name, value);
  }

  public Set<String> getChanges() {
    return changes;
  }

  public int getPlaylistIndex() {
    int n = getIntAttribute(ATTR_SONG, 0);
    return n;
  }

  public int getSongId() {
    return getIntAttribute(ATTR_SONGID, 0);
  }

  public int getVolume() {
    return getIntAttribute(ATTR_VOLUME, 0);
  }

  private int getIntAttribute(String name, int defaultValue) {
    String s = attrs.get(name);
    if (s == null) {
      return defaultValue;
    }
    try {
      return Integer.parseInt(s);
    } catch (NumberFormatException e) {
      log.error("bad number: " + s);
    }
    return defaultValue;
  }

  private String getStringAttribute(String name, String defaultValue) {
    String s = attrs.get(name);
    if (s != null)
      return s;
    return defaultValue;
  }

  public String getState() {
    return getStringAttribute(ATTR_STATE, "");
  }

  public boolean isPlaying() {
    return getState().equals(STATE_PLAY);
  }

  public void clear() {
    attrs.clear();
    changes.clear();
  }

  public String getTime() {
    return getStringAttribute(ATTR_TIME, "0:0");
  }
}
