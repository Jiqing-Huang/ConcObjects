package HashSet;

import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.locks.ReentrantLock;

public class RefinableCuckooHashSet<T> extends PhasedCuckooHashSet<T> implements ConcHashSet<T> {

  public RefinableCuckooHashSet(int capacity) {
    super(capacity);
    int num_locks = Math.max(1, capacity / BucketsPerLock);
    locks = new ReentrantLock[2][num_locks];
    for (int i = 0; i != 2; ++i)
      for (int j = 0; j != num_locks; ++j)
        locks[i][j] = new ReentrantLock();
    owner = new AtomicMarkableReference<>(null, false);
  }

  @Override
  protected void Acquire(T t) {
    boolean[] mark = new boolean[] {true};
    Thread me = Thread.currentThread();
    Thread who = null;
    while (true) {
      while (mark[0] && who != me)
        who = owner.get(mark);
      ReentrantLock[][] local_locks = locks;
      ReentrantLock lock_zero = local_locks[0][Hash(t) % local_locks[0].length];
      ReentrantLock lock_one = local_locks[1][HashAlt(t) % local_locks[1].length];
      lock_zero.lock();
      lock_one.lock();
      who = owner.get(mark);
      if ((who == me || !mark[0]) && local_locks == locks) {
        return;
      } else {
        lock_zero.unlock();
        lock_one.unlock();
      }
    }
  }

  @Override
  protected void Release(T t) {
    locks[0][Hash(t) % locks[0].length].unlock();
    locks[1][HashAlt(t) % locks[1].length].unlock();
  }

  @Override
  protected boolean Resize(int old_capacity) {
    Thread me = Thread.currentThread();
    if (owner.compareAndSet(null, me, false, true))
      try {
        for (ReentrantLock lock: locks[0])
          while (lock.isLocked()); // spin
        for (ReentrantLock lock: locks[1])
          while (lock.isLocked()); // spin
        if (!super.Resize(old_capacity))
          return false;
        int num_locks = Math.max(1, capacity / BucketsPerLock);
        if (num_locks > locks[0].length) {
          ReentrantLock[][] new_locks = new ReentrantLock[2][num_locks];
          for (int i = 0; i != 2; ++i)
            for (int j = 0; j != num_locks; ++j)
              new_locks[i][j] = new ReentrantLock();
          locks = new_locks;
        }
        return true;
      } finally {
        owner.set(null, false);
      }
    return false;
  }

  private volatile ReentrantLock[][] locks;
  private AtomicMarkableReference<Thread> owner;
  private static final int BucketsPerLock = 64;
}
