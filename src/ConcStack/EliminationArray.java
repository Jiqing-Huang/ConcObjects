package ConcStack;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class EliminationArray<T> {

  public EliminationArray(int capacity) {
    exchanger = (LockFreeExchanger<T>[]) new LockFreeExchanger[capacity];
    for (int i = 0; i != capacity; ++i)
      exchanger[i] = new LockFreeExchanger<>();
    random = new Random();
  }

  public T Visit(T value, int range) throws TimeoutException {
    int slot = random.nextInt(range);
    return exchanger[slot].Exchange(value, timeout, TimeUnit.NANOSECONDS);
  }

  private int capacity;
  private LockFreeExchanger<T>[] exchanger;
  private Random random;
  private static final int timeout = 1_000;
}
