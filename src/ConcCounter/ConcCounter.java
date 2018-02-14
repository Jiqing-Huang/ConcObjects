package ConcCounter;

public interface ConcCounter<T extends Associable<T>> {
  T GetAndAdd(T t, int thread_id) throws InterruptedException;
  T AddAndGet(T t, int thread_id) throws InterruptedException;
}
