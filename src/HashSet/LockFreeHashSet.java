package HashSet;

import java.util.concurrent.atomic.AtomicInteger;

public class LockFreeHashSet<T> implements ConcHashSet<T> {

  public LockFreeHashSet(int capacity) {
    buckets = new LockFreeBucketList<>(capacity);
    buckets.Set(0, new BucketList<>());
    bucket_size = new AtomicInteger(capacity);
    set_size = new AtomicInteger(0);
  }

  @Override
  public boolean Add(T t) {
    int bucket_id = t.hashCode() % bucket_size.get();
    BucketList<T> bucket = GetBucketList(bucket_id);
    if (!bucket.Add(t)) return false;
    int new_set_size = set_size.incrementAndGet();
    int old_bucket_size = bucket_size.get();
    if (new_set_size > 4 * old_bucket_size)
      bucket_size.compareAndSet(old_bucket_size, 2 * old_bucket_size);
    return true;
  }

  @Override
  public boolean Remove(T t) {
    int bucket_id = t.hashCode() % bucket_size.get();
    BucketList<T> bucket = GetBucketList(bucket_id);
    return bucket.Remove(t);
  }

  @Override
  public boolean Contains(T t) {
    int bucket_id = t.hashCode() % bucket_size.get();
    BucketList<T> bucket = GetBucketList(bucket_id);
    return bucket.Contains(t);
  }

  private BucketList<T> GetBucketList(int bucket_id) {
    if (buckets.Get(bucket_id) == null)
      InitializeBucket(bucket_id);
    return buckets.Get(bucket_id);
  }

  private void InitializeBucket(int bucket_id) {
    int parent_id = GetParent(bucket_id);
    if (buckets.Get(parent_id) == null)
      InitializeBucket(parent_id);
    BucketList<T> bucket = buckets.Get(parent_id).GetSenital(bucket_id);
    if (bucket != null)
      buckets.Set(bucket_id, bucket);
  }

  private int GetParent(int bucket_id) {
    int parent_id = bucket_size.get();
    while (parent_id > bucket_id)
      parent_id >>= 1;
    return bucket_id - parent_id;
  }

  private LockFreeBucketList<T> buckets;
  private AtomicInteger bucket_size;
  private AtomicInteger set_size;
}
