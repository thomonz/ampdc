package org.danbrough.ampdc;

public interface Callback<T> {

  void onError(Exception e);

  void callback(T result);
}
