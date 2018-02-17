package HashSet;

import java.util.concurrent.locks.ReentrantLock;

public class CoarseHashSet<T> extends BaseHashSet<T> implements ConcHashSet<T> {

  public CoarseHashSet(int capacity) {
    super(capacity);
    lock = new ReentrantLock();
  }

  @Override
  protected void Acquire(T t) {
    lock.lock();
  }

  @Override
  protected void Release(T t) {
    lock.unlock();
  }

  @Override
  protected boolean Resize(int old_capacity) {
    lock.lock();
    try {
      return super.Resize(old_capacity);
    } finally {
      lock.unlock();
    }
  }

  private ReentrantLock lock;
}
