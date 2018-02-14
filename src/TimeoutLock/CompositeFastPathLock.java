package TimeoutLock;

import Lock.CQNode;

import java.util.concurrent.TimeUnit;

public class CompositeFastPathLock extends CompositeLock implements TOLock {

  @Override
  public boolean TryLock(long time, TimeUnit unit) {
    if (FastPathLock()) return true;
    if (super.TryLock(time, unit)) {
      while ((tail.getStamp() & FastPath) != 0) {} // spin
      return true;
    }
    return false;
  }

  @Override
  public void Unlock() {
    if (!FastPathUnlock())
      super.Unlock();
  }

  private boolean FastPathLock() {
    int old_stamp, new_stamp;
    int[] stamp = new int[] {0};
    CQNode cqnode = tail.get(stamp);
    old_stamp = stamp[0];
    if (cqnode != null) return false;
    if ((old_stamp & FastPath) != 0) return false;
    new_stamp = (old_stamp + 1) | FastPath;
    return tail.compareAndSet(cqnode, null, old_stamp, new_stamp);
  }

  private boolean FastPathUnlock() {
    int old_stamp, new_stamp;
    old_stamp = tail.getStamp();
    if ((old_stamp & FastPath) == 0) return false;
    int[] stamp = new int[] {0};
    CQNode cqnode;
    do {
      cqnode = tail.get(stamp);
      old_stamp = stamp[0];
      new_stamp = old_stamp & (~FastPath);
    } while (!tail.compareAndSet(cqnode, cqnode, old_stamp, new_stamp));
    return true;
  }

  private static final int FastPath = 0xf0000000;
}
