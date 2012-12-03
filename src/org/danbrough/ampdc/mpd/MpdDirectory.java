package org.danbrough.ampdc.mpd;

import java.util.LinkedList;
import java.util.List;

public class MpdDirectory extends MpdNode {

  private static final long serialVersionUID = 7141151394340094588L;

  LinkedList<MpdNode> children;
  int fileCount = 0;

  public MpdDirectory(String path) {
    super(path);
  }

  public List<MpdNode> getChildren() {
    if (children == null)
      children = new LinkedList<MpdNode>();
    return children;
  }
}
