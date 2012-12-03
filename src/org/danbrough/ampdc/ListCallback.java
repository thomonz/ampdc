package org.danbrough.ampdc;

public interface ListCallback<T> {

  void onItem(T item);

  void onComplete();
}
