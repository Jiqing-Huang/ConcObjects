package Lock;

import java.util.concurrent.atomic.AtomicBoolean;

public class BackOffLock implements Lock {
  public BackOffLock(int min, int max) {
    state = new AtomicBoolean(false);
    backoff = new BackOff(min_delay, max_delay);
    min_delay = min;
    max_delay = max;
  }

  @Override
  public void Lock() {
    backoff.Reset();
    while (true) {
      while (state.get()); // spin
      if (!state.getAndSet(true)) {
        return;
      } else {
        backoff.Wait();
      }
    }
  }

  @Override
  public void Unlock() {
    state.set(false);
  }

  private AtomicBoolean state;
  private BackOff backoff;
  private int min_delay;
  private int max_delay;
}
