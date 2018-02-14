package Test;

import ConcCounter.BitonicCounter;
import ConcCounter.CombiningTreeCounter;
import ConcCounter.ConcCounter;
import ConcCounter.SimpleCounter;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ConcCounterTest extends Thread {

  public static void main(String[] args) throws InterruptedException {
    int num_tests = 9;
    int[] num_threads = new int[] {4, 4, 4, 40, 40, 40, 400, 400, 400};
    int[] num_opes = new int[] {25_000, 50_000, 100_000,
                                25_000, 50_000, 100_000,
                                25_000, 50_000, 100_000};

    System.out.println("Testing simple counter");
    for (int i = 0; i != num_tests; ++i) {
      counter = new SimpleCounter();
      Test(num_threads[i], num_opes[i]);
    }
    System.out.println();

    System.out.println("Testing combining tree counter");
    for (int i = 0; i != num_tests; ++i) {
      counter = new CombiningTreeCounter(num_threads[i] * 2);
      Test(num_threads[i], num_opes[i]);
    }
    System.out.println();

    System.out.println("Testing bitonic counter");
    for (int i = 0; i != num_tests; ++i) {
      counter = new BitonicCounter(num_threads[i]);
      Test(num_threads[i], num_opes[i]);
    }
    System.out.println();
  }

  private static void Test(int num_threads, int num_ope) throws InterruptedException {
    num_ope_per_thread = num_ope;
    total_sum.set(0L);
    ConcCounterTest[] workers = new ConcCounterTest[num_threads];
    long start_time = System.currentTimeMillis();
    for (int i = 0; i != num_threads; ++i) {
      workers[i] = new ConcCounterTest();
      workers[i].thread_id = i;
      workers[i].start();
    }
    for (int i = 0; i != num_threads; ++i)
      workers[i].join();
    long end_time = System.currentTimeMillis();
    double elapsed_time = (end_time - start_time) / 1000.0;
    long n = (long)num_threads * (long)num_ope;
    long result = n * (n - 1) / 2;
    String correct = (result == total_sum.get())? "correct" : "incorrect";
    System.out.println("Thread number: " + num_threads + "; Number of operations: " + num_ope +
      "; Finished in " + elapsed_time + " second(s); Result is " + correct + ".");
  }

  @Override
  public void run() {
    sum.set(0L);
    for (int i = 0; i != num_ope_per_thread; ++i) {
      try {
        sum.set(sum.get() + (long)counter.GetAndIncrement(thread_id));
      } catch (InterruptedException e) {
        return;
      }
    }
    total_sum.getAndAdd(sum.get());
  }

  private static ConcCounter counter;
  private static int num_ope_per_thread;
  private int thread_id;
  private ThreadLocal<Long> sum = ThreadLocal.withInitial(() -> 0L);
  private static AtomicLong total_sum = new AtomicLong(0L);
}