package HashSet;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseHashSet<T> implements ConcHashSet<T> {
  public BaseHashSet(int capacity) {
    table = (List<T>[]) new List[capacity];
    for (int i = 0; i != capacity; ++i)
      table[i] = new ArrayList<>();
    size = 0;
  }

  public boolean Contains(T t) {
    Acquire(t);
    try {
      int bucket_id = t.hashCode() % table.length;
      return table[bucket_id].contains(t);
    } finally {
      Release(t);
    }
  }

  public boolean Add(T t) {
    boolean was_not_in_set;
    Acquire(t);
    try {
      int bucket_id = t.hashCode() % table.length;
      was_not_in_set = !table[bucket_id].contains(t);
      if (was_not_in_set) {
        table[bucket_id].add(t);
        ++size;
      }
    } finally {
      Release(t);
    }
    if (Policy())
      Resize(table.length);
    return was_not_in_set;
  }

  public boolean Remove(T t) {
    int index;
    Acquire(t);
    try {
      int bucket_id = t.hashCode() % table.length;
      index = table[bucket_id].indexOf(t);
      if (index != -1) {
        table[bucket_id].set(index, table[bucket_id].get(table[bucket_id].size() - 1));
        table[bucket_id].remove(table[bucket_id].size() - 1);
        --size;
      }
    } finally {
      Release(t);
    }
    if (Policy())
      Resize(table.length);
    return index != -1;
  }

  protected boolean Resize(int old_capacity) {
    if (old_capacity != table.length) return false;
    int new_capacity = old_capacity * 2;
    List<T> [] new_table = (List<T>[]) new List[new_capacity];
    for (int i = 0; i != new_capacity; ++i)
      new_table[i] = new ArrayList<>();
    for (List<T> bucket: table)
      for (T t: bucket)
        new_table[t.hashCode() % new_capacity].add(t);
    table = new_table;
    return true;
  }

  protected boolean Policy() {
    return size / table.length > 4;
  }

  protected abstract void Acquire(T t);
  protected abstract void Release(T t);

  protected List<T>[] table;
  protected int size;
}
