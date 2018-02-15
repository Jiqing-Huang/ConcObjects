package Queue;

import java.util.concurrent.atomic.AtomicReference;

enum NodeType {Item, Reservation};

public class SDQNode<T> {

  public SDQNode(T t, NodeType node_type) {
    type = node_type;
    item = new AtomicReference<>(t);
    next = new AtomicReference<>(null);
  }

  public volatile NodeType type;
  public volatile AtomicReference<T> item;
  public volatile AtomicReference<SDQNode<T>> next;
}
