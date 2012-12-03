package org.danbrough.ampd.covers;

import org.danbrough.ampdc.mpd.MpdFile;
import org.danbrough.job.JobQueue.JobCallback;

import android.net.Uri;

public interface CoverService {
  void getCover(MpdFile song, JobCallback<Uri> listener);

  void start();

  void stop();
}
