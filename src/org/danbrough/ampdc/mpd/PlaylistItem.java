package org.danbrough.ampdc.mpd;

public class PlaylistItem {
  String title;
  String artist;
  String album;
  String file;

  public PlaylistItem() {
    super();
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getArtist() {
    return artist;
  }

  public void setArtist(String artist) {
    this.artist = artist;
  }

  public String getAlbum() {
    return album;
  }

  public void setAlbum(String album) {
    this.album = album;
  }

  @Override
  public String toString() {
    return title + " from " + album + " by " + artist;
  }
}
