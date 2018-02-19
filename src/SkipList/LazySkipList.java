package SkipList;

import java.util.Random;

@SuppressWarnings({"unchecked", "StatementWithEmptyBody"})
public class LazySkipList<T> implements ConcSkipList<T> {

  public LazySkipList() {
    head = new SLNode<>(Integer.MIN_VALUE, MaxLevel);
    tail = new SLNode<>(Integer.MAX_VALUE, MaxLevel);
    for (int level = 0; level <= MaxLevel; ++level)
      head.next[level] = tail;
    random = ThreadLocal.withInitial(() -> new Random(Thread.currentThread().getId()));
  }

  @Override
  public boolean Add(T t) {
    int top_level = RandomLevel();
    SLNode<T>[] preds = new SLNode[MaxLevel + 1];
    SLNode<T>[] succs = new SLNode[MaxLevel + 1];
    while (true) {
      int level = Find(t, preds, succs);
      if (level != -1) {
        SLNode<T> node = succs[level];
        if (!node.marked) {
          while (!node.fully_linked) {} // spin
          return false;
        }
        continue;
      }
      int highest_locked = -1;
      try {
        SLNode<T> pred, succ;
        boolean valid = true;
        for (int level_to_lock = 0; level_to_lock <= top_level && valid; ++level_to_lock) {
          pred = preds[level_to_lock];
          succ = succs[level_to_lock];
          pred.Lock();
          highest_locked = level_to_lock;
          valid = !pred.marked && !succ.marked && pred.next[level_to_lock] == succ;
        }
        if (!valid) continue;
        SLNode<T> node = new SLNode<>(t, top_level);
        for (int level_to_link = 0; level_to_link <= top_level; ++level_to_link)
          node.next[level_to_link] = succs[level_to_link];
        for (int level_to_link = 0; level_to_link <= top_level; ++level_to_link)
          preds[level_to_link].next[level_to_link] = node;
        node.fully_linked = true;
        return true;
      } finally {
        for (int level_to_unlock = 0; level_to_unlock <= highest_locked; ++level_to_unlock)
          preds[level_to_unlock].Unlock();
      }
    }
  }

  @Override
  public boolean Remove(T t) {
    SLNode<T> node = null;
    boolean is_marked = false;
    int top_level = -1;
    SLNode<T>[] preds = new SLNode[MaxLevel + 1];
    SLNode<T>[] succs = new SLNode[MaxLevel + 1];
    while (true) {
      int level = Find(t, preds, succs);
      if (level != -1) node = succs[level];
      if (is_marked || (level != -1 && node.fully_linked && node.top_level == level && !node.marked)) {
        if (!is_marked) {
          top_level = node.top_level;
          node.Lock();
          if (node.marked) {
            node.Unlock();
            return false;
          }
          node.marked = true;
          is_marked = true;
        }
        int highest_locked = -1;
        try {
          SLNode<T> pred, succ;
          boolean valid = true;
          for (int level_to_lock = 0; level_to_lock <= top_level && valid; ++level_to_lock) {
            pred = preds[level_to_lock];
            pred.Lock();
            highest_locked = level_to_lock;
            valid = !pred.marked && pred.next[level_to_lock] == node;
          }
          if (!valid) continue;
          for (int level_to_unlink = top_level; level_to_unlink >= 0; --level_to_unlink)
            preds[level_to_unlink].next[level_to_unlink] = node.next[level_to_unlink];
          node.Unlock();
          return true;
        } finally {
          for (int level_to_unlock = 0; level_to_unlock <= highest_locked; ++level_to_unlock)
            preds[level_to_unlock].Unlock();
        }
      } else {
        return false;
      }
    }
  }

  @Override
  public boolean Contains(T t) {
    SLNode<T>[] preds = new SLNode[MaxLevel + 1];
    SLNode<T>[] succs = new SLNode[MaxLevel + 1];
    int level = Find(t, preds, succs);
    return level != -1 && succs[level].fully_linked && !succs[level].marked;
  }

  private int Find(T t, SLNode<T>[] preds, SLNode<T>[] succs) {
    int key = t.hashCode();
    int ret = -1;
    SLNode<T> pred = head;
    for (int level = MaxLevel; level >= 0; --level) {
      SLNode<T> curr = pred.next[level];
      while (key > curr.key) {
        pred = curr;
        curr = curr.next[level];
      }
      if (ret == -1 && key == curr.key)
        ret = level;
      preds[level] = pred;
      succs[level] = curr;
    }
    return ret;
  }

  private int RandomLevel() {
    int ret = 0;
    Random rnd = random.get();
    while (rnd.nextInt(InvertProb) == 0)
      ++ret;
    return ret;
  }

  private final SLNode<T> head;
  private final SLNode<T> tail;
  private ThreadLocal<Random> random;
  private static final int MaxLevel = 32;
  private static final int InvertProb = 4;
}
