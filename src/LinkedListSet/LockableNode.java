package LinkedListSet;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LockableNode<T> {
  public LockableNode(T t) {
    item = t;
    key = item.hashCode();
    next = null;
    lock = new ReentrantLock();
  }

  public LockableNode(int hashcode) {
    item = null;
    key = hashcode;
    next = null;
    lock = new ReentrantLock();
  }

  public void Lock() {
    lock.lock();
  }

  public void Unlock() {
    lock.unlock();
  }

  public T item;
  public int key;
  public LockableNode<T> next;
  private Lock lock;
}