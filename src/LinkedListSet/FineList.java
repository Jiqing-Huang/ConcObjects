package LinkedListSet;

public class FineList<T> implements LinkedListSet<T> {

  public FineList() {
    head = new LockableNode<>(Integer.MIN_VALUE);
    head.next = new LockableNode<>(Integer.MAX_VALUE);
  }

  @Override
  public boolean Add(T t) {
    LockableNode<T> pred = head;
    pred.Lock();
    LockableNode<T> curr;
    int key = t.hashCode();
    try {
      curr = pred.next;
      curr.Lock();
      try {
        while (curr.key < key) {
          pred.Unlock();
          pred = curr;
          curr = curr.next;
          curr.Lock();
        }
        if (curr.key == key) {
          return false;
        } else {
          LockableNode<T> node = new LockableNode<>(t);
          node.next = curr;
          pred.next = node;
          return true;
        }
      } finally {
        curr.Unlock();
      }
    } finally {
      pred.Unlock();
    }
  }

  @Override
  public boolean Remove(T t) {
    LockableNode<T> pred = head;
    pred.Lock();
    LockableNode<T> curr;
    int key = t.hashCode();
    try {
      curr = pred.next;
      curr.Lock();
      try {
        while (curr.key < key) {
          pred.Unlock();
          pred = curr;
          curr = curr.next;
          curr.Lock();
        }
        if (curr.key == key) {
          pred.next = curr.next;
          return true;
        } else {
          return false;
        }
      } finally {
        curr.Unlock();
      }
    } finally {
      pred.Unlock();
    }
  }

  @Override
  public boolean Contains(T t) {
    LockableNode<T> pred = head;
    pred.Lock();
    LockableNode<T> curr;
    int key = t.hashCode();
    try {
      curr = pred.next;
      curr.Lock();
      try {
        while (curr.key < key) {
          pred.Unlock();
          pred = curr;
          curr = curr.next;
          curr.Lock();
        }
        return curr.key == key;
      } finally {
        curr.Unlock();
      }
    } finally {
      pred.Unlock();
    }
  }

  private LockableNode<T> head;
}
