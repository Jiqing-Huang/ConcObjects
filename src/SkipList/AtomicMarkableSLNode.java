package SkipList;

import java.util.concurrent.atomic.AtomicMarkableReference;

@SuppressWarnings("unchecked")
public class AtomicMarkableSLNode<T> {
  public AtomicMarkableSLNode(int sentinel_key, int sentinel_level) {
    item = null;
    key = sentinel_key;
    next = new AtomicMarkableReference[sentinel_level + 1];
    for (int i = 0; i != next.length; ++i)
      next[i] = new AtomicMarkableReference<>(null, false);
    top_level = sentinel_level;
  }

  public AtomicMarkableSLNode(T t, int level) {
    item = t;
    key = t.hashCode();
    next = new AtomicMarkableReference[level + 1];
    for (int i = 0; i != next.length; ++i)
      next[i] = new AtomicMarkableReference<>(null, false);
    top_level = level;
  }

  public final T item;
  public final int key;
  public final AtomicMarkableReference<AtomicMarkableSLNode<T>>[] next;
  public int top_level;
}
