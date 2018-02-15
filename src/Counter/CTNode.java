package Counter;

enum State { Idle, First, Second, Result, Root }

public class CTNode {

  public CTNode() {
    first = 0;
    second = 0;
    result = 0;
    locked = false;
    state = State.Root;
    parent = null;
  }

  public CTNode(CTNode parent) {
    first = 0;
    second = 0;
    result = 0;
    locked = false;
    state = State.Idle;
    this.parent = parent;
  }

  synchronized public boolean Precombine() throws InterruptedException {
    while (locked)
      wait();
    switch (state) {
      case Idle:
        state = State.First;
        return true;
      case First:
        locked = true;
        state = State.Second;
        return false;
      case Root:
        return false;
      default:
        throw new IllegalStateException("Illegal node state.");
    }
  }

  synchronized public int Combine(int combined) throws InterruptedException {
    while (locked)
      wait();
    locked = true;
    first = combined;
    switch (state) {
      case First:
        return combined;
      case Second:
        return first + second;
      default:
        throw new IllegalStateException("Illegal node state.");
    }
  }

  synchronized public int Op(int combined) throws InterruptedException {
    switch (state) {
      case Root:
        int prior = result;
        result += combined;
        return prior;
      case Second:
        second = combined;
        locked = false;
        notifyAll();
        while (state != State.Result)
          wait();
        state = State.Idle;
        locked = false;
        notifyAll();
        return result;
      default:
        throw new IllegalStateException("Illegal node state.");
    }
  }

  synchronized public void Distribute(int prior) {
    switch (state) {
      case First:
        state = State.Idle;
        locked = false;
        break;
      case Second:
        result = prior + first;
        state = State.Result;
        break;
      default:
        throw new IllegalStateException("Illegal node state.");
    }
    notifyAll();
  }

  public CTNode Parent() {
    return parent;
  }

  private int first;
  private int second;
  private int result;
  private volatile boolean locked;
  private volatile State state;
  private CTNode parent;
}
