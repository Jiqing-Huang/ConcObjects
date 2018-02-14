package LinkedListSet;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CoarseList<T> implements LinkedListSet<T> {

  public CoarseList() {
    head = new Node<>(Integer.MIN_VALUE);
    head.next = new Node<>(Integer.MAX_VALUE);
    lock = new ReentrantLock();
  }

  @Override
  public boolean Add(T t) {
    Node<T> pred, curr;
    int key = t.hashCode();
    lock.lock();
    try {
      pred = head;
      curr = head.next;
      while (curr.key < key) {
        pred = curr;
        curr = curr.next;
      }
      if (curr.key == key) {
        return false;
      } else {
        Node<T> node = new Node<>(t);
        node.next = curr;
        pred.next = node;
        return true;
      }
    } finally {
      lock.unlock();
    }
  }

  @Override
  public boolean Remove(T t) {
    Node<T> pred, curr;
    int key = t.hashCode();
    lock.lock();
    try {
      pred = head;
      curr = head.next;
      while (curr.key < key) {
        pred = curr;
        curr = curr.next;
      }
      if (curr.key == key) {
        pred.next = curr.next;
        return true;
      } else {
        return false;
      }
    } finally {
      lock.unlock();
    }
  }

  @Override
  public boolean Contains(T t) {
    Node<T> node = head;
    int key = t.hashCode();
    lock.lock();
    try {
      while (node.key < key)
        node = node.next;
      return node.key == key;
    } finally {
      lock.unlock();
    }
  }

  private Node<T> head;
  private Lock lock;
}
