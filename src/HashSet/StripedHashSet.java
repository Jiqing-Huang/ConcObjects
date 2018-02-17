package HashSet;

import java.util.concurrent.locks.ReentrantLock;

public class StripedHashSet<T> extends BaseHashSet<T> implements ConcHashSet<T> {

  public StripedHashSet(int capacity) {
    super(capacity);
    InitCapacity = capacity;
    locks = new ReentrantLock[capacity];
    for (int i = 0; i != capacity; ++i)
      locks[i] = new ReentrantLock();
  }

  @Override
  protected void Acquire(T t) {
    int lock_id = t.hashCode() % InitCapacity;
    locks[lock_id].lock();
  }

  @Override
  protected void Release(T t) {
    int lock_id = t.hashCode() % InitCapacity;
    locks[lock_id].unlock();
  }

  @Override
  protected boolean Resize(int old_capacity) {
    for (ReentrantLock lock: locks)
      lock.lock();
    try {
      return super.Resize(old_capacity);
    } finally {
      for (ReentrantLock lock: locks)
        lock.unlock();
    }
  }

  private ReentrantLock[] locks;
  private final int InitCapacity;
}
