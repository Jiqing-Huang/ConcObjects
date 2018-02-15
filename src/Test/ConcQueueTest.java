package Test;

import Queue.BoundedQueue;
import Queue.ConcQueue;
import Queue.LockFreeQueue;
import Queue.UnboundedQueue;

import java.util.Random;

public class ConcQueueTest extends Thread {

  public static void main(String[] args) throws InterruptedException {
    int num_tests = 6;
    int[] num_threads = new int[] {4, 40, 4, 4, 4, 4};
    int[] num_opes = new int[] {500_000, 500_000, 1_000_000, 2_000_000, 4_000_000, 8_000_000};

    capacity = 2000;
    System.out.println("Testing bounded queue with capacity of " + capacity);
    queue = new BoundedQueue<>(capacity);
    for (int i = 0; i != num_tests; ++i)
      Test(num_threads[i], num_opes[i]);
    System.out.println();

    capacity = Integer.MAX_VALUE;
    System.out.println("Testing unbounded queue");
    queue = new UnboundedQueue<>();
    for (int i = 0; i != num_tests; ++i)
      Test(num_threads[i], num_opes[i]);
    System.out.println();

    capacity = Integer.MAX_VALUE;
    System.out.println("Testing lock-free queue");
    queue = new LockFreeQueue<>();
    for (int i = 0; i != num_tests; ++i)
      Test(num_threads[i], num_opes[i]);
    System.out.println();
  }

  private static void Test(int n_threads, int num_ope) throws InterruptedException {
    num_threads = n_threads;
    num_ope_per_thread = num_ope;
    ConcQueueTest[] workers = new ConcQueueTest[num_threads];
    long start_time = System.currentTimeMillis();
    for (int i = 0; i != num_threads; ++i) {
      workers[i] = new ConcQueueTest();
      workers[i].start();
    }
    for (int i = 0; i != num_threads; ++i)
      workers[i].join();
    queue.Clear();
    long end_time = System.currentTimeMillis();
    double elapsed_time = (end_time - start_time) / 1000.0;
    System.out.println("Thread number: " + num_threads + "; Number of operations: " + num_ope +
      "; Finished in " + elapsed_time + " second(s).");
  }

  @Override
  public void run() {
    Random rnd = random.get();
    rnd.setSeed(Thread.currentThread().getId());
    int count = 0;
    for (int i = 0; i != num_ope_per_thread; ++i) {
      int ope_type = rnd.nextInt(2);
      if ((ope_type == 0 && count > 0) || count >= capacity / num_threads) {
        try {
          queue.Poll();
        } catch (InterruptedException e) {
          return;
        }
        --count;
      } else {
        try {
          queue.Offer(i);
        } catch (InterruptedException e) {
          return;
        }
        ++count;
      }
    }
  }

  private static ConcQueue<Integer> queue;
  private static int capacity;
  private static int num_threads;
  private static int num_ope_per_thread;
  private ThreadLocal<Random> random = ThreadLocal.withInitial(Random::new);
}
