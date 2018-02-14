package LinkedListSet;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LockableMarkableNode<T> {
  public LockableMarkableNode(T t) {
    item = t;
    key = item.hashCode();
    next = null;
    lock = new ReentrantLock();
    marked = false;
  }

  public LockableMarkableNode(int hashcode) {
    item = null;
    key = hashcode;
    next = null;
    lock = new ReentrantLock();
    marked = false;
  }

  public void Lock() {
    lock.lock();
  }

  public void Unlock() {
    lock.unlock();
  }

  public T item;
  public int key;
  public boolean marked;
  public LockableMarkableNode<T> next;
  private Lock lock;
}
