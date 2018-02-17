package HashSet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class LockFreeBucketList<T> {

  LockFreeBucketList(int capacity) {
    InitialCapacity = capacity;
    bucket_lists = new ArrayList<>();
    for (int i = 0; i != 32; ++i)
      bucket_lists.add(new AtomicReference<>(null));
    bucket_lists.get(0).set(new BucketList[capacity]);
  }

  public BucketList<T> Get(int bucket_id) {
    int[] loc = Find(bucket_id);
    return bucket_lists.get(loc[0]).get()[loc[1]];
  }

  public void Set(int bucket_id, BucketList<T> bucket) {
    int[] loc = Find(bucket_id);
    bucket_lists.get(loc[0]).get()[loc[1]] = bucket;
  }

  private int[] Find(int bucket_id) {
    int line_id, index, line_size;
    if (bucket_id < InitialCapacity) {
      line_id = 0;
      index = bucket_id;
      line_size = InitialCapacity;
    } else {
      line_id = 1;
      index = bucket_id - InitialCapacity;
      line_size = InitialCapacity;
      while (index >= line_size) {
        index -= line_size;
        line_size *= 2;
        ++line_id;
      }
    }
    if (bucket_lists.get(line_id).get() == null) {
      BucketList<T>[] new_list = new BucketList[line_size];
      bucket_lists.get(line_id).compareAndSet(null, new_list);
    }
    return new int[] {line_id, index};
  }

  private final int InitialCapacity;
  private List<AtomicReference<BucketList<T>[]>> bucket_lists;
}
