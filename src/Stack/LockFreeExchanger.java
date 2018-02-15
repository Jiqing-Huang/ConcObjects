package Stack;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicStampedReference;

public class LockFreeExchanger<T> {

  public LockFreeExchanger() {
    slot = new AtomicStampedReference<>(null, Empty);
  }

  public T Exchange(T t, long timeout, TimeUnit unit) throws TimeoutException {
    long end_time = System.nanoTime() + unit.toNanos(timeout);
    int[] stamp = new int[] { Empty };
    while (true) {
      T item = slot.get(stamp);
      switch (stamp[0]) {
        case Empty:
          if (slot.compareAndSet(null, t, Empty, Waiting)) {
            while (System.nanoTime() < end_time) {
              item = slot.get(stamp);
              if (stamp[0] == Busy) {
                slot.set(null, Empty);
                return item;
              }
            }
            if (slot.compareAndSet(t, null, Waiting, Empty)) {
              throw new TimeoutException();
            } else {
              item = slot.get(stamp);
              slot.set(null, Empty);
              return item;
            }
          }
          break;
        case Waiting:
          if (slot.compareAndSet(item, t, Waiting, Busy)) {
            slot.set(null, Empty);
            return item;
          }
          break;
        case Busy:
          break;
        default: // unreachable
      }
    }
  }

  private AtomicStampedReference<T> slot;
  private static final int Empty = 0;
  private static final int Waiting = 1;
  private static final int Busy = 2;
}
