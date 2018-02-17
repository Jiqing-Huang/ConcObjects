package HashSet;

import LinkedListSet.AtomicMarkableNode;
import LinkedListSet.LockFreeList;
import LinkedListSet.Window;
import java.util.concurrent.atomic.AtomicMarkableReference;

class BucketList<T> extends LockFreeList<T> implements ConcHashSet<T> {
  public BucketList() {
    head = new AtomicMarkableNode<>(0);
    head.next = new AtomicMarkableReference<>(new AtomicMarkableNode<>(Integer.MAX_VALUE), false);
  }

  private BucketList(AtomicMarkableNode<T> node) {
    head = node;
  }

  private int MakeOrdinaryKey(T t) {
    int code = t.hashCode() & Mask;
    return Reverse(code | HighMask);
  }

  private int MakeSenitalKey(int key) {
    return Reverse(key & Mask);
  }

  private int Reverse(int x) {
    int ret = 0;
    for (int i = 0; i != 28; ++i) {
      ret <<= 1;
      ret += (x & 1);
      x >>= 1;
    }
    return ret;
  }

  public BucketList<T> GetSenital(int index) {
    int key = MakeSenitalKey(index);
    boolean splice;
    while (true) {
      Window<T> window = Find(head, key);
      AtomicMarkableNode<T> pred = window.pred;
      AtomicMarkableNode<T> curr = window.curr;
      if (curr.key == key) {
        return new BucketList<>(curr);
      } else {
        AtomicMarkableNode<T> node = new AtomicMarkableNode<>(key);
        node.next.set(pred.next.getReference(), false);
        splice = pred.next.compareAndSet(curr, node, false, false);
        if (splice) return new BucketList<>(node);
      }
    }
  }

  @Override
  public boolean Contains(T t) {
    int key = Hash(t);
    Window window = Find(head, key);
    return window.curr.key == key;
  }

  @Override
  protected int Hash(T t) {
    return MakeOrdinaryKey(t);
  }

  private static final int HighMask = 0x08000000;
  private static final int Mask = 0x0fffffff;
}
