package ConcStack;

public class SNode<T> {
  public SNode(T t) {
    value = t;
    next = null;
  }

  public T value;
  public SNode<T> next;
}
