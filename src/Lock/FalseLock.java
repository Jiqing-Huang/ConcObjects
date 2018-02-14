package Lock;

// This lock doesn't lock anything. It is merely used as a baseline for performance benchmarking.

public class FalseLock implements Lock {
  @Override
  public void Lock() {
    // do nothing
  }

  @Override
  public void Unlock() {
    // do nothing
  }
}
