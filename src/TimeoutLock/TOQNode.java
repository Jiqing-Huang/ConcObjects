package TimeoutLock;

public class TOQNode {
  public TOQNode() {
    pred = null;
  }

  public volatile TOQNode pred;
}
