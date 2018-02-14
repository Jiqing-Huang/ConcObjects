package ConcCounter;

import java.util.Stack;

public class CombiningTreeCounter<T extends Associable<T>> implements ConcCounter<T> {

  public CombiningTreeCounter(int width, T identity) {
    CTNode<T>[] nodes = (CTNode<T>[]) new CTNode[width - 1];
    nodes[0] = new CTNode<>(identity);
    for (int i = 1; i != nodes.length; ++i)
      nodes[i] = new CTNode<>(nodes[(i - 1) / 2], identity);
    leaves = (CTNode<T>[]) new CTNode[(width + 1) / 2];
    for (int i = 0; i != leaves.length; ++i)
      leaves[i] = nodes[nodes.length - 1 - i];
  }

  @Override
  public T GetAndAdd(T t, int thread_id) throws InterruptedException {
    Stack<CTNode<T>> stack = new Stack<>();
    CTNode<T> leaf = leaves[thread_id];
    CTNode<T> node = leaf;
    while (node.Precombine())
      node = node.Parent();
    CTNode<T> stop = node;
    node = leaf;
    T combined = t.Clone();
    while (node != stop) {
      combined = node.Combine(combined);
      stack.push(node);
      node = node.Parent();
    }
    T prior = stop.Op(combined);
    while (!stack.empty()) {
      node = stack.pop();
      node.Distribute(prior);
    }
    return prior;
  }

  @Override
  public T AddAndGet(T t, int thread_id) throws InterruptedException {
    T prior = GetAndAdd(t, thread_id);
    prior.Aggregate(t);
    return prior;
  }

  private CTNode<T>[] leaves;
}
