package Stack;

public interface ConcStack<T> {
  void Push(T t);
  T Pop();
  void Clear();
  int Size();
}
