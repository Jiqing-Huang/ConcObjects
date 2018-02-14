package Test;

import ConcCounter.Associable;
import ConcCounter.CombiningTreeCounter;
import ConcCounter.ConcCounter;
import ConcCounter.SimpleCounter;

public class ConcCounterTest extends Thread {

  private static class IntegerWrapper implements Associable<IntegerWrapper> {

    IntegerWrapper(int value) {
      this.value = value;
    }

    @Override
    public IntegerWrapper Clone() {
      return new IntegerWrapper(value);
    }

    @Override
    public void Set(IntegerWrapper rhs) {
      value = rhs.value;
    }

    @Override
    public void Aggregate(IntegerWrapper rhs) {
      value += rhs.value;
    }

    private int value;
  }

  public static void main(String[] args) throws InterruptedException {
    int num_tests = 9;
    int[] num_threads = new int[] {4, 4, 4, 40, 40, 40, 400, 400, 400};
    int[] num_opes = new int[] {50_000, 100_000, 200_000,
                                50_000, 100_000, 200_000,
                                50_000, 100_000, 200_000};

    System.out.println("Testing simple counter");
    for (int i = 0; i != num_tests; ++i) {
      counter = new SimpleCounter<>(new IntegerWrapper(0));
      Test(num_threads[i], num_opes[i]);
    }
    System.out.println();

    System.out.println("Testing combining tree counter");
    for (int i = 0; i != num_tests; ++i) {
      counter = new CombiningTreeCounter<>(num_threads[i] * 2, new IntegerWrapper(0));
      Test(num_threads[i], num_opes[i]);
    }
    System.out.println();
  }

  private static void Test(int num_threads, int num_ope) throws InterruptedException {
    num_ope_per_thread = num_ope;
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
    System.out.println("Thread number: " + num_threads + "; Number of operations: " + num_ope +
      "; Finished in " + elapsed_time + " second(s).");
  }

  @Override
  public void run() {
    IntegerWrapper one = new IntegerWrapper(1);
    for (int i = 0; i != num_ope_per_thread; ++i) {
      try {
        counter.GetAndAdd(one, thread_id);
      } catch (InterruptedException e) {
        return;
      }
    }
  }

  private static ConcCounter<IntegerWrapper> counter;
  private static int num_ope_per_thread;
  private int thread_id;
}