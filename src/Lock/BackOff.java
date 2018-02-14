package Lock;

import java.util.Random;

public class BackOff {

  public BackOff(int min, int max) {
    min_delay = min;
    max_delay = max;
    limit = min_delay;
    random = new Random();
  }

  public void Wait() {
    int delay = random.nextInt(limit - min_delay + 1) + min_delay;
    limit = Math.max(max_delay, 2 * limit);
    int milli_delay = delay / 1_000_000;
    int nano_delay = delay % 1_000_000;
    try {
      Thread.sleep(milli_delay, nano_delay);
    } catch (InterruptedException e) {
      return;
    }
  }

  public void Reset() {
    limit = min_delay;
  }

  private final int min_delay;
  private final int max_delay;
  private int limit;
  final private Random random;
}
