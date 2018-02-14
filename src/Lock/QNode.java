package Lock;

public class QNode {
  public QNode() {
    locked = false;
    next = null;
  }

  public volatile boolean locked;
  public volatile QNode next;
}
