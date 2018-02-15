package Counter;

import java.util.concurrent.atomic.AtomicLong;

public class Balancer {
  public Balancer() {
    toggle = new AtomicLong(0);
  }

  public int Discriminate() {
    boolean untoggled = (toggle.getAndIncrement() & 1) == 0;
    return (untoggled)? output_first : output_second;
  }

  public void SetFirstOutput(int output) {
    output_first = output;
  }

  public void SetSecondOutput(int output) {
    output_second = output;
  }

  private int output_first;
  private int output_second;
  private AtomicLong toggle;
}
