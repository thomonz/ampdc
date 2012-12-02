package org.danbrough.ampd.mpd;

public class ListDirectoryCommand extends MpdCommand {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(ListDirectoryCommand.class.getSimpleName());

  MpdDirectory directory;
  MpdFile file;

  public ListDirectoryCommand(MpdDirectory directory) {
    super("lsinfo \"" + directory.getPath() + "\"");
    this.directory = directory;
  }

  private static final String PREFIX_FILE = "file";
  private static final String PREFIX_PLAYLIST = "playlist";
  private static final String PREFIX_DIRECTORY = "directory";

  @Override
  protected void onAfterResponse(String lastLine) {
  }

  @Override
  protected void processLine(String line) {
    // log.debug("processLine [{}]", line);
    int i = line.indexOf(": ");
    String key = line.substring(0, i);
    String value = line.substring(i + 2);
    if (key.equals(PREFIX_FILE)) {

      file = new MpdFile(value);

      directory.getChildren().add(file);
    } else if (key.equals(PREFIX_DIRECTORY)) {
      file = null;
      if (!value.startsWith("."))
        directory.getChildren().add(new MpdDirectory(value));
    } else if (key.equals(PREFIX_PLAYLIST)) {
      // ignore
      file = null;
    } else {
      // must be a file attribute
      if (file != null)
        file.setAttribute(key, value);
    }
  }
}
