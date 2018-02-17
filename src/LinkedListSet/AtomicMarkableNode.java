package LinkedListSet;

import java.util.concurrent.atomic.AtomicMarkableReference;

public class AtomicMarkableNode<T> {
  public AtomicMarkableNode(T t, int hashcode) {
    item = t;
    key = hashcode;
    next = new AtomicMarkableReference<>(null, false);
  }

  public AtomicMarkableNode(int hashcode) {
    item = null;
    key = hashcode;
    next = new AtomicMarkableReference<>(null, false);
  }

  public T item;
  public int key;
  public AtomicMarkableReference<AtomicMarkableNode<T>> next;
}
