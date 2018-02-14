package ConcCounter;

public class SimpleCounter<T extends Associable<T>> implements ConcCounter<T> {

  public SimpleCounter(T identity) {
    counter = identity.Clone();
  }

  @Override
  public T GetAndAdd(T t, int thread_id) {
    return SyncGetAndAdd(t);
  }

  @Override
  public T AddAndGet(T t, int thread_id) {
    return SyncAddAndGet(t);
  }

  synchronized private T SyncGetAndAdd(T t) {
    T ret = counter.Clone();
    counter.Aggregate(t);
    return ret;
  }

  synchronized private T SyncAddAndGet(T t) {
    counter.Aggregate(t);
    T ret = counter.Clone();
    return ret;
  }

  private T counter;
}
