package PriorityQueue;

public interface ConcUnboundedPQ<T extends Comparable<T>> {
  void Offer(T t);
  T Poll();
}
