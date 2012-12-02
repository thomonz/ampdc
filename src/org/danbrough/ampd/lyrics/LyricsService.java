package org.danbrough.ampd.lyrics;

import org.danbrough.ampd.mpd.MpdFile;
import org.danbrough.job.JobQueue.JobCallback;

public interface LyricsService {

  void getLyrics(MpdFile song, JobCallback<String> listener);

  void start();

  void stop();
}
