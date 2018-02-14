package Test;

import LinkedListSet.*;

import java.util.Random;

public class LinkedListSetTest extends Thread {

  public static void main(String[] args) throws InterruptedException {
    int num_tests = 6;
    int[] num_threads = new int[] {4, 8, 8, 8, 8, 8};
    int[] num_opes = new int[] {1000, 1000, 5000, 10000, 20000, 30000};
    System.out.println("Testing coarse list");
    set = new CoarseList<>();
    for (int i = 0; i != num_tests; ++i)
      Test(num_threads[i], num_opes[i]);
    System.out.println();

    System.out.println("Testing fine list");
    set = new FineList<>();
    for (int i = 0; i != num_tests; ++i)
      Test(num_threads[i], num_opes[i]);
    System.out.println();

    System.out.println("Optimistic list");
    set = new OptimisticList<>();
    for (int i = 0; i != num_tests; ++i)
      Test(num_threads[i], num_opes[i]);
    System.out.println();

    System.out.println("Lazy list");
    set = new LazyList<>();
    for (int i = 0; i != num_tests; ++i)
      Test(num_threads[i], num_opes[i]);
    System.out.println();

    System.out.println("Lock-free list");
    set = new LockFreeList<>();
    for (int i = 0; i != num_tests; ++i)
      Test(num_threads[i], num_opes[i]);
    System.out.println();
  }

  private static void Test(int num_threads, int num_ope) throws InterruptedException {
    LinkedListSetTest[] workers = new LinkedListSetTest[num_threads];
    num_ope_per_thread = num_ope;
    upper_bound = num_threads * num_ope / 10;
    long start_time = System.currentTimeMillis();
    for (int i = 0; i != num_threads; ++i) {
      workers[i] = new LinkedListSetTest();
      workers[i].start();
    }
    for (int i = 0; i != num_threads; ++i)
      workers[i].join();
    long end_time = System.currentTimeMillis();
    double elapsed_time = (end_time - start_time) / 1000.0;
    System.out.println("Thread number: " + num_threads + "; Number of operations: " + num_ope +
                       "; Finished in " + elapsed_time + " second(s).");
  }

  @Override
  public void run() {
    Random rnd = random.get();
    rnd.setSeed(Thread.currentThread().getId());
    for (int i = 0; i != num_ope_per_thread; ++i) {
      int ope_type = rnd.nextInt(3);
      int item = rnd.nextInt(upper_bound);
      if (ope_type == 0) {
        set.Add(item);
      } else if (ope_type == 1) {
        set.Remove(item);
      } else {
        set.Contains(item);
      }
    }
  }

  private ThreadLocal<Random> random = ThreadLocal.withInitial(Random::new);
  private static LinkedListSet<Integer> set;
  private static int num_ope_per_thread;
  private static int upper_bound;
}
