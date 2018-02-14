package Lock;

import java.util.concurrent.atomic.AtomicReference;

public class CLHLock implements Lock {

  public CLHLock() {
    tail = new AtomicReference<QNode>(new QNode());
    node = ThreadLocal.withInitial(QNode::new);
    pred = ThreadLocal.withInitial(() -> null);
  }

  @Override
  public void Lock() {
    QNode qnode = node.get();
    qnode.locked = true;
    QNode qpred = tail.getAndSet(qnode);
    pred.set(qpred);
    while (qpred.locked) {} // spin;
  }

  @Override
  public void Unlock() {
    QNode qnode = node.get();
    qnode.locked = false;
    node.set(pred.get());
  }

  private AtomicReference<QNode> tail;
  private ThreadLocal<QNode> pred;
  private ThreadLocal<QNode> node;
}
