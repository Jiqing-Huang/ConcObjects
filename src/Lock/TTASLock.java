package Lock;

import java.util.concurrent.atomic.AtomicBoolean;

public class TTASLock implements Lock {
  TTASLock() {
    state = new AtomicBoolean(false);
  }

  @Override
  public void Lock() {
    while (true) {
      while (state.get()); // spin
      if (!state.getAndSet(true))
        return;
    }
  }

  @Override
  public void Unlock() {
    state.set(false);
  }

  private AtomicBoolean state;
}
