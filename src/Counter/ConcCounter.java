package Counter;

public interface ConcCounter {
  int GetAndIncrement(int thread_id) throws InterruptedException;
  int IncrementAndGet(int thread_id) throws InterruptedException;
}
