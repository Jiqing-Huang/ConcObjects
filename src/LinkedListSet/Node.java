package LinkedListSet;

public class Node<T> {
  public Node(T t) {
    item = t;
    key = item.hashCode();
    next = null;
  }

  public Node(int hashcode) {
    item = null;
    key = hashcode;
    next = null;
  }

  public T item;
  public int key;
  public Node<T> next;
}
