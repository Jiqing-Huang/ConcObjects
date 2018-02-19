package SkipList;

import java.util.Random;

@SuppressWarnings("unchecked")
public class LockFreeSkipList<T> implements ConcSkipList<T> {

  public LockFreeSkipList() {
    head = new AtomicMarkableSLNode<>(Integer.MIN_VALUE, MaxLevel);
    tail = new AtomicMarkableSLNode<>(Integer.MAX_VALUE, MaxLevel);
    for (int level = 0; level <= MaxLevel; ++level)
      head.next[level].set(tail, false);
    random = ThreadLocal.withInitial(() -> new Random(Thread.currentThread().getId()));
  }

  @Override
  public boolean Add(T t) {
    int top_level = RandomLevel();
    AtomicMarkableSLNode<T>[] preds = new AtomicMarkableSLNode[MaxLevel + 1];
    AtomicMarkableSLNode<T>[] succs = new AtomicMarkableSLNode[MaxLevel + 1];
    while (true) {
      boolean found = Find(t, preds, succs);
      if (found) {
        return false;
      } else {
        AtomicMarkableSLNode<T> node = new AtomicMarkableSLNode<>(t, top_level);
        for (int level = 0; level <= top_level; ++level)
          node.next[level].set(succs[level], false);
        if (!preds[0].next[0].compareAndSet(succs[0], node, false, false))
          continue;
        for (int level = 1; level <= top_level; ++level)
          while (true) {
            if (preds[level].next[level].compareAndSet(succs[level], node, false, false)) break;
            Find(t, preds, succs);
          }
        return true;
      }
    }
  }

  @Override
  public boolean Remove(T t) {
    AtomicMarkableSLNode<T>[] preds = new AtomicMarkableSLNode[MaxLevel + 1];
    AtomicMarkableSLNode<T>[] succs = new AtomicMarkableSLNode[MaxLevel + 1];
    AtomicMarkableSLNode<T> succ;
    while (true) {
      boolean found = Find(t, preds, succs);
      if (!found) {
        return false;
      } else {
        AtomicMarkableSLNode<T> node = succs[0];
        for (int level = node.top_level; level >= 1; --level) {
          boolean[] mark = new boolean[] {false};
          succ = node.next[level].get(mark);
          while (!mark[0]) {
            node.next[level].compareAndSet(succ, succ, false, true);
            succ = node.next[level].get(mark);
          }
        }
        boolean[] mark = new boolean[] {false};
        succ = node.next[0].get(mark);
        while (true) {
          boolean i_marked_it = node.next[0].compareAndSet(succ, succ, false, true);
          succ = succs[0].next[0].get(mark);
          if (i_marked_it) {
            Find(t, preds, succs);
            return true;
          } else if (mark[0]) {
            return false;
          }
        }
      }
    }
  }

  @Override
  public boolean Contains(T t) {
    int key = t.hashCode();
    boolean[] mark = new boolean[] {false};
    AtomicMarkableSLNode<T> pred = head;
    AtomicMarkableSLNode<T> curr = null;
    AtomicMarkableSLNode<T> succ = null;
    for (int level = MaxLevel; level >= 0; --level) {
      curr = pred.next[level].getReference();
      while (true) {
        succ = curr.next[level].get(mark);
        while (mark[0]) {
          curr = curr.next[level].getReference();
          succ = curr.next[level].get(mark);
        }
        if (curr.key < key) {
          pred = curr;
          curr = succ;
        } else {
          break;
        }
      }
    }
    return curr.key == key;
  }

  private boolean Find(T t, AtomicMarkableSLNode<T>[] preds, AtomicMarkableSLNode<T>[] succs) {
    int key = t.hashCode();
    boolean[] mark = new boolean[] {false};
    boolean snip;
    AtomicMarkableSLNode<T> pred = null;
    AtomicMarkableSLNode<T> curr = null;
    AtomicMarkableSLNode<T> succ = null;
    retry: while (true) {
      pred = head;
      for (int level = MaxLevel; level >= 0; --level) {
        curr = pred.next[level].getReference();
        while (true) {
          succ = curr.next[level].get(mark);
          while (mark[0]) {
            snip = pred.next[level].compareAndSet(curr, succ, false, false);
            if (!snip) continue retry;
            curr = pred.next[level].getReference();
            succ = curr.next[level].get(mark);
          }
          if (curr.key < key) {
            pred = curr;
            curr = succ;
          } else {
            break;
          }
        }
        preds[level] = pred;
        succs[level] = curr;
      }
      return curr.key == key;
    }
  }

  private int RandomLevel() {
    int ret = 0;
    Random rnd = random.get();
    while (rnd.nextInt(InvertProb) == 0)
      ++ret;
    return ret;
  }

  private final AtomicMarkableSLNode<T> head;
  private final AtomicMarkableSLNode<T> tail;
  private ThreadLocal<Random> random;
  private static final int MaxLevel = 32;
  private static final int InvertProb = 4;
}
