package ReadWriteLock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FifoReadWriteLock implements ReadWriteLock {

  class ReadLock implements Lock {

    @Override
    public void lock() {
      lock.lock();
      try {
        while (writer) condition.await();
        ++readers_acq;
      } catch (InterruptedException e) {
        e.printStackTrace();
      } finally {
        lock.unlock();
      }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean tryLock() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean tryLock(long l, TimeUnit timeUnit) throws InterruptedException {
      throw new UnsupportedOperationException();
    }

    @Override
    public void unlock() {
      lock.lock();
      try {
        ++readers_rel;
        if (readers_acq == readers_rel) condition.signalAll();
      } finally {
        lock.unlock();
      }
    }

    @Override
    public Condition newCondition() {
      throw new UnsupportedOperationException();
    }
  }

  class WriteLock implements Lock {

    @Override
    public void lock() {
      lock.lock();
      try {
        while (writer) condition.await();
        writer = true;
        while (readers_acq != readers_rel) condition.await();
      } catch (InterruptedException e) {
        e.printStackTrace();
      } finally {
        lock.unlock();
      }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean tryLock() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean tryLock(long l, TimeUnit timeUnit) throws InterruptedException {
      throw new UnsupportedOperationException();
    }

    @Override
    public void unlock() {
      lock.lock();
      try {
        writer = false;
        condition.signalAll();
      } finally {
        lock.unlock();
      }
    }

    @Override
    public Condition newCondition() {
      throw new UnsupportedOperationException();
    }
  }

  public FifoReadWriteLock() {
    readers_acq = 0;
    readers_rel = 0;
    writer = false;
    lock = new ReentrantLock();
    read_lock = new FifoReadWriteLock.ReadLock();
    write_lock = new FifoReadWriteLock.WriteLock();
    condition = lock.newCondition();
  }

  @Override
  public Lock ReadLock() {
    return read_lock;
  }

  @Override
  public Lock WriteLock() {
    return write_lock;
  }

  private int readers_acq;
  private int readers_rel;
  private boolean writer;
  private Lock lock;
  private Lock read_lock;
  private Lock write_lock;
  private Condition condition;
}
