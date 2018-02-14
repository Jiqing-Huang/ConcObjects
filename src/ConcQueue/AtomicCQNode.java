package ConcQueue;

import java.util.concurrent.atomic.AtomicReference;

public class AtomicCQNode<T> {
  public AtomicCQNode(T t) {
    value = t;
    next = new AtomicReference<>(null);
  }

  public T value;
  public AtomicReference<AtomicCQNode<T>> next;
}
