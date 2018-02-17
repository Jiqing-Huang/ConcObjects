package HashSet;

import java.util.concurrent.locks.ReentrantLock;

public class StripedCuckooHashSet<T> extends PhasedCuckooHashSet<T> implements ConcHashSet<T> {

  public StripedCuckooHashSet(int capacity) {
    super(capacity);
    InitCapacity = capacity;
    locks = new ReentrantLock[2][capacity];
    for (int i = 0; i != 2; ++i)
      for (int j = 0; j != capacity; ++j)
        locks[i][j] = new ReentrantLock();
  }

  @Override
  protected void Acquire(T t) {
    locks[0][Hash(t) % InitCapacity].lock();
    locks[1][HashAlt(t) % InitCapacity].lock();
  }

  @Override
  protected void Release(T t) {
    locks[0][Hash(t) % InitCapacity].unlock();
    locks[1][HashAlt(t) % InitCapacity].unlock();
  }

  @Override
  protected boolean Resize(int old_capacity) {
    for (int i = 0; i != InitCapacity; ++i)
      locks[0][i].lock();
    try {
      return super.Resize(old_capacity);
    } finally {
      for (int i = 0; i != InitCapacity; ++i)
        locks[0][i].unlock();
    }
  }

  private ReentrantLock[][] locks;
  private final int InitCapacity;
}
