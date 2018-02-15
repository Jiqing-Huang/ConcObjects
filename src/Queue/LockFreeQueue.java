package Queue;

import java.util.concurrent.atomic.AtomicReference;

public class LockFreeQueue<T> implements ConcQueue<T> {

  public LockFreeQueue() {
    head = new AtomicReference<>(new AtomicCQNode<T>(null));
    tail = new AtomicReference<>(head.get());
  }

  @Override
  public void Offer(T t) {
    AtomicCQNode<T> node = new AtomicCQNode<>(t);
    while (true) {
      AtomicCQNode<T> last = tail.get();
      AtomicCQNode<T> next = last.next.get();
      if (last == tail.get())
        if (next == null) {
          if (last.next.compareAndSet(null, node)) {
            tail.compareAndSet(last, node);
            return;
          }
        } else {
          tail.compareAndSet(last, next);
        }
    }
  }

  @Override
  public T Poll() {
    while (true) {
      AtomicCQNode<T> first = head.get();
      AtomicCQNode<T> second = first.next.get();
      AtomicCQNode<T> last = tail.get();
      if (first == head.get())
        if (first == last) {
          if (second == null)
            throw new IllegalStateException("Queue is empty.");
          tail.compareAndSet(last, second);
        } else {
          T ret = second.value;
          if (head.compareAndSet(first, second)) {
            return ret;
          }
        }
    }
  }

  @Override
  public void Clear() {
    head = new AtomicReference<>(new AtomicCQNode<T>(null));
    tail = new AtomicReference<>(head.get());
  }

  private AtomicReference<AtomicCQNode<T>> head;
  private AtomicReference<AtomicCQNode<T>> tail;
}
