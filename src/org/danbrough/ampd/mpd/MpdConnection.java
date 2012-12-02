package org.danbrough.ampd.mpd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.LinkedList;

public class MpdConnection {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(MpdConnection.class.getSimpleName());

  private final String host;
  private final int port;
  private boolean running = false;
  private boolean connected = false;
  private OutputStreamWriter output;
  private BufferedReader input;
  private Socket socket = null;
  private MpdCommand currentCmd;
  private boolean idleSent = false;
  private final LinkedList<MpdCommand> cmdQueue = new LinkedList<MpdCommand>();

  public MpdConnection(String host, int port) {
    super();
    this.host = host;
    this.port = port;
  }

  public synchronized void start() {
    if (running)
      return;
    log.info("start()");
    running = true;
    connected = false;
    idleSent = false;
    cmdQueue.clear();

    new Thread() {
      @Override
      public void run() {
        try {
          socket = new Socket(host, port);
          input = new BufferedReader(new InputStreamReader(
              socket.getInputStream()));
          output = new OutputStreamWriter(socket.getOutputStream());

          new Thread() {
            @Override
            public void run() {
              try {
                noIdleLoop();
              } catch (IOException e) {
              } finally {
                MpdConnection.this.stop();
              }
            }
          }.start();

          readLoop();
        } catch (IOException e) {
        } finally {
          MpdConnection.this.stop();
        }
      }
    }.start();

  }

  protected void readLoop() throws IOException {
    while (running) {
      String line = input.readLine();
      // log.info("read [{}]", line);

      if (!connected) {
        connected = true;
        onConnect(line);
        sendCommands();
        continue;
      }

      if (line.startsWith("OK") || line.startsWith("ACK")) {

        if (idleSent) {
          idleSent = false;
        } else {

          currentCmd.onAfterResponse(line);

          cmdQueue.removeFirst();
          currentCmd = null;
        }

        sendCommands();
      } else {

        if (idleSent) {
          onChanged(line.substring(9));
        } else {
          currentCmd.processLine(line);
        }
      }

    }
  }

  private void sendCommands() throws IOException {
    if (!cmdQueue.isEmpty()) {
      currentCmd = cmdQueue.getFirst();
      currentCmd.onBeforeRequest();
      currentCmd.write(this);
      if (!currentCmd.hasResponse()) {
        currentCmd = null;
        cmdQueue.removeFirst();
        sendCommands();
      }
    } else {
      write("idle\n");
      idleSent = true;
    }
  }

  protected void noIdleLoop() throws IOException {
    while (running) {
      synchronized (cmdQueue) {
        try {
          cmdQueue.wait();
        } catch (InterruptedException e) {
          return;
        }
        if (!running)
          return;

        if (idleSent) {
          // cmdQueue not empty so send a noidle to terminate the existing idle

          write("noidle\n");
          continue;
        }
      }
    }
  }

  protected void onConnect(String line) {
    log.info("onConnect() [{}]", line);
  }

  protected void onChanged(String changed) {
    log.debug("onChanged() [{}]", changed);
  }

  protected void onStop() {
    log.info("onStop()");
  }

  public final synchronized void stop() {
    if (!running)
      return;

    running = false;

    log.info("stop()");
    if (socket != null) {
      try {
        socket.close();
        socket = null;
      } catch (Exception e) {
      }
    }
    if (input != null) {
      try {
        input.close();
        input = null;
      } catch (Exception e) {
      }
    }

    if (output != null) {
      try {
        output.close();
        output = null;
      } catch (Exception e) {
      }
    }

    log.debug("getting lock on cmdQueue");
    synchronized (cmdQueue) {
      log.debug("notifying all");
      cmdQueue.notifyAll();
      cmdQueue.clear();
    }

    onStop();
  }

  public void invoke(MpdCommand cmd) {
    synchronized (cmdQueue) {
      cmdQueue.addLast(cmd);
      cmdQueue.notify();
    }
  }

  void write(String data) throws IOException {
    synchronized (output) {
      output.write(data);
      output.flush();
    }
  }

}
