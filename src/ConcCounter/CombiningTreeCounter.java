package ConcCounter;

import java.util.Stack;

public class CombiningTreeCounter implements ConcCounter {

  public CombiningTreeCounter(int width) {
    CTNode[] nodes = new CTNode[width - 1];
    nodes[0] = new CTNode();
    for (int i = 1; i != nodes.length; ++i)
      nodes[i] = new CTNode(nodes[(i - 1) / 2]);
    leaves = new CTNode[(width + 1) / 2];
    for (int i = 0; i != leaves.length; ++i)
      leaves[i] = nodes[nodes.length - 1 - i];
  }

  @Override
  public int GetAndIncrement(int thread_id) throws InterruptedException {
    Stack<CTNode> stack = new Stack<>();
    CTNode leaf = leaves[thread_id];
    CTNode node = leaf;
    while (node.Precombine())
      node = node.Parent();
    CTNode stop = node;
    node = leaf;
    int combined = 1;
    while (node != stop) {
      combined = node.Combine(combined);
      stack.push(node);
      node = node.Parent();
    }
    int prior = stop.Op(combined);
    while (!stack.empty()) {
      node = stack.pop();
      node.Distribute(prior);
    }
    return prior;
  }

  @Override
  public int IncrementAndGet(int thread_id) throws InterruptedException {
    return GetAndIncrement(thread_id) + 1;
  }

  private CTNode[] leaves;
}
