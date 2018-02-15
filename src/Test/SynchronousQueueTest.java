package Test;

import Queue.ConcQueue;
import Queue.SynchronousQueue;
import Queue.SynchronousDualQueue;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("Duplicates")
public class SynchronousQueueTest extends Thread {
  public static void main(String[] args) throws InterruptedException {
    int num_tests = 6;
    int[] num_threads = new int[] {4, 40, 4, 4, 4, 4};
    int[] num_opes = new int[] {50_000, 50_000, 100_000, 200_000, 400_000, 800_000};

    System.out.println("Testing synchronous queue");
    queue = new SynchronousQueue<>();
    for (int i = 0; i != num_tests; ++i)
      Test(num_threads[i], num_opes[i]);
    System.out.println();


    System.out.println("Testing synchronous dual queue");
    queue = new SynchronousDualQueue<>();
    for (int i = 0; i != num_tests; ++i)
      Test(num_threads[i], num_opes[i]);
    System.out.println();
  }

  private static void Test(int num_threads, int num_ope) throws InterruptedException {
    num_ope_per_thread = num_ope;
    put_all.set(0);
    get_all.set(0);
    System.out.print("Thread number: " + num_threads + "; Number of operations: " + num_ope + "; ");
    SynchronousQueueTest[] workers = new SynchronousQueueTest[num_threads];
    long start_time = System.currentTimeMillis();
    int half = num_threads / 2;
    for (int i = 0; i != half; ++i) {
      workers[i] = new SynchronousQueueTest();
      workers[i].is_enqueuer = true;
      workers[i].start();
    }
    for (int i = half; i != num_threads; ++i) {
      workers[i] = new SynchronousQueueTest();
      workers[i].is_enqueuer = false;
      workers[i].start();
    }
    for (int i = 0; i != num_threads; ++i) {
      if (System.currentTimeMillis() - start_time > timeout) {
        System.out.println("Timeout. Abort test.");
        for (int j = 0; j != num_threads; ++j) {
          workers[j].interrupt();
          workers[j].join();
        }
        queue.Clear();
        return;
      }
      workers[i].join(timeout);
    }
    queue.Clear();
    long end_time = System.currentTimeMillis();
    double elapsed_time = (end_time - start_time) / 1000.0;
    String correct = (put_all.get() == get_all.get())? "correct" : "incorrect";
    System.out.println("Finished in " + elapsed_time + " second(s); Result is " + correct + ".");
  }

  @Override
  public void run() {
    put.set(0);
    get.set(0);
    Random rnd = random.get();
    for (int i = 0; i != num_ope_per_thread; ++i)
      if (is_enqueuer) {
        int value = rnd.nextInt(100);
        put.set(put.get() + value);
        try {
          queue.Offer(value);
        } catch (InterruptedException e) {
          return;
        }
      } else {
        int value = 0;
        try {
          value = queue.Poll();
        } catch (InterruptedException e) {
          return;
        }
        get.set(get.get() + value);
      }
    put_all.addAndGet(put.get());
    get_all.addAndGet(get.get());
  }

  private static ConcQueue<Integer> queue;
  private static int num_ope_per_thread;
  private static int timeout = 30000;
  private boolean is_enqueuer = false;
  private ThreadLocal<Random> random = ThreadLocal.withInitial(Random::new);
  private ThreadLocal<Integer> put = ThreadLocal.withInitial(() -> 0);
  private ThreadLocal<Integer> get = ThreadLocal.withInitial(() -> 0);
  private static AtomicInteger put_all = new AtomicInteger(0);
  private static AtomicInteger get_all = new AtomicInteger(0);
}
