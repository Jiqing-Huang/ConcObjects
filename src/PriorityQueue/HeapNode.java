package PriorityQueue;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

enum State { Empty, Available, Busy};

public class HeapNode<T extends Comparable<T>> {

  public HeapNode() {
    tag = State.Empty;
    lock = new ReentrantLock();
  }

  public void Init(T t) {
    item = t;
    tag = State.Busy;
    owner = Thread.currentThread().getId();
  }

  public void Lock() {
    lock.lock();
  }

  public void Unlock() {
    lock.unlock();
  }

  public boolean AmOwner() {
    return owner == Thread.currentThread().getId();
  }

  public State tag;
  public T item;
  public long owner;
  private Lock lock;
}
