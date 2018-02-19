package PriorityQueue;

public interface ConcBoundedPQ<T extends Rankable> {
  void Offer(T t);
  T Poll();
}
