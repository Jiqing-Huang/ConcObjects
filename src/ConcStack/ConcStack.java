package ConcStack;

public interface ConcStack<T> {
  void Push(T t);
  T Pop();
  void Clear();
}
