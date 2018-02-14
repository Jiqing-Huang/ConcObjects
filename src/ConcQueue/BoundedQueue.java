package ConcQueue;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("Duplicates")
public class BoundedQueue<T> implements ConcQueue<T> {

  public BoundedQueue(int capacity) {
    this.capacity = capacity;
    size = new AtomicInteger(0);
    enq_lock = new ReentrantLock();
    deq_lock = new ReentrantLock();
    not_full_cond = enq_lock.newCondition();
    not_empty_cond = deq_lock.newCondition();
    head = new CQNode<T>(null);
    tail = head;
  }

  @Override
  public void Offer(T t) {
    boolean was_empty = false;
    enq_lock.lock();
    try {
      while (size.get() == capacity)
        not_full_cond.await();
      tail.next = new CQNode<>(t);
      tail = tail.next;
      if (size.getAndIncrement() == 0)
        was_empty = true;
    } catch (InterruptedException e) {
      return;
    } finally {
      enq_lock.unlock();
    }
    if (was_empty) {
      deq_lock.lock();
      try {
        not_empty_cond.signalAll();
      } finally {
        deq_lock.unlock();
      }
    }
  }

  @Override
  public T Poll() {
    boolean was_full = false;
    T ret;
    deq_lock.lock();
    try {
      while (size.get() == 0)
        not_empty_cond.await();
      ret = head.next.value;
      head = head.next;
      if (size.getAndDecrement() == capacity)
        was_full = true;
    } catch (InterruptedException e) {
      return null;
    } finally {
      deq_lock.unlock();
    }
    if (was_full) {
      enq_lock.lock();
      try {
        not_full_cond.signalAll();
      } finally {
        enq_lock.unlock();
      }
    }
    return ret;
  }

  @Override
  public void Clear() {
    size.set(0);
    head = new CQNode<T>(null);
    tail = head;
  }

  private ReentrantLock enq_lock, deq_lock;
  private Condition not_full_cond, not_empty_cond;
  private AtomicInteger size;
  private int capacity;
  private CQNode<T> head, tail;
}
