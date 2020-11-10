package app.coronawarn.server.services.download;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Implements a size-limited {@link Queue} with a {@link Set}-like fast lookup.
 * When elements are added and the number of elements exceeds the given limit,
 * oldest elements are removed (FIFO).
 *
 * @param <E> the type of elements held in this collection.
 * 
 * @see Queue
 * @see Set
 */
public class FifoMaxEntriesSet<E> {

  private final Set<E> fast = new HashSet<>();
  private final Queue<E> order = new LinkedList<>();
  private final int maxSize;

  public FifoMaxEntriesSet(int maxSize) {
    this.maxSize = maxSize;
  }

  /**
   * Adds the element to this set and removes elements until size <= maxSize.
   * 
   * @param e The element to add
   */
  public void add(E e) {
    if (fast.add(e)) {
      order.add(e);
    }
    while (fast.size() > maxSize) {
      fast.remove(order.remove());
    }
  }

  /**
   * Returns {@code true} if this set still contains the specified element. More
   * formally, returns {@code true} if and only if this set contains an element
   * {@code e} such that {@code Objects.equals(o, e)}.
   *
   * @param e element whose presence in this set is to be tested
   * @return {@code true} if this set still contains the specified element
   */
  public boolean contains(E e) {
    return fast.contains(e);
  }

}
