package LinkedListSet;

public class OptimisticList<T> implements LinkedListSet<T> {

  public OptimisticList() {
    head = new LockableNode<>(Integer.MIN_VALUE);
    head.next = new LockableNode<>(Integer.MAX_VALUE);
  }

  @Override
  public boolean Add(T t) {
    int key = t.hashCode();
    while (true) {
      LockableNode<T> pred = head;
      LockableNode<T> curr = pred.next;
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
            LockableNode<T> node = new LockableNode<>(t);
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
      LockableNode<T> pred = head;
      LockableNode<T> curr = pred.next;
      while (curr.key < key) {
        pred = curr;
        curr = curr.next;
      }
      pred.Lock();
      curr.Lock();
      try {
        if (Validate(pred, curr))
          if (curr.key == key) {
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
    while (true) {
      LockableNode<T> pred = head;
      LockableNode<T> curr = pred.next;
      while (curr.key < key) {
        pred = curr;
        curr = curr.next;
      }
      pred.Lock();
      curr.Lock();
      try {
        if (Validate(pred, curr))
          return curr.key == key;
      } finally {
        pred.Unlock();
        curr.Unlock();
      }
    }
  }

  private boolean Validate(LockableNode<T> pred, LockableNode<T> curr) {
    LockableNode<T> node = head;
    while (node.key <= pred.key) {
      if (node == pred) return pred.next == curr;
      node = node.next;
    }
    return false;
  }

  private LockableNode<T> head;
}
