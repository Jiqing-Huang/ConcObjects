package ConcStack;

import java.util.concurrent.TimeoutException;

public class EliminationBackoffStack<T> extends LockFreeStack<T> implements ConcStack<T> {

  private static class RangePolicy {
    public RangePolicy() {
      range = 1;
    }

    public int GetRange() {
      return range;
    }

    public void RegisterSuccess() {
      range = Math.min(Capacity, Math.max(range + 1, (int)((double)range * Ratio)));
    }

    public void RegisterFail() {
      range = Math.max(1, Math.min(range - 1, (int)((double)range / Ratio)));
    }

    private int range;
    private static final double Ratio = 1.25;
  }

  public EliminationBackoffStack() {
    elimination_array = new EliminationArray<>(Capacity);
  }

  @Override
  public void Push(T t) {
    RangePolicy pol = policy.get();
    SNode<T> node = new SNode<>(t);
    while (true) {
      if (TryPush(node)) {
        return;
      } else try {
        T item = elimination_array.Visit(t, pol.GetRange());
        if (item == null) {
          pol.RegisterSuccess();
          return;
        }
      } catch (TimeoutException e) {
        pol.RegisterFail();
      } finally {
        backoff.Wait();
      }
    }
  }

  @Override
  public T Pop() {
    RangePolicy pol = policy.get();
    while (true) {
      SNode<T> ret = TryPop();
      if (ret != null) {
        return ret.value;
      } else try {
        T item = elimination_array.Visit(null, pol.GetRange());
        if (item != null) {
          pol.RegisterSuccess();
          return item;
        }
      } catch (TimeoutException e) {
        pol.RegisterFail();
      } finally {
        backoff.Wait();
      }
    }
  }

  @Override
  public void Clear() {
    super.Clear();
    elimination_array = new EliminationArray<>(Capacity);
  }

  private EliminationArray<T> elimination_array;
  private static final int Capacity = 40;
  private static ThreadLocal<RangePolicy> policy = ThreadLocal.withInitial(RangePolicy::new);
}
