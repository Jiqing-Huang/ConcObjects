package ConcQueue;

import java.util.concurrent.locks.ReentrantLock;

public class UnboundedQueue<T> implements ConcQueue<T> {

  public UnboundedQueue() {
    enq_lock = new ReentrantLock();
    deq_lock = new ReentrantLock();
    head = new CQNode<T>(null);
    tail = head;
  }

  @Override
  public void Offer(T t) {
    enq_lock.lock();
    try {
      tail.next = new CQNode<>(t);
      tail = tail.next;
    } finally {
      enq_lock.unlock();
    }
  }

  @Override
  public T Poll() {
    T ret;
    deq_lock.lock();
    try {
      if (head.next == null)
        throw new IllegalStateException("Queue is empty");
      ret = head.next.value;
      head = head.next;
    } finally {
      deq_lock.unlock();
    }
    return ret;
  }

  @Override
  public void Clear() {
    head = new CQNode<T>(null);
    tail = head;
  }

  private ReentrantLock enq_lock, deq_lock;
  private CQNode<T> head, tail;
}
