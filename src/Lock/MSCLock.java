package Lock;

import java.util.concurrent.atomic.AtomicReference;

public class MSCLock implements Lock {

  public MSCLock() {
    tail = new AtomicReference<QNode>(null);
    node = ThreadLocal.withInitial(QNode::new);
  }

  @Override
  public void Lock() {
    QNode qnode = node.get();
    QNode pred = tail.getAndSet(qnode);
    if (pred != null) {
      qnode.locked = true;
      pred.next = qnode;
      while (qnode.locked) {} // spin
    }
  }

  @Override
  public void Unlock() {
    QNode qnode = node.get();
    if (qnode.next == null) {
      if (tail.compareAndSet(qnode, null)) return;
      while (qnode.next == null) {} // spin
    }
    qnode.next.locked = false;
    qnode.next = null;
  }

  private AtomicReference<QNode> tail;
  private ThreadLocal<QNode> node;
}
