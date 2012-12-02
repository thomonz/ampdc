package org.danbrough.job;

import java.util.LinkedList;

public class JobQueue<T extends Runnable> {

  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(JobQueue.class.getSimpleName());

  private final LinkedList<T> queue = new LinkedList<T>();
  private boolean running = false;
  private boolean isDaemon = true;
  private int maxSize = 0;

  public interface JobListener<T> {
    void onComplete(T job);

    void onError(Exception e);
  }

  public interface JobCallback<R> {
    void onError(Exception error);

    void onComplete(R result);
  }

  public final synchronized void start() {
    log.info("start()");
    if (running)
      return;
    this.running = true;
    Thread thread = new Thread() {
      @Override
      public void run() {
        try {
          workerLoop();
        } catch (Exception e) {
          log.error(e.getMessage(), e);
        }
      }
    };
    thread.setDaemon(isDaemon);
    thread.start();
  }

  private void workerLoop() throws Exception {
    onStart();

    try {

      while (running) {

        synchronized (queue) {
          if (queue.isEmpty()) {
            try {
              queue.wait();
            } catch (InterruptedException e) {
              return;
            }
          }
        }

        if (!running)
          return;

        T job = null;
        synchronized (queue) {
          job = queue.removeFirst();
        }
        runJob(job);
      }

    } finally {
      running = false;
      onStop();
    }
  }

  public final synchronized void stop() {
    log.info("stop()");
    if (!running)
      return;

    this.running = false;
    synchronized (queue) {
      queue.notify();
    }
  }

  public final void queueJob(T Runnable) {
    synchronized (queue) {
      queue.addLast(Runnable);
      queue.notify();
      if (maxSize > 0 && queue.size() > maxSize) {
        onExpired(queue.removeFirst());
      }
    }
  }

  public final void clear() {
    synchronized (queue) {
      queue.clear();
    }
  }

  public final boolean isDaemon() {
    return this.isDaemon;
  }

  public final void setDaemon(boolean isDaemon) {
    this.isDaemon = isDaemon;
  }

  public final int getMaxSize() {
    return this.maxSize;
  }

  public final void setMaxSize(int maxSize) {
    this.maxSize = maxSize;
  }

  protected void runJob(T job) {
    job.run();
  }

  protected void onStart() {
  }

  protected void onStop() {
  }

  protected void onExpired(T job) {
    log.warn("onExpired() {}", job);
  }
}
