package Test;

import ConcStack.ConcStack;
import ConcStack.EliminationBackoffStack;
import ConcStack.LockFreeStack;

import java.util.Random;

public class ConcStackTest extends Thread {

  public static void main(String[] args) throws InterruptedException {
    int num_tests = 15;
    int[] num_threads = new int[] {4, 4, 4, 4, 4,
                                   40, 40, 40, 40, 40,
                                   200, 200, 200, 200, 200};
    int[] num_opes = new int[] {500_000, 1_000_000, 2_000_000, 4_000_000, 8_000_000,
                                500_000, 1_000_000, 2_000_000, 4_000_000, 8_000_000,
                                500_000, 1_000_000, 2_000_000, 4_000_000, 8_000_000};

    System.out.println("Testing lock-free stack");
    stack = new LockFreeStack<>();
    for (int i = 0; i != num_tests; ++i)
      Test(num_threads[i], num_opes[i]);
    System.out.println();

    System.out.println("Testing elimination backoff stack");
    stack = new EliminationBackoffStack<>();
    for (int i = 0; i != num_tests; ++i)
      Test(num_threads[i], num_opes[i]);
    System.out.println();
  }

  private static void Test(int num_threads, int num_ope) throws InterruptedException {
    num_ope_per_thread = num_ope;
    ConcStackTest[] workers = new ConcStackTest[num_threads];
    long start_time = System.currentTimeMillis();
    for (int i = 0; i != num_threads; ++i) {
      workers[i] = new ConcStackTest();
      workers[i].start();
    }
    for (int i = 0; i != num_threads; ++i)
      workers[i].join();
    stack.Clear();
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
      if (ope_type == 0 && count > 0) {
        stack.Pop();
        --count;
      } else {
        stack.Push(rnd.nextInt(100));
        ++count;
      }
    }
  }

  private static ConcStack<Integer> stack;
  private static int num_ope_per_thread;
  private ThreadLocal<Random> random = ThreadLocal.withInitial(Random::new);
}
