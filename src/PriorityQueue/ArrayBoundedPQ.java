package PriorityQueue;

import Stack.LockFreeStack;

@SuppressWarnings("unchecked")
public class ArrayBoundedPQ<T extends Rankable> implements ConcBoundedPQ<T> {

  public ArrayBoundedPQ(int range) {
    this.range = range;
    bins = new LockFreeStack[range];
    for (int i = 0; i != range; ++i)
      bins[i] = new LockFreeStack<>();
  }

  @Override
  public void Offer(T t) {
    int rank = t.Rank();
    bins[rank].Push(t);
  }

  @Override
  public T Poll() {
    T t = null;
    int rank = 0;
    while (t == null && rank != range)
      if (bins[rank].Size() == 0) {
        ++rank;
      } else {
        try {
          t = bins[rank].Pop();
        } catch (IllegalStateException e) {
          ++rank;
        }
      }
    if (t == null) {
      throw new IllegalStateException();
    } else {
      return t;
    }
  }

  private LockFreeStack<T>[] bins;
  private int range;
}
