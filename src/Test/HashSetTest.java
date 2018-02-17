package Test;

import HashSet.*;

import java.util.ArrayList;
import java.util.List;

public class HashSetTest extends Thread {

  public static void main(String[] args) throws InterruptedException {
    int num_tests = 6;
    int[] num_threads = new int[] {8, 8, 8, 80, 80, 80};
    int[] num_opes = new int[] {20_000, 40_000, 80_000, 20_000, 40_000, 80_000};

    System.out.println("Testing coarse hash set");
    for (int i = 0; i != num_tests; ++i) {
      set = new CoarseHashSet<>(InitCapacity);
      Test(num_threads[i], num_opes[i]);
    }
    System.out.println();

    System.out.println("Testing striped hash set");
    for (int i = 0; i != num_tests; ++i) {
      set = new StripedHashSet<>(InitCapacity);
      Test(num_threads[i], num_opes[i]);
    }
    System.out.println();

    System.out.println("Testing refinable hash set");
    for (int i = 0; i != num_tests; ++i) {
      set = new RefinableHashSet<>(InitCapacity);
      Test(num_threads[i], num_opes[i]);
    }
    System.out.println();

    System.out.println("Testing lock-free hash set");
    for (int i = 0; i != num_tests; ++i) {
      set = new LockFreeHashSet<>(InitCapacity);
      Test(num_threads[i], num_opes[i]);
    }
    System.out.println();

    System.out.println("Testing coarse cuckoo hash set");
    for (int i = 0; i != num_tests; ++i) {
      set = new CoarseCuckooHashSet<>(InitCapacity);
      Test(num_threads[i], num_opes[i]);
    }
    System.out.println();

    System.out.println("Testing striped cuckoo hash set");
    for (int i = 0; i != num_tests; ++i) {
      set = new StripedCuckooHashSet<>(InitCapacity);
      Test(num_threads[i], num_opes[i]);
    }
    System.out.println();

    System.out.println("Testing refinable cuckoo hash set");
    for (int i = 0; i != num_tests; ++i) {
      set = new RefinableCuckooHashSet<>(InitCapacity);
      Test(num_threads[i], num_opes[i]);
    }
    System.out.println();
  }

  private static void Test(int n_threads, int num_ope) throws InterruptedException {
    all_correct = true;
    num_ope_per_thread = num_ope;
    num_threads = n_threads;
    long start_time = System.currentTimeMillis();
    HashSetTest[] workers = new HashSetTest[num_threads];
    for (int i = 0; i != num_threads; ++i) {
      workers[i] = new Test.HashSetTest();
      workers[i].thread_id = i;
      workers[i].start();
    }
    for (int i = 0; i != num_threads; ++i)
      workers[i].join();
    long end_time = System.currentTimeMillis();
    double elapsed_time = (end_time - start_time) / 1000.0;
    String correctness = (all_correct)? "correct" : "incorrect";
    System.out.println("Thread number: " + num_threads + "; Number of operations: " + num_ope +
      "; Finished in " + elapsed_time + " second(s); Result is " + correctness);
  }

  @Override
  public void run() {
    correct.set(true);
    List<Integer> list = new ArrayList<>();
    for (int i = 0; i != num_ope_per_thread; ++i) {
      int item = i * 321 + thread_id;
      list.add(item);
    }
    for (Integer item: list) {
      boolean added = set.Add(item);
      if (!added) correct.set(false);
    }
    for (Integer item: list) {
      boolean contained = set.Contains(item);
      if (!contained) correct.set(false);
    }
    for (Integer item: list) {
      boolean removed = set.Remove(item);
      if (!removed) correct.set(false);
    }
    for (Integer item: list) {
      boolean contained = set.Contains(item);
      if (contained) correct.set(false);
    }
    if (!correct.get())
      all_correct = false;
  }

  private static ConcHashSet<Integer> set;
  private static int num_threads;
  private static int num_ope_per_thread;
  private int thread_id;
  private ThreadLocal<Boolean> correct = ThreadLocal.withInitial(() -> true);
  private static boolean all_correct = true;
  private static final int InitCapacity = 64;
}
