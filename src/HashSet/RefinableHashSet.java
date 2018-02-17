package HashSet;

import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.locks.ReentrantLock;

public class RefinableHashSet<T> extends BaseHashSet<T> implements ConcHashSet<T> {

  public RefinableHashSet(int capacity) {
    super(capacity);
    int num_locks = Math.max(1, capacity / BucketsPerLock);
    locks = new ReentrantLock[num_locks];
    for (int i = 0; i != num_locks; ++i)
      locks[i] = new ReentrantLock();
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
      ReentrantLock[] local_locks = locks;
      ReentrantLock lock = local_locks[t.hashCode() % local_locks.length];
      lock.lock();
      who = owner.get(mark);
      if ((who == me || !mark[0]) && local_locks == locks) {
        return;
      } else {
        lock.unlock();
      }
    }
  }

  @Override
  protected void Release(T t) {
    locks[t.hashCode() % locks.length].unlock();
  }

  @Override
  protected boolean Resize(int old_capacity) {
    Thread me = Thread.currentThread();
    if (owner.compareAndSet(null, me, false, true))
      try {
        for (ReentrantLock lock : locks)
          while (lock.isLocked()); // spin
        if (!super.Resize(old_capacity))
          return false;
        int num_locks = Math.max(1, table.length / BucketsPerLock);
        if (num_locks > locks.length) {
          ReentrantLock[] new_locks = new ReentrantLock[num_locks];
          for (int i = 0; i != new_locks.length; ++i)
            new_locks[i] = new ReentrantLock();
          locks = new_locks;
        }
        return true;
      } finally {
        owner.set(null, false);
      }
    return false;
  }

  private volatile ReentrantLock[] locks;
  private AtomicMarkableReference<Thread> owner;
  private static final int BucketsPerLock = 8;
}
