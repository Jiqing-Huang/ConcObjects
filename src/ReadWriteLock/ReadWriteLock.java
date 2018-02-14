package ReadWriteLock;

import java.util.concurrent.locks.Lock;

public interface ReadWriteLock {
  Lock ReadLock();
  Lock WriteLock();
}
