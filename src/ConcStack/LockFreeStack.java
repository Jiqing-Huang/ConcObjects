package ConcStack;

import Lock.BackOff;

import java.util.concurrent.atomic.AtomicReference;

public class LockFreeStack<T> implements ConcStack<T> {

  public LockFreeStack() {
    top = new AtomicReference<>(null);
    backoff = new BackOff(MinDelay, MaxDelay);
  }

  @Override
  public void Push(T t) {
    SNode<T> node = new SNode<>(t);
    while (true)
      if (TryPush(node)) {
        return;
      } else {
        backoff.Wait();
      }
  }

  @Override
  public T Pop() {
    while (true) {
      SNode<T> node = TryPop();
      if (node != null) {
        return node.value;
      } else {
        backoff.Wait();
      }
    }
  }

  @Override
  public void Clear() {
    top = new AtomicReference<>(null);
  }

  protected boolean TryPush(SNode<T> node) {
    SNode<T> old_top = top.get();
    node.next = old_top;
    return top.compareAndSet(old_top, node);
  }

  protected SNode<T> TryPop() throws IllegalStateException {
    SNode<T> first = top.get();
    if (first == null)
      throw new IllegalStateException("Stack is empty.");
    SNode<T> second = first.next;
    return (top.compareAndSet(first, second))? first : null;
  }

  private AtomicReference<SNode<T>> top;
  private static final int MinDelay = 1_000;
  private static final int MaxDelay = 1_000_000;
  protected BackOff backoff;
}
