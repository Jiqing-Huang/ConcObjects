package LinkedListSet;

public class LazyList<T> implements LinkedListSet<T> {

  public LazyList() {
    head = new LockableMarkableNode<>(Integer.MIN_VALUE);
    head.next = new LockableMarkableNode<>(Integer.MAX_VALUE);
  }

  @Override
  public boolean Add(T t) {
    int key = t.hashCode();
    while (true) {
      LockableMarkableNode<T> pred = head;
      LockableMarkableNode<T> curr = pred.next;
      while (curr.key < key) {
        pred = curr;
        curr = curr.next;
      }
      pred.Lock();
      curr.Lock();
      try {
        if (Validate(pred, curr))
          if (curr.key == key) {
            return false;
          } else {
            LockableMarkableNode<T> node = new LockableMarkableNode<>(t);
            node.next = curr;
            pred.next = node;
            return true;
          }
      } finally {
        pred.Unlock();
        curr.Unlock();
      }
    }
  }

  @Override
  public boolean Remove(T t) {
    int key = t.hashCode();
    while (true) {
      LockableMarkableNode<T> pred = head;
      LockableMarkableNode<T> curr = pred.next;
      while (curr.key < key) {
        pred = curr;
        curr = curr.next;
      }
      pred.Lock();
      curr.Lock();
      try {
        if (Validate(pred, curr))
          if (curr.key == key) {
            curr.marked = true;
            pred.next = curr.next;
            return true;
          } else {
            return false;
          }
      } finally {
        pred.Unlock();
        curr.Unlock();
      }
    }
  }

  @Override
  public boolean Contains(T t) {
    int key = t.hashCode();
    LockableMarkableNode<T> curr = head;
    while (curr.key < key)
      curr = curr.next;
    return !curr.marked && curr.key == key;
  }

  private boolean Validate(LockableMarkableNode<T> pred, LockableMarkableNode<T> curr) {
    return !pred.marked && !curr.marked && pred.next == curr;
  }

  private LockableMarkableNode<T> head;
}
