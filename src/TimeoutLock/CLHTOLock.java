package TimeoutLock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class CLHTOLock implements TOLock {

  public CLHTOLock() {
    tail = new AtomicReference<TOQNode>(null);
    node = ThreadLocal.withInitial(TOQNode::new);
  }

  @Override
  public boolean TryLock(long time, TimeUnit unit) {
    long start_time = System.currentTimeMillis();
    long duration = TimeUnit.MILLISECONDS.convert(time, unit);
    long end_time = start_time + duration;
    TOQNode toqnode = new TOQNode();
    node.set(toqnode);
    TOQNode pred = tail.getAndSet(toqnode);
    if (pred == null || pred.pred == Available) return true;
    while (end_time > System.currentTimeMillis()) {
      TOQNode pred_pred = pred.pred;
      if (pred_pred == Available) return true;
      if (pred_pred != null) pred = pred_pred;
    }
    if (!tail.compareAndSet(toqnode, pred)) toqnode.pred = pred;
    return false;
  }

  @Override
  public void Unlock() {
    TOQNode toqnode = node.get();
    if (!tail.compareAndSet(toqnode, null)) toqnode.pred = Available;
  }

  private static TOQNode Available = new TOQNode();
  private AtomicReference<TOQNode> tail;
  private ThreadLocal<TOQNode> node;
}
