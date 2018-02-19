package PriorityQueue;

@SuppressWarnings({"unchecked", "Duplicates"})
public class TreeBoundedPQ<T extends Rankable> implements ConcBoundedPQ<T> {

  public TreeBoundedPQ(int range) {
    int tree_width = CeilingPowerOfTwo(range);
    leaves = new TreeNode[tree_width];
    for (int i = 0; i != tree_width; ++i)
      leaves[i] = new TreeNode<T>(true);
    root = BuildTree(leaves);
  }

  @Override
  public void Offer(T t) {
    int rank = t.Rank();
    leaves[rank].bin.Push(t);
    TreeNode<T> node = leaves[rank];
    while (node != root) {
      TreeNode<T> parent = node.parent;
      if (parent.left == node)
        parent.counter.getAndIncrement();
      node = parent;
    }
  }

  @Override
  public T Poll() {
    TreeNode<T> node = root;
    while (!node.IsLeaf())
      if (node.counter.get() > 0) {
        int count = node.counter.getAndDecrement();
        if (count > 0) {
          node = node.left;
        } else {
          node.counter.getAndIncrement();
          node = node.right;
        }
      } else {
        node = node.right;
      }
    return node.bin.Pop();
  }

  private int CeilingPowerOfTwo(int x) {
    --x;
    x |= x >> 1;
    x |= x >> 2;
    x |= x >> 4;
    x |= x >> 8;
    x |= x >> 16;
    return x + 1;
  }

  private TreeNode<T> BuildTree(TreeNode<T>[] leaves) {
    if (leaves.length == 1) return leaves[0];
    TreeNode<T>[] list = new TreeNode[leaves.length / 2];
    for (int i = 0; i != leaves.length / 2; ++i) {
      list[i] = new TreeNode<>(false);
      list[i].left = leaves[i * 2];
      list[i].right = leaves[i * 2 + 1];
      leaves[i * 2].parent = list[i];
      leaves[i * 2 + 1].parent = list[i];
    }
    return BuildTree(list);
  }



  private TreeNode[] leaves;
  private TreeNode root;
}
