package TimeoutLock;

import Lock.BackOff;
import Lock.CQNode;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicStampedReference;

public class CompositeLock implements TOLock {

  public CompositeLock() {
    tail = new AtomicStampedReference<CQNode>(null, 0);
    waiting = new CQNode[Size];
    for (int i = 0; i != Size; ++i)
      waiting[i] = new CQNode();
    node = ThreadLocal.withInitial(() -> null);
    backoff = new BackOff(MinBackoff, MaxBackoff);
    random = new Random();
  }

  @Override
  public boolean TryLock(long time, TimeUnit unit) {
    long start_time = System.currentTimeMillis();
    long duration = TimeUnit.MILLISECONDS.convert(time, unit);
    long end_time = start_time + duration;
    backoff.Reset();
    try {
      CQNode cqnode = AcquireCQNode(end_time);
      CQNode pred = SpliceCQNode(cqnode, end_time);
      WaitForPredecessor(pred, cqnode, end_time);
      return true;
    } catch (TimeoutException e) {
      return false;
    }
  }

  @Override
  public void Unlock() {
    CQNode cqnode = node.get();
    cqnode.state.set(State.Released);
    node.set(null);
  }

  private CQNode AcquireCQNode(long end_time) throws TimeoutException {
    CQNode cqnode = waiting[random.nextInt(Size)];
    CQNode cur_tail;
    int[] cur_stamp = new int[] {0};
    while (end_time > System.currentTimeMillis()) {
      if (cqnode.state.compareAndSet(State.Free, State.Waiting)) return cqnode;
      cur_tail = tail.get(cur_stamp);
      State state = cqnode.state.get();
      if (state == State.Aborted || state == State.Released)
        if (cqnode == cur_tail) {
          CQNode pred = null;
          if (state == State.Aborted) pred = cqnode.pred;
          if (tail.compareAndSet(cur_tail, pred, cur_stamp[0], cur_stamp[0] + 1)) {
            cqnode.state.set(State.Waiting);
            return cqnode;
          }
        }
      backoff.Wait();
    }
    throw new TimeoutException();
  }

  private CQNode SpliceCQNode(CQNode cqnode, long end_time) throws TimeoutException {
    CQNode cur_tail;
    int[] cur_stamp = new int[] {0};
    do {
      cur_tail = tail.get(cur_stamp);
      if (end_time <= System.currentTimeMillis()) {
        cqnode.state.set(State.Free);
        throw new TimeoutException();
      }
    } while (!tail.compareAndSet(cur_tail, cqnode, cur_stamp[0], cur_stamp[0] + 1));
    return cur_tail;
  }

  private void WaitForPredecessor(CQNode pred, CQNode cqnode, long end_time) throws TimeoutException {
    if (pred == null) {
      node.set(cqnode);
      return;
    }
    State pred_state = pred.state.get();
    while (pred_state != State.Released) {
      if (pred_state == State.Aborted) {
        CQNode temp = pred;
        pred = pred.pred;
        temp.state.set(State.Free);
      }
      if (end_time <= System.currentTimeMillis()) {
        cqnode.pred = pred;
        cqnode.state.set(State.Aborted);
        throw new TimeoutException();
      }
      pred_state = pred.state.get();
    }
    pred.state.set(State.Free);
    node.set(cqnode);
  }

  private static final int Size = 8;
  private static final int MinBackoff = 1;
  private static final int MaxBackoff = 100;
  private BackOff backoff;
  protected AtomicStampedReference<CQNode> tail;
  private CQNode[] waiting;
  private ThreadLocal<CQNode> node;
  private Random random;
}
