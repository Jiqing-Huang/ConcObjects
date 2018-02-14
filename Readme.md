This repository is a collection of concurrent Java objects. Most of them are implementations and extensions of
algorithms in the book "The Art of Multiprocessor 
Programming 2008 Edition"

This repository is still under construction

List of Packages:

1. Lock: spin lock\
  \
  TASLock: simple test and set spin lock\
  TTASLock: test and test and set spin lock\
  BackOffLock: a test and set lock with exponential backoff policy\
  ArrayLock: array based queuing spin lock\
  MSCLock: linked list based queuing spin lock\
  CLHLock: similar to MSCLock, the difference being that the linked list is implicit
  
2. TimeoutLock: spin lock with a timeout option\
  \
  CLHTOLock: basically CLHLock with timeout\
  CompositeLock: a two-phase lock. The lock tries to enqueue itself to a fixed-sized queue\
  with an exponential backoff policy. If it successes, it busy waits for its term to\
  actually acquire the lock.\
  CompositeFastPathLock: a CompositeLock that has a fast path lock acquisition mechanism\
  when contention is low.
  
3. ReadWriteLock: spin lock for data to which multiple readers and multiple writers may access\
  \
  SimpleReadWriteLock: implemented with standard library ReentrantLock. The lock makes no\
  guarantee on fairness and favors readers.\
  FifoReadWriteLock: guarantees FIFO and favors writers.
  
4. LinkedListSet: a linked list based concurrent set. The purpose is to demonstrate various
   locking policies for a concurrent data structure. Linked list is a poor choice to implement a set
   in terms of performance.\
   \
   CoarseList: A naive approach by imposing lock on the whole set\
   FineList: impose lock on each node. Nodes are traversed with hand-by-hand locking.\
   OptimisticList: does not lock when traversing list but double check before performing 
   insertion/removal/query. Good for low contention concurrency.\
   LazyList: does not lock when traversing and performing query. Do lock when insert/remove. Removal
   is done in a two step lazy manner.\
   LockFreeList: a variant of LazyList, using stamped CAS to replace lock in insertion/removal to
   achieve total lock-free.\
   
5. ConcQueue: concurrent FIFO queue\
  \
  BoundedQueue: implemented with standard library ReentrantLock for enqueuing and dequeuing with
  an upper limit on size.\
  UnboundedQueue: ReentrantLock based queue without limit on size.\
  LockFreeQueue: unbounded queue with CAS to replace lock.\
  SynchronousQueue: queue that requires the enqueuer not to return until it hands over
  its object to a dequeuer, and <i>vise versa</i>. This is a naive and slow approach.\
  SynchronousDualQueue: A lock free implementation of synchronous queue. It uses a reservation/item 
  list to decouple enqueue and dequeue in memory.
  
6. ConcStack: concurrent LIFO stack\
  \
  LockFreeStack: a CAS based lock free stack.
  EliminationBackoffStack: a LockFreeStack with an exchange array for failed pusher and 
  popper to exchange items with a backoff policy. 
  
7. ConcCounter: \
  \
  (under construction)