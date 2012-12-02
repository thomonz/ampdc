package org.danbrough.ampd.covers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.danbrough.ampd.R;
import org.danbrough.ampd.mpd.MpdFile;
import org.danbrough.job.JobQueue;
import org.danbrough.job.JobQueue.JobCallback;

import android.content.Context;
import android.net.Uri;
import android.net.http.AndroidHttpClient;

public class CoverManager implements CoverService {

  private static final String LASTFM_API_KEY = "fa2e1d87c585c52854b877031863c3f6";
  private static final String LASTFM_URL = "http://ws.audioscrobbler.com/2.0?method=album.getinfo&api_key="
      + LASTFM_API_KEY + "&artist=%s&album=%s";

  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(CoverManager.class.getSimpleName());

  private final Context context;

  private final String userAgent;
  private AndroidHttpClient client;
  private final JobQueue<Runnable> queue;

  public CoverManager(Context context, JobQueue<Runnable> queue) {
    super();
    this.context = context;
    this.userAgent = context.getString(R.string.app_name);
    this.queue = queue;
  }

  @Override
  public void start() {
    client = AndroidHttpClient.newInstance(userAgent, context);
  }

  @Override
  public void stop() {
    client.close();
    client = null;
  }

  private String readStream(InputStream input) throws IOException {
    byte buf[] = new byte[1024];
    int c = 0;
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    while ((c = input.read(buf, 0, buf.length)) > -1) {
      bos.write(buf, 0, c);
    }
    bos.flush();
    input.close();
    return new String(bos.toByteArray(), "UTF8");
  }

  private void writeStream(InputStream input, OutputStream output)
      throws IOException {
    byte buf[] = new byte[1024];
    int c = 0;
    while ((c = input.read(buf)) != -1) {
      output.write(buf, 0, c);
    }
    input.close();
    output.close();
  }

  private static final long DAY = 1000 * 60 * 60 * 24;
  private static final long WEEK = DAY * 7;

  protected void downloadCover(MpdFile song, JobCallback<Uri> callback) {
    log.error("downloadCover() {}", song);
    String artist = song.getArtist();
    String album = song.getAlbum();
    String url = String.format(LASTFM_URL, Uri.encode(artist),
        Uri.encode(album));
    log.debug("url [{}]", url);

    File coverFile = getCoverFile(song);
    coverFile.getParentFile().mkdirs();

    HttpGet request = new HttpGet(url);
    HttpResponse response = null;
    try {
      response = client.execute(request);

      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode != HttpStatus.SC_OK) {
        log.trace("statusCode: {}", statusCode);
        return;
      }

      String xml = readStream(response.getEntity().getContent());
      log.debug("xml [{}]", xml);
      coverFile.createNewFile();

      String urlStart = "<image size=\"extralarge\">";
      int i = xml.indexOf(urlStart);
      if (i > -1) {
        int j = xml.indexOf("</image>", i + 1);
        String coverUrl = xml.substring(i + urlStart.length(), j);
        log.debug("coverUrl [{}]", coverUrl);

        if (!coverUrl.equals("")) {
          response = client.execute(new HttpGet(coverUrl));
          statusCode = response.getStatusLine().getStatusCode();
          if (statusCode != HttpStatus.SC_OK) {
            log.trace("statusCode: {}", statusCode);
            return;
          }

          writeStream(response.getEntity().getContent(), new FileOutputStream(
              coverFile));
          callback.onComplete(Uri.fromFile(coverFile));
        }
      }
    } catch (Exception e) {
      callback.onError(e);
    }

  }

  public File getCoverFile(MpdFile song) {
    String fileName = song.getArtist().toLowerCase() + "-"
        + song.getAlbum().toLowerCase();
    File cacheDir = context.getExternalCacheDir();
    if (cacheDir == null)
      cacheDir = context.getCacheDir();
    fileName = cacheDir.getAbsolutePath() + "/covers/" + fileName;
    fileName = fileName.replace(' ', '_') + ".png";
    return new File(fileName);
  }

  @Override
  public void getCover(final MpdFile song, final JobCallback<Uri> listener) {
    log.debug("getCover() {}", song);
    File cover = getCoverFile(song);
    if (cover.exists()) {
      log.trace("found existing cover");
      if (cover.length() == 0) {
        if ((System.currentTimeMillis() - cover.lastModified()) < WEEK) {
          listener.onError(new Exception("Cover not available"));
          return;
        }
      } else {
        listener.onComplete(Uri.fromFile(cover));
      }
      return;
    }

    queue.queueJob(new Runnable() {

      @Override
      public void run() {
        downloadCover(song, listener);
      }
    });

  }

}
