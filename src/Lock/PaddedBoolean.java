package Lock;

public class PaddedBoolean {
  public PaddedBoolean(int num_bytes, boolean init) {
    padding = new byte[num_bytes];
    padding[0] = (init)? (byte)1 : (byte)0;
  }

  public boolean Get() {
    return padding[0] == (byte)1;
  }

  public void Set(boolean value) {
    padding[0] = (value)? (byte)1 : (byte)0;
  }

  private byte[] padding;
}
