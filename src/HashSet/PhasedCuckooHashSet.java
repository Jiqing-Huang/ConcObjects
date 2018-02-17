package HashSet;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unchecked", "NonAtomicOperationOnVolatileField"})
public abstract class PhasedCuckooHashSet<T> implements ConcHashSet<T> {

  public PhasedCuckooHashSet(int capacity) {
    this.capacity = capacity;
    table = (List<T>[][]) new ArrayList[2][capacity];
    for (int i = 0; i != 2; ++i)
      for (int j = 0; j != capacity; ++j)
        table[i][j] = new ArrayList<>(ProbeSize);
  }

  @Override
  public boolean Add(T t) {
    Acquire(t);
    int hash = Hash(t) % capacity;
    int hash_alt = HashAlt(t) % capacity;
    int table_id = -1;
    int bucket_id = -1;
    T victim = null;
    boolean to_resize = false;
    try {
      List<T> set_zero = table[0][hash];
      List<T> set_one = table[1][hash_alt];
      if (set_zero.contains(t) || set_one.contains(t))
        return false;
      if (set_zero.size() < Threshold) {
        set_zero.add(t);
        return true;
      } else if (set_one.size() < Threshold) {
        set_one.add(t);
        return true;
      } else if (set_zero.size() < ProbeSize) {
        set_zero.add(t);
        table_id = 0;
        bucket_id = hash;
        victim = set_zero.get(0);
      } else if (set_one.size() < ProbeSize) {
        set_one.add(t);
        table_id = 1;
        bucket_id = hash_alt;
        victim = set_one.get(0);
      } else {
        to_resize = true;
      }
    } finally {
      Release(t);
    }
    if (to_resize) {
      Resize(capacity);
      Add(t);
    } else if (!Relocate(table_id, bucket_id, victim)) {
      Resize(capacity);
    }
    return true;
  }

  @Override
  public boolean Remove(T t) {
    Acquire(t);
    try {
      List<T> set_zero = table[0][Hash(t) % capacity];
      if (set_zero.contains(t)) {
        set_zero.remove(t);
        return true;
      }
      List<T> set_one = table[1][HashAlt(t) % capacity];
      if (set_one.contains(t)) {
        set_one.remove(t);
        return true;
      }
      return false;
    } finally {
      Release(t);
    }
  }

  @Override
  public boolean Contains(T t) {
    Acquire(t);
    try {
      List<T> set_zero = table[0][Hash(t) % capacity];
      if (set_zero.contains(t))
        return true;
      List<T> set_one = table[1][HashAlt(t) % capacity];
      return set_one.contains(t);
    } finally {
      Release(t);
    }
  }

  protected int Hash(T t) {
    return t.hashCode();
  }

  protected int HashAlt(T t) {
    int x = t.hashCode();
    x = ((x >>> 16) ^ x) * 0x119de1f3;
    x = ((x >>> 16) ^ x) * 0x119de1f3;
    x = (x >>> 16) ^ x;
    return (x == Integer.MIN_VALUE)? Integer.MAX_VALUE : Math.abs(x);
  }

  protected boolean Relocate(int table_id, int bucket_id, T t) {
    for (int round = 0; round != RelocateLimit; ++round) {
      List<T> set_from = table[table_id][bucket_id];
      int hash = (table_id == 0)? HashAlt(t) % capacity : Hash(t) % capacity;
      Acquire(t);
      List<T> set_to = table[1 - table_id][hash];
      try {
        if (set_from == null) return true;
        if (set_from.remove(t)) {
          if (set_to.size() < Threshold) {
            set_to.add(t);
            return true;
          } else if (set_to.size() < ProbeSize) {
            set_to.add(t);
            table_id = 1 - table_id;
            bucket_id = hash;
          } else {
            set_from.add(t);
            return false;
          }
        } else if (set_from.size() < Threshold) {
          return true;
        }
      } finally {
        Release(t);
      }
    }
    return false;
  }

  protected boolean Resize(int old_capcacity) {
    if (old_capcacity != capacity) return false;
    capacity = capacity * 2;
    List<T>[][] old_table = table;
    table = new List[2][capacity];
    for (int i = 0; i != 2; ++i)
      for (int j = 0; j != capacity; ++j)
        table[i][j] = new ArrayList<>(ProbeSize);
    for (int i = 0; i != 2; ++i)
      for (int j = 0; j != old_capcacity; ++j)
        for (T t: old_table[i][j])
          Add(t);
    return true;
  }

  protected abstract void Acquire(T t);
  protected abstract void Release(T t);

  protected volatile int capacity;
  private volatile List<T>[][] table;
  private static final int ProbeSize = 4;
  private static final int Threshold = 2;
  private static final int RelocateLimit = 4;
}
