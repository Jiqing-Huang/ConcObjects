package ConcQueue;

public interface ConcQueue<T> {
  void Offer(T t) throws InterruptedException;
  T Poll() throws InterruptedException;
  void Clear();
}
