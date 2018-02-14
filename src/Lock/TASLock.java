package Lock;

import java.util.concurrent.atomic.AtomicBoolean;

public class TASLock implements Lock {
  public TASLock() {
    state = new AtomicBoolean(false);
  }

  @Override
  public void Lock() {
    while (state.getAndSet(true)); // spin
  }

  @Override
  public void Unlock() {
    state.set(false);
  }

  private AtomicBoolean state;
}
