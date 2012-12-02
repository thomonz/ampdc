package org.danbrough.ampd.mpd;

import java.io.Serializable;

public class MpdNode implements Serializable {

  private static final long serialVersionUID = 8600222751062271166L;

  public static final String FILE = "file";

  String path = "";

  public MpdNode(String path) {
    super();
    this.path = path;
  }

  public String getPath() {
    return path;
  }

  public String getParentPath() {
    int i = path.lastIndexOf('/');
    return i == -1 ? "/" : path.substring(0, i);
  }

  public String getTitle() {
    int i = path.lastIndexOf('/');
    return i == -1 ? path : path.substring(i + 1);
  }

  @Override
  public String toString() {
    return getTitle();
  }
}
