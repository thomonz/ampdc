package org.danbrough.ampd.mpd;

import java.io.IOException;

public class MpdCommand {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(MpdCommand.class.getSimpleName());

  String cmd = "nodata";

  public MpdCommand() {
    super();
  }

  public MpdCommand(String cmd) {
    super();
    this.cmd = cmd;
  }

  protected void processLine(String line) {
    log.trace(cmd + ": " + line);
  }

  public String getData() {
    return this.cmd;
  }

  public boolean hasResponse() {
    return true;
  }

  protected void onBeforeRequest() {
  }

  protected void onAfterResponse(String lastLine) {
  }

  public void write(MpdConnection conn) throws IOException {
    conn.write(getData() + '\n');
  }

  @Override
  public final String toString() {
    return "Command[" + getClass().getName() + ":" + cmd + "]";
  }
}
