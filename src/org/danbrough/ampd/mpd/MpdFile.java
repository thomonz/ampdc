package org.danbrough.ampd.mpd;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import android.annotation.SuppressLint;

@SuppressLint("SimpleDateFormat")
public class MpdFile extends MpdNode {

  private static SimpleDateFormat ISO8601DATEFORMAT = new SimpleDateFormat(
      "yyyy-MM-dd'T'HH:mm:ss'Z'");
  static {
    ISO8601DATEFORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
  }

  private static final long serialVersionUID = -4919174286907480479L;

  // file: Miriam Clancy/Magnetic/02 When I Do.mp3
  // Last-Modified: 2009-10-13T06:25:38Z
  // Time: 156
  // Artist: Miriam Clancy
  // Title: When I Do
  // Album: Magnetic
  // Track: 2
  // Date: 2009
  // Genre: Pop
  // Pos: 2
  // Id: 1

  public static final String TIME = "Time";
  public static final String ARTIST = "Artist";
  public static final String TITLE = "Title";
  public static final String ALBUM = "Album";
  public static final String TRACK = "Track";
  public static final String DATE = "Date";
  public static final String GENRE = "Genre";
  public static final String POSITION = "Pos";
  public static final String ID = "Id";
  public static final String LAST_MODIFIED = "Last-Modified";

  int id = 0;
  int time = 0;
  int position = 0;
  String artist = "";
  String genre = "";
  String album = "";
  int track = 0;
  int lastModified = 0;
  String title = null;

  public MpdFile(String path) {
    super(path);
  }

  public int getId() {
    return id;
  }

  public int getTime() {
    return time;
  }

  public int getPosition() {
    return position;
  }

  public String getArtist() {
    return artist;
  }

  public String getAlbum() {
    return album;
  }

  public String getGenre() {
    return genre;
  }

  public int getTrack() {
    return track;
  }

  public int getLastModified() {
    return lastModified;
  }

  @Override
  public String getTitle() {
    if (title != null)
      return title;
    title = getPath();
    int i = title.lastIndexOf('/');
    if (i != -1)
      title = title.substring(i + 1);

    return title;
  }

  @Override
  public String toString() {
    return getTitle();
  }

  public void setAttribute(String name, String value) {
    try {
      if (name.equals(TIME)) {
        time = Integer.parseInt(value);
      } else if (name.equals(ID)) {
        id = Integer.parseInt(value);
      } else if (name.equals(POSITION)) {
        position = Integer.parseInt(value);
      } else if (name.equals(ARTIST)) {
        artist = value;
      } else if (name.equals(GENRE)) {
        genre = value;
      } else if (name.equals(ALBUM)) {
        album = value;
      } else if (name.equals(TRACK)) {
        track = Integer.parseInt(value);
      } else if (name.equals(TITLE)) {
        title = value;
      } else if (name.equals(LAST_MODIFIED)) {
        lastModified = (int) (ISO8601DATEFORMAT.parse(value).getTime() / 1000);
      } else {
        // System.out.println("unhandled attribute " + name + ":" + value);
      }
    } catch (Exception e) {
      // System.out.println("Invalid file attribute: " + name + ":" + value);
    }
  }
}
