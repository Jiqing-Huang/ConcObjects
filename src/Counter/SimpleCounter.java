package Counter;

public class SimpleCounter implements Counter.ConcCounter {

  public SimpleCounter() {
    counter = 0;
  }

  @Override
  public int GetAndIncrement(int thread_id) throws InterruptedException {
    return SyncGetAndIncrement();
  }

  @Override
  public int IncrementAndGet(int thread_id) throws InterruptedException {
    return SyncGetAndIncrement() + 1;
  }

  synchronized private int SyncGetAndIncrement() {
    return counter++;
  }

  private int counter;
}
