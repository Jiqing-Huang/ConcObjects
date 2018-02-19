package SkipList;

import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("unchecked")
public class SLNode<T> {
  public SLNode(int sentinel_key, int sentinel_level) {
    item = null;
    key = sentinel_key;
    next = new SLNode[sentinel_level + 1];
    marked = false;
    fully_linked = false;
    top_level = sentinel_level;
    lock = new ReentrantLock();
  }

  public SLNode(T t, int level) {
    item = t;
    key = t.hashCode();
    next = new SLNode[level + 1];
    marked = false;
    fully_linked = false;
    top_level = level;
    lock = new ReentrantLock();
  }

  public void Lock() {
    lock.lock();
  }

  public void Unlock() {
    lock.unlock();
  }

  public final T item;
  public final int key;
  public final SLNode<T>[] next;
  public volatile boolean marked;
  public volatile boolean fully_linked;
  public int top_level;
  private final ReentrantLock lock;
}
