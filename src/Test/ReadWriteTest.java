package Test;

import ReadWriteLock.FifoReadWriteLock;
import ReadWriteLock.ReadWriteLock;

import java.util.concurrent.atomic.AtomicInteger;

public class ReadWriteTest extends Thread {

  public ReadWriteTest(boolean is_reader) {
    this.is_reader = is_reader;
  }

  public static void main(String[] args) throws InterruptedException {
    int num_readers = 40;
    num_reads_per_thread = 100000;
    num_writes_per_thread = 1000;
    lock = new FifoReadWriteLock();
    MRSWTest(num_readers);
  }

  private static void MRSWTest(int num_readers) throws InterruptedException {
    long start_time = System.currentTimeMillis();
    ReadWriteTest[] readers = new ReadWriteTest[num_readers];
    for (int i = 0; i != num_readers; ++i) {
      readers[i] = new ReadWriteTest(true);
      readers[i].start();
    }
    ReadWriteTest writer = new ReadWriteTest(false);
    writer.start();
    for (int i = 0; i != num_readers; ++i)
      readers[i].join();
    writer.join();
    long end_time = System.currentTimeMillis();
    double elapsed_time = (end_time - start_time) / 1000.0;
    double mean = sum.get() / num_readers;
    System.out.println("Program finished in " + elapsed_time + " second(s).");
    System.out.println("Reader finished at " + mean + "/" + num_writes_per_thread + " of writer loops on average.");
  }

  @Override
  public void run() {
    if (is_reader) {
      for (int i = 0; i != num_reads_per_thread; ++i) {
        lock.ReadLock().lock();
        store.set(Math.max(store.get(), counter));
        lock.ReadLock().unlock();
      }
      sum.set(sum.get() + store.get());
    } else {
      for (int i = 0; i != num_writes_per_thread; ++i) {
        lock.WriteLock().lock();
        try {
          ++counter;
        } finally {
          lock.WriteLock().unlock();
        }
      }
    }
  }

  private static int counter = 0;
  private static int num_reads_per_thread = 0;
  private static int num_writes_per_thread = 0;
  private static ReadWriteLock lock;
  private static ThreadLocal<Integer> store = ThreadLocal.withInitial(() -> 0);
  private static AtomicInteger sum = new AtomicInteger(0);
  private boolean is_reader;
}
