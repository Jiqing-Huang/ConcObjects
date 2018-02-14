package Lock;

import TimeoutLock.State;

import java.util.concurrent.atomic.AtomicReference;

public class CQNode {
  public CQNode() {
    state = new AtomicReference<State>(State.Free);
  }
  public AtomicReference<State> state;
  public volatile CQNode pred;
}
