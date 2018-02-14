package ConcQueue;

public class CQNode<T> {
  public CQNode(T t) {
    value = t;
    next = null;
  }

  public T value;
  public CQNode<T> next;
}
