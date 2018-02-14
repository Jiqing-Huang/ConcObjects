package ConcQueue;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SynchronousQueue<T> implements ConcQueue<T> {

  public SynchronousQueue() {
    item = null;
    enqueuing = false;
    lock = new ReentrantLock();
    cond = lock.newCondition();
  }

  @Override
  public void Offer(T t) throws InterruptedException {
    lock.lock();
    try {
      while (enqueuing)
        cond.await();
      enqueuing = true;
      item = t;
      cond.signalAll();
      while (item != null)
        cond.await();
      enqueuing = false;
      cond.signalAll();
    } finally {
      lock.unlock();
    }
  }

  @Override
  public T Poll() throws InterruptedException {
    lock.lock();
    try {
      while (item == null)
        cond.await();
      T ret = item;
      item = null;
      cond.signalAll();
      return ret;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void Clear() {
    item = null;
    enqueuing = false;
  }

  private T item;
  private boolean enqueuing;
  private Lock lock;
  private Condition cond;
}
