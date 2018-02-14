package Lock;

import java.util.concurrent.atomic.AtomicInteger;

public class ArrayLock implements Lock {
  public ArrayLock(int capacity) {
    size = capacity;
    slot_index = ThreadLocal.withInitial(() -> 0);
    end = new AtomicInteger(0);
    flags = new PaddedBoolean[size];
    flags[0] = new PaddedBoolean(NumBytesPerCacheLine, true);
    for (int i = 1; i != size; ++i)
      flags[i] = new PaddedBoolean(NumBytesPerCacheLine, false);
  }

  @Override
  public void Lock() {
    int slot = end.getAndIncrement() % size;
    slot_index.set(slot);
    while (!flags[slot].Get()); // spin
  }

  @Override
  public void Unlock() {
    int slot = slot_index.get();
    flags[slot].Set(false);
    flags[(slot + 1) % size].Set(true);
  }

  private ThreadLocal<Integer> slot_index;
  private AtomicInteger end;
  private volatile PaddedBoolean[] flags;
  private int size;
  private static final int NumBytesPerCacheLine = 64;
}
