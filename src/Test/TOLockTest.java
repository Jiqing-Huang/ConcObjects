package Test;

import TimeoutLock.CLHTOLock;
import TimeoutLock.CompositeFastPathLock;
import TimeoutLock.CompositeLock;
import TimeoutLock.TOLock;

import java.util.concurrent.TimeUnit;

public class TOLockTest extends Thread {

  public static void main(String[] args) throws InterruptedException {
    TestAll(4, 100, 1_000, "Low concurrency. Low contention; Low workload.");
    TestAll(4, 100_000, 1_000, "Low concurrency. High contention. Low workload.");
    TestAll(4, 100_000, 100_000, "Low concurrency. High contention. High workload.");
    TestAll(40, 100, 1_000, "High concurrency. Low contention. High workload.");
    TestAll(40, 100_000, 1_000, "High concurrency. High contention. Low workload.");
    TestAll(40, 100_000, 100_000, "High concurrency. High contention. High workload.");
  }

  private static void TestAll(int n_threads, int n_lock_acq, int n_inner_loop, String message) throws InterruptedException {
    System.out.println(message);
    System.out.print("CLHTOLock: ");
    timeout_locker = new CLHTOLock();
    Test(n_threads, n_lock_acq, n_inner_loop);
    System.out.print("CompositeLock: ");
    timeout_locker = new CompositeLock();
    Test(n_threads, n_lock_acq, n_inner_loop);
    System.out.print("CompositeFastPathLock: ");
    timeout_locker = new CompositeFastPathLock();
    Test(n_threads, n_lock_acq, n_inner_loop);
    System.out.println();
  }

  private static void Test(int n_threads, int n_lock_acq, int n_inner_loop) throws InterruptedException {
    counter = 0;
    num_lock_acq = n_lock_acq;
    num_inner_loop = n_inner_loop;
    long start_time = System.currentTimeMillis();
    TOLockTest[] workers = new TOLockTest[n_threads];
    for (int i = 0; i != n_threads; ++i) {
      workers[i] = new TOLockTest();
      workers[i].start();
    }
    for (int i = 0; i != n_threads; ++i) {
      if (System.currentTimeMillis() - start_time > timeout) {
        System.out.println("Timeout. Abort test.");
        for (int j = 0; j != n_threads; ++j)
          workers[j].interrupt();
        for (int j = 0; j != n_threads; ++j)
          workers[j].join();
        return;
      }
      workers[i].join(timeout);
    }
    long end_time = System.currentTimeMillis();
    double elapsed_time = (end_time - start_time) / 1000.0;
    long expected_counter = (long)n_threads * (long)n_lock_acq * (long)n_inner_loop;
    String result = (counter == expected_counter)? "correct" : "incorrect";
    System.out.println("Program finished with " + result + " result in " + elapsed_time + " second(s).");
  }

  @Override
  public void run() {
    for (int i = 0; i != num_lock_acq; ++i) {
      while (!timeout_locker.TryLock(5, TimeUnit.MILLISECONDS))
        try {
          if (Thread.currentThread().isInterrupted()) return;
          Thread.sleep(50);
        } catch (InterruptedException e) {
          return;
        }
      try {
        for (int j = 0; j != num_inner_loop; ++j)
          ++counter;
      } finally {
        timeout_locker.Unlock();
      }
    }
  }

  private static final int timeout = 30000;
  private static long counter = 0;
  private static TOLock timeout_locker;
  private static int num_lock_acq = 1;
  private static int num_inner_loop = 1;
}
