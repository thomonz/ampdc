package org.danbrough.ampd;

public interface Callback<T> {

  void onError(Exception e);

  void callback(T result);
}
