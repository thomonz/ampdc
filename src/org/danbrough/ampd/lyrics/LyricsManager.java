package org.danbrough.ampd.lyrics;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.danbrough.ampdc.R;
import org.danbrough.ampdc.mpd.MpdFile;
import org.danbrough.job.JobQueue;
import org.danbrough.job.JobQueue.JobCallback;

import android.content.Context;
import android.net.Uri;
import android.net.http.AndroidHttpClient;

public class LyricsManager implements LyricsService {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(LyricsManager.class.getSimpleName());

  private static final String URL = "http://letras.mus.br/winamp.php?t=%s";
  JobQueue<Runnable> jobQueue;
  Context context;
  String userAgent;

  AndroidHttpClient client;

  public LyricsManager(Context context, JobQueue<Runnable> jobQueue) {
    this.context = context;
    this.jobQueue = jobQueue;
    userAgent = context.getString(R.string.app_name);
  }

  @Override
  public void start() {
    log.info("start()");
    client = AndroidHttpClient.newInstance(userAgent, context);
  }

  @Override
  public void stop() {
    log.info("stop()");
    client.close();
    client = null;
  }

  @Override
  public void getLyrics(final MpdFile song, final JobCallback<String> listener) {
    log.debug("getLyrics() {}", song);
    File lyricsFile = getLyricsFile(song);

    if (lyricsFile.exists()) {
      if (lyricsFile.length() == 0) {
        if ((System.currentTimeMillis() - lyricsFile.lastModified()) < WEEK) {
          listener.onError(new Exception("Lyrics not available"));
          return;
        }
      } else {
        try {
          listener.onComplete(readLyrics(lyricsFile));
        } catch (IOException e) {
          log.error(e.getMessage(), e);
        }
        return;
      }
    }

    jobQueue.queueJob(new Runnable() {

      @Override
      public void run() {
        dowloadLyrics(song, listener);
      }
    });
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

  private String readLyrics(File lyricsFile) throws IOException {
    return readStream(new GZIPInputStream(new FileInputStream(lyricsFile)));
  }

  private static final long DAY = 1000 * 60 * 60 * 24;
  private static final long WEEK = DAY * 7;

  private void dowloadLyrics(MpdFile song, JobCallback<String> listener) {
    log.debug("dowloadLyrics() {}", song);

    File lyricsFile = getLyricsFile(song);
    lyricsFile.getParentFile().mkdirs();

    log.trace("lyrics file: " + lyricsFile);

    String artist = song.getArtist();
    String title = song.getTitle();
    String url = String.format(URL,
        Uri.encode(artist) + "-" + Uri.encode(title));
    log.debug("url [{}]", url);

    HttpGet request = new HttpGet(url);
    HttpResponse response = null;
    try {
      response = client.execute(request);
    } catch (Exception e) {
      listener.onError(e);
      return;
    }

    int statusCode = response.getStatusLine().getStatusCode();
    if (statusCode != HttpStatus.SC_OK) {
      log.trace("statusCode: {}", statusCode);
      listener.onError(new Exception("status code: " + statusCode));
      return;
    }

    try {
      BufferedReader input = new BufferedReader(new InputStreamReader(response
          .getEntity().getContent()));
      String s = null;
      StringBuffer content = new StringBuffer();

      while ((s = input.readLine()) != null) {
        content.append(s);
      }
      input.close();

      lyricsFile.createNewFile();
      String lyrics = content.toString();
      log.info("lyrics [{}]", lyrics);

      int i = lyrics.indexOf("</h2>");

      if (i > -1) {
        lyrics = lyrics.substring(i + 5);

        i = lyrics.indexOf("</div>");
        if (i > -1) {
          lyrics = lyrics.substring(i + 6);

          i = lyrics.indexOf("<div id=\"banner\">");
          if (i > -1) {
            lyrics = lyrics.substring(0, i).trim();

            log.warn("lyrics [{}]", lyrics);
            GZIPOutputStream output = new GZIPOutputStream(
                new FileOutputStream(lyricsFile));
            output.write(lyrics.getBytes());
            output.close();
            listener.onComplete(lyrics);
          }
        }
      }

    } catch (Exception e) {
      listener.onError(e);
    }

  }

  public File getLyricsFile(MpdFile song) {
    String fileName = song.getArtist().toLowerCase() + "-"
        + song.getAlbum().toLowerCase() + "-" + song.getTitle();
    File cacheDir = context.getExternalCacheDir();
    if (cacheDir == null)
      cacheDir = context.getCacheDir();
    fileName = cacheDir.getAbsolutePath() + "/lyrics/" + fileName;
    fileName = fileName.replace(' ', '_') + ".gz";
    return new File(fileName);
  }
}
