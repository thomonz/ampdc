package org.danbrough.ampd;

public interface ListCallback<T> {

  void onItem(T item);

  void onComplete();
}
