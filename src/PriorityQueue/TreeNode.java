package PriorityQueue;

import Stack.LockFreeStack;

import java.util.concurrent.atomic.AtomicInteger;

public class TreeNode<T> {
  public TreeNode(boolean is_leaf) {
    if (is_leaf) {
      counter = null;
      bin = new LockFreeStack<>();
    } else {
      counter = new AtomicInteger(0);
      bin = null;
    }
    parent = null;
    left = null;
    right = null;
  }

  public boolean IsLeaf() {
    return right == null;
  }

  public AtomicInteger counter;
  public TreeNode parent, left, right;
  public LockFreeStack<T> bin;
}
