package ConcQueue;

import java.util.concurrent.atomic.AtomicReference;

public class SynchronousDualQueue<T> implements ConcQueue<T> {

  public SynchronousDualQueue() {
    head = new AtomicReference<>(new SDQNode<T>(null, NodeType.Item));
    tail = new AtomicReference<>(head.get());
  }

  @Override
  public void Offer(T t) {
    SDQNode<T> node = new SDQNode<>(t, NodeType.Item);
    while (true) {
      SDQNode<T> first = head.get();
      SDQNode<T> last = tail.get();
      if (first == last || last.type == NodeType.Item) {
        SDQNode<T> next = last.next.get();
        if (next != null) {
          tail.compareAndSet(last, next);
        } else if (last.next.compareAndSet(null, node)) {
          tail.compareAndSet(last, node);
          while (node.item.get() == t) {} // spin
          first = head.get();
          if (node == first.next.get())
            head.compareAndSet(first, node);
          return;
        }
      } else {
        SDQNode<T> second = first.next.get();
        if (first != head.get() || last != tail.get() || second == null) continue;
        boolean success = second.item.compareAndSet(null, t);
        head.compareAndSet(first, second);
        if (success) return;
      }
    }
  }

  @Override
  public T Poll() {
    SDQNode<T> node = new SDQNode<>(null, NodeType.Reservation);
    while (true) {
      SDQNode<T> first = head.get();
      SDQNode<T> last = tail.get();
      if (first == last || last.type == NodeType.Reservation) {
        SDQNode<T> next = last.next.get();
        if (next != null) {
          tail.compareAndSet(last, next);
        } else if (last.next.compareAndSet(null, node)) {
          tail.compareAndSet(last, node);
          while (node.item.get() == null) {} // spin
          T ret = node.item.get();
          first = head.get();
          if (node == first.next.get())
            head.compareAndSet(first, node);
          return ret;
        }
      } else {
        SDQNode<T> second = first.next.get();
        if (first != head.get() || last != tail.get() || second == null) continue;
        T ret = second.item.get();
        if (ret == null) continue;
        boolean success = second.item.compareAndSet(ret, null);
        head.compareAndSet(first, second);
        if (success) return ret;
      }
    }
  }

  @Override
  public void Clear() {
    head = new AtomicReference<>(new SDQNode<T>(null, NodeType.Item));
    tail = new AtomicReference<>(head.get());
  }

  private AtomicReference<SDQNode<T>> head;
  private AtomicReference<SDQNode<T>> tail;
}
