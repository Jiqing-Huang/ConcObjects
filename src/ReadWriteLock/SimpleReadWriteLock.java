package ReadWriteLock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SimpleReadWriteLock implements ReadWriteLock {

  class ReadLock implements Lock {

    @Override
    public void lock() {
      lock.lock();
      try {
        while (writer) condition.await();
        ++readers;
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
        --readers;
        if (readers == 0) condition.signalAll();
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
        while (readers > 0 || writer) condition.await();
        writer = true;
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

  public SimpleReadWriteLock() {
    readers = 0;
    writer = false;
    lock = new ReentrantLock();
    read_lock = new ReadLock();
    write_lock = new WriteLock();
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

  private int readers;
  private boolean writer;
  private Lock lock;
  private Lock read_lock;
  private Lock write_lock;
  private Condition condition;
}
