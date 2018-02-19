package Test;

import PriorityQueue.ArrayBoundedPQ;
import PriorityQueue.ConcBoundedPQ;
import PriorityQueue.Rankable;
import PriorityQueue.TreeBoundedPQ;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class BoundedPQTest extends Thread {
  public static void main(String[] args) throws InterruptedException {
    int num_tests = 6;
    int[] num_threads = new int[] {8, 8, 8, 80, 80, 80};
    int[] num_opes = new int[] {50_000, 100_000, 200_000, 50_000, 100_000, 200_000};

    System.out.println("Testing array based bounded priority queue");
    for (int i = 0; i != num_tests; ++i) {
      pq = new ArrayBoundedPQ<>(range);
      Test(num_threads[i], num_opes[i]);
    }
    System.out.println();

    System.out.println("Testing tree based bounded priority queue");
    for (int i = 0; i != num_tests; ++i) {
      pq = new TreeBoundedPQ<>(range);
      Test(num_threads[i], num_opes[i]);
    }
    System.out.println();
  }

  private static void Test(int n_threads, int num_ope) throws InterruptedException {
    num_ope_per_thread = num_ope;
    num_threads = n_threads;
    long start_time = System.currentTimeMillis();
    BoundedPQTest[] workers = new BoundedPQTest[num_threads];
    for (int i = 0; i != num_threads; ++i) {
      workers[i] = new Test.BoundedPQTest();
      workers[i].start();
    }
    for (int i = 0; i != num_threads; ++i)
      workers[i].join();
    long end_time = System.currentTimeMillis();
    double elapsed_time = (end_time - start_time) / 1000.0;
    String correctness = (total_sum.get() == 0)? "correct" : "incorrect";
    System.out.println("Thread number: " + num_threads + "; Number of operations: " + num_ope +
      "; Finished in " + elapsed_time + " second(s); Result is " + correctness);
  }

  @Override
  public void run() {
    int sum = 0;
    Random random = new Random();
    for (int i = 0; i != num_ope_per_thread; ++i) {
      int x = random.nextInt(range);
      sum += x;
      pq.Offer(new IntegerWrapper(x));
    }
    for (int i = 0; i != num_ope_per_thread; ++i)
      sum -= pq.Poll().Get();
    total_sum.addAndGet(sum);
  }

  private static class IntegerWrapper implements Rankable {

    public IntegerWrapper(int x) {
      value = x;
    }

    public int Get() {
      return value;
    }

    public void Set(int x) {
      value = x;
    }

    @Override
    public int Rank() {
      return value;
    }

    private int value;
  }

  private static ConcBoundedPQ<IntegerWrapper> pq;
  private static int num_threads;
  private static int num_ope_per_thread;
  private static int range = 64;
  private static AtomicInteger total_sum = new AtomicInteger(0);
}
