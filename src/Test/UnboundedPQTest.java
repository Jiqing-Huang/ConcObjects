package Test;

import PriorityQueue.ConcUnboundedPQ;
import PriorityQueue.HeapUnboundedPQ;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class UnboundedPQTest extends Thread {
  public static void main(String[] args) throws InterruptedException {
    int num_tests = 6;
    int[] num_threads = new int[] {4, 4, 4, 40, 40, 40};
    int[] num_opes = new int[] {20_000, 40_000, 80_000, 20_000, 40_000, 80_000};

    System.out.println("Testing heap based unbounded priority queue");
    for (int i = 0; i != num_tests; ++i) {
      pq = new HeapUnboundedPQ<>(num_threads[i] * num_opes[i]);
      Test(num_threads[i], num_opes[i]);
    }
    System.out.println();
  }

  private static void Test(int n_threads, int num_ope) throws InterruptedException {
    num_ope_per_thread = num_ope;
    num_threads = n_threads;
    long start_time = System.currentTimeMillis();
    UnboundedPQTest[] workers = new UnboundedPQTest[num_threads];
    for (int i = 0; i != num_threads; ++i) {
      workers[i] = new Test.UnboundedPQTest();
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
    long sum = 0;
    Random random = new Random();
    for (int i = 0; i != num_ope_per_thread; ++i) {
      int x = random.nextInt(Integer.MAX_VALUE);
      x -= x % 100;
      x += Thread.currentThread().getId();
      sum += x;
      pq.Offer(x);
    }
    for (int i = 0; i != num_ope_per_thread; ++i)
      sum -= pq.Poll();
    total_sum.addAndGet(sum);
  }


  private static ConcUnboundedPQ<Integer> pq;
  private static int num_threads;
  private static int num_ope_per_thread;
  private static AtomicLong total_sum = new AtomicLong(0);
}
