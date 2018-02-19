package Test;

import SkipList.ConcSkipList;
import SkipList.LazySkipList;
import SkipList.LockFreeSkipList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SkipListTest extends Thread {

  public static void main(String[] args) throws InterruptedException {
    int num_tests = 6;
    int[] num_threads = new int[] {8, 8, 8, 80, 80, 80};
    int[] num_opes = new int[] {20_000, 40_000, 80_000, 20_000, 40_000, 80_000};

    System.out.println("Testing lazy skip list");
    for (int i = 0; i != num_tests; ++i) {
      skip_list = new LazySkipList<>();
      Test(num_threads[i], num_opes[i]);
    }
    System.out.println();

    System.out.println("Testing lock-free skip list");
    for (int i = 0; i != num_tests; ++i) {
      skip_list = new LockFreeSkipList<>();
      Test(num_threads[i], num_opes[i]);
    }
    System.out.println();
  }

  private static void Test(int n_threads, int num_ope) throws InterruptedException {
    all_correct = true;
    num_ope_per_thread = num_ope;
    num_threads = n_threads;
    long start_time = System.currentTimeMillis();
    SkipListTest[] workers = new SkipListTest[num_threads];
    for (int i = 0; i != num_threads; ++i) {
      workers[i] = new Test.SkipListTest();
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
    Collections.shuffle(list);
    for (Integer item: list) {
      boolean added = skip_list.Add(item);
      if (!added) correct.set(false);
    }
    for (Integer item: list) {
      boolean contained = skip_list.Contains(item);
      if (!contained) correct.set(false);
    }
    for (Integer item: list) {
      boolean removed = skip_list.Remove(item);
      if (!removed) correct.set(false);
    }
    for (Integer item: list) {
      boolean contained = skip_list.Contains(item);
      if (contained) correct.set(false);
    }
    if (!correct.get())
      all_correct = false;
  }

  private static ConcSkipList<Integer> skip_list;
  private static int num_threads;
  private static int num_ope_per_thread;
  private int thread_id;
  private ThreadLocal<Boolean> correct = ThreadLocal.withInitial(() -> true);
  private static boolean all_correct = true;
}
