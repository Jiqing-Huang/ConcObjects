package ConcCounter;

enum State { Idle, First, Second, Result, Root }

public class CTNode<T extends Associable<T>> {

  public CTNode(T identity) {
    first = identity.Clone();
    second = identity.Clone();
    result = identity.Clone();
    locked = false;
    state = State.Root;
    parent = null;
  }

  public CTNode(CTNode<T> parent, T identity) {
    first = identity.Clone();
    second = identity.Clone();
    result = identity.Clone();
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

  synchronized public T Combine(T t) throws InterruptedException {
    while (locked)
      wait();
    locked = true;
    first.Set(t);
    switch (state) {
      case First:
        return t;
      case Second:
        t.Aggregate(second);
        return t;
      default:
        throw new IllegalStateException("Illegal node state.");
    }
  }

  synchronized public T Op(T combined) throws InterruptedException {
    switch (state) {
      case Root:
        T prior = result.Clone();
        result.Aggregate(combined);
        return prior;
      case Second:
        second.Set(combined);
        locked = false;
        notifyAll();
        while (state != State.Result)
          wait();
        state = State.Idle;
        combined.Set(result);
        locked = false;
        notifyAll();
        return combined;
      default:
        throw new IllegalStateException("Illegal node state.");
    }
  }

  synchronized public void Distribute(T prior) {
    switch (state) {
      case First:
        state = State.Idle;
        locked = false;
        break;
      case Second:
        result.Set(prior);
        result.Aggregate(first);
        state = State.Result;
        break;
      default:
        throw new IllegalStateException("Illegal node state.");
    }
    notifyAll();
  }

  public CTNode<T> Parent() {
    return parent;
  }

  private T first;
  private T second;
  private T result;
  private volatile boolean locked;
  private volatile State state;
  private CTNode<T> parent;
}
