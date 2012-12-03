package org.danbrough.ampdc.mpd;

public class MpdServer {
  // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
  // .getLogger(MpdServer.class);

  String host = "127.0.0.1";
  int port = 6600;
  int lastAccessed = 0;
  String hash;

  public MpdServer() {
    super();
  }

  public MpdServer(String host, int port) {
    super();

    this.host = host;
    this.port = port;
  }

  public int getLastAccessed() {
    return this.lastAccessed;
  }

  public void setLastAccessed(int lastAccessed) {
    this.lastAccessed = lastAccessed;
  }

  public String getHost() {
    return this.host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public int getPort() {
    return this.port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getHash() {
    return hash;
  }

  public void setHash(String hash) {
    this.hash = hash;
  }

  @Override
  public String toString() {
    return host + ":" + port + "-" + lastAccessed;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null)
      return false;
    if (!(o instanceof MpdServer))
      return false;

    MpdServer s = (MpdServer) o;
    return s.host.equals(host) && s.port == port;
  }

  public void touchLastAccessed() {
    setLastAccessed((int) (System.currentTimeMillis() / 1000));
  }

}
