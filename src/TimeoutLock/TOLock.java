package TimeoutLock;

import java.util.concurrent.TimeUnit;

public interface TOLock {
  boolean TryLock(long time, TimeUnit unit);
  void Unlock();
}
