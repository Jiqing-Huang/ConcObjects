package LinkedListSet;

public class Window<T> {

  public Window(AtomicMarkableNode<T> pred, AtomicMarkableNode<T> curr) {
    this.pred = pred;
    this.curr = curr;
  }

  public AtomicMarkableNode<T> pred;
  public AtomicMarkableNode<T> curr;
}
