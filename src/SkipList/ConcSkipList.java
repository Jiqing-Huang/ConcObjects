package SkipList;

public interface ConcSkipList<T> {
  boolean Add(T t);
  boolean Remove(T t);
  boolean Contains(T t);
}
