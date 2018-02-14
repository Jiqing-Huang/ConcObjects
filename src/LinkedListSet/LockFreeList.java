package LinkedListSet;

import java.util.concurrent.atomic.AtomicMarkableReference;

public class LockFreeList<T> implements LinkedListSet<T> {

  public LockFreeList() {
    head = new AtomicMarkableNode<>(Integer.MIN_VALUE);
    head.next = new AtomicMarkableReference<>(new AtomicMarkableNode<>(Integer.MAX_VALUE), false);
  }

  @Override
  public boolean Add(T t) {
    int key = t.hashCode();
    while (true) {
      Window<T> window = Find(head, key);
      AtomicMarkableNode<T> pred = window.pred;
      AtomicMarkableNode<T> curr = window.curr;
      if (curr.key == key) {
        return false;
      } else {
        AtomicMarkableNode<T> node = new AtomicMarkableNode<>(t);
        node.next = new AtomicMarkableReference<>(curr, false);
        if (pred.next.compareAndSet(curr, node, false, false)) return true;
      }
    }
  }

  @Override
  public boolean Remove(T t) {
    int key = t.hashCode();
    boolean snip;
    while (true) {
      Window<T> window = Find(head, key);
      AtomicMarkableNode<T> pred = window.pred;
      AtomicMarkableNode<T> curr = window.curr;
      if (curr.key == key) {
        AtomicMarkableNode<T> succ = curr.next.getReference();
        snip = curr.next.attemptMark(succ, true);
        if (!snip) continue;
        pred.next.compareAndSet(curr, succ, false, false);
        return true;
      } else {
        return false;
      }
    }
  }

  @Override
  public boolean Contains(T t) {
    boolean[] marked = new boolean[] {false};
    int key = t.hashCode();
    AtomicMarkableNode<T> curr = head;
    while (curr.key < key) {
      curr = curr.next.getReference();
      AtomicMarkableNode<T> succ = curr.next.get(marked);
    }
    return (curr.key == key && !marked[0]);
  }

  private Window<T> Find(AtomicMarkableNode<T> head, int key) {
    AtomicMarkableNode<T> pred, curr, succ;
    boolean[] marked = new boolean[] {false};
    boolean snip;
    retry: while (true) {
      pred = head;
      curr = pred.next.getReference();
      while (true) {
        succ = curr.next.get(marked);
        while (marked[0]) {
          snip = pred.next.compareAndSet(curr, succ, false, false);
          if (!snip) continue retry;
          curr = succ;
          succ = curr.next.get(marked);
        }
        if (curr.key >= key) return new Window<T>(pred, curr);
        pred = curr;
        curr = succ;
      }
    }
  }

  private AtomicMarkableNode<T> head;
}
