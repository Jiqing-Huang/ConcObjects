package PriorityQueue;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("unchecked")
public class HeapUnboundedPQ<T extends Comparable<T>> implements ConcUnboundedPQ<T> {

  public HeapUnboundedPQ(int capacity) {
    heap_lock = new ReentrantLock();
    next = Root;
    int width = CeilingPowerOfTwo(capacity + 1);
    heap = new HeapNode[width];
    for (int i = 0; i != width; ++i)
      heap[i] = new HeapNode<>();
  }

  @Override
  public void Offer(T t) {
    heap_lock.lock();
    int child = GetAndIncrement();
    heap[child].Lock();
    heap[child].Init(t);
    heap_lock.unlock();
    heap[child].Unlock();

    while (child > Root) {
      int parent = child / 2;
      heap[parent].Lock();
      heap[child].Lock();
      int old_child = child;
      try {
        if (heap[parent].tag == State.Available && heap[child].AmOwner()) {
          if (heap[child].item.compareTo(heap[parent].item) < 0) {
            Swap(child, parent);
            child = parent;
          } else {
            heap[child].tag = State.Available;
            heap[child].owner = NoOne;
            return;
          }
        } else if (heap[parent].tag == State.Empty) {
          child = 0;
        } else if (!heap[child].AmOwner()) {
          child = parent;
        }
      } finally {
        heap[old_child].Unlock();
        heap[parent].Unlock();
      }
    }
    if (child == Root) {
      heap[Root].Lock();
      if (heap[Root].AmOwner()) {
        heap[Root].tag = State.Available;
        heap[Root].owner = NoOne;
      }
      heap[Root].Unlock();
    }
  }

  @Override
  public T Poll() {
    heap_lock.lock();
    int bottom = DecrementAndGet();
    heap[Root].Lock();
    heap[bottom].Lock();
    heap_lock.unlock();
    T t = heap[Root].item;
    heap[Root].item = heap[bottom].item;
    heap[Root].tag = State.Available;
    heap[Root].owner = NoOne;
    heap[bottom].tag = State.Empty;
    heap[bottom].owner = NoOne;
    heap[bottom].Unlock();
    if (heap[Root].tag == State.Empty) {
      heap[Root].Unlock();
      return t;
    }
    int child = 0;
    int parent = Root;
    while (parent < heap.length / 2) {
      int left = parent * 2;
      int right = parent * 2 + 1;
      heap[left].Lock();
      heap[right].Lock();
      if (heap[left].tag == State.Empty) {
        heap[right].Unlock();
        heap[left].Unlock();
        break;
      } else if (heap[right].tag == State.Empty || heap[left].item.compareTo(heap[right].item) < 0) {
        heap[right].Unlock();
        child = left;
      } else {
        heap[left].Unlock();
        child = right;
      }
      if (heap[child].item.compareTo(heap[parent].item) < 0) {
        Swap(parent, child);
        heap[parent].Unlock();
        parent = child;
      } else {
        heap[child].Unlock();
        break;
      }
    }
    heap[parent].Unlock();
    return t;
  }

  private void Swap(int x, int y) {
    T temp_item = heap[x].item;
    heap[x].item = heap[y].item;
    heap[y].item = temp_item;
    State temp_tag = heap[x].tag;
    heap[x].tag = heap[y].tag;
    heap[y].tag = temp_tag;
    long temp_owner = heap[x].owner;
    heap[x].owner = heap[y].owner;
    heap[y].owner = temp_owner;
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

  private int FloorPowerOfTwo(int x) {
    return CeilingPowerOfTwo(x + 1) >> 1;
  }

  private int GetAndIncrement() {
    int x = next++;
    return ReverseBit(x);
  }

  private int DecrementAndGet() {
    int x = --next;
    return ReverseBit(x);
  }

  private int ReverseBit(int x) {
    int y = FloorPowerOfTwo(x);
    int z = x - y;
    int u = 0;
    int v = y;
    while (v > 1) {
      u <<= 1;
      u += (z & 1);
      z >>= 1;
      v >>= 1;
    }
    return u + y;
  }

  private HeapNode<T>[] heap;
  private int next;
  private Lock heap_lock;
  private static final int Root = 1;
  private static final int NoOne = -1;
}
