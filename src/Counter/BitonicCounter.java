package Counter;

import java.util.concurrent.atomic.AtomicInteger;

public class BitonicCounter implements Counter.ConcCounter {

  public BitonicCounter(int num_threads) {
    width = CeilingPowerOfTwo(num_threads);
    layer = GetLayer(width);
    counters = new AtomicInteger[width];
    for (int i = 0; i != width; ++i)
      counters[i] = new AtomicInteger(0);
    network = new Balancer[layer][width];
    BuildBitonic();
  }

  @Override
  public int GetAndIncrement(int thread_id) {
    int output = thread_id;
    for (int depth = 0; depth != layer; ++depth) {
      Balancer balancer = network[depth][output];
      output = balancer.Discriminate();
    }
    AtomicInteger counter = counters[output];
    return counter.getAndIncrement() * width + output;
  }

  @Override
  public int IncrementAndGet(int thread_id) {
    return GetAndIncrement(thread_id) + 1;
  }

  private int CeilingPowerOfTwo(int x) {
    x = x - 1;
    x |= x >> 1;
    x |= x >> 2;
    x |= x >> 4;
    x |= x >> 8;
    x |= x >> 16;
    return x + 1;
  }

  private int GetLayer(int x) {
    int ret = -1;
    while (x > 0) {
      ++ret;
      x >>= 1;
    }
    return (ret + 1) * ret / 2;
  }

  private void BuildBitonic() {
    int depth = 0;
    int sub_width = 1;
    while (depth != layer) {
      int stride = 1 << sub_width;
      while (stride > 1) {
        BuildLayer(depth, stride);
        ++depth;
        stride >>= 1;
      }
      ++sub_width;
    }
  }

  private void BuildLayer(int depth, int stride) {
    Balancer[] balancers = network[depth];
    for (int i = 0; i != width; i += stride)
      for (int j = 0; j != stride / 2; ++j) {
        int input_first = i + j;
        int input_second = i + j + stride / 2;
        balancers[input_first] = new Balancer();
        balancers[input_second] = balancers[input_first];
        balancers[input_first].SetFirstOutput(input_first);
        balancers[input_first].SetSecondOutput(input_second);
      }
  }

  private int width;
  private int layer;
  private AtomicInteger[] counters;
  private Balancer[][] network;
}
