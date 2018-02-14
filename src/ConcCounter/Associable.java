package ConcCounter;

public interface Associable<T> {
  T Clone();
  void Set(T t);
  void Aggregate(T rhs);
}
