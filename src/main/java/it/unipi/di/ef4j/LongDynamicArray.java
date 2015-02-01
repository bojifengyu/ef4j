/*
 * 
 * Copyright (C) 2014 Giulio Ermanno Pibiri
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses/>.
 */

package it.unipi.di;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * The <tt>LongDynamicArray</tt> class represents a dynamic array of <tt>long</tt>s. It avoids the
 * space/time overhead introduced by the wrapper <tt>java.lang.Long</tt> class.
 * This is an <em>utility class</em>, used in Elias-Fano succinct data structures therefore avoids
 * index bound checks.
 * 
 * @author Giulio Ermanno Pibiri
 */
public final class LongDynamicArray implements Iterable<Long> {
  // Array of stored longs.
  protected long[] array;

  // Number of inserted longs.
  protected int length;

  // Default initial capacity.
  protected static final int INITIAL_CAPACITY = 2;
  
  //Maximum allowed capacity.
  protected int maxCapacity = Integer.MAX_VALUE;

  private void init(final int capacity) {
    array = new long[capacity];
    length = 0;
  }

  /**
   * Constructor for unknown initial capacity. Default initial capacity is fixed to 2.
   */
  public LongDynamicArray() {
    init(INITIAL_CAPACITY);
  }

  /**
   * Constructor for known initial capacity and maximum allowed capacity.
   * 
   * @param capacity the specified initial capacity.
   * @param maxCapacity the specified maximum allowed capacity.
   * @throws illegalArgumentException if the maximum capacity is less than the initial one.
   */
  public LongDynamicArray(final int capacity, final int maxCapacity) {
    if (maxCapacity < capacity) {
      throw new IllegalArgumentException(
          "Maximum capacity must be at least equal to the specified initial one.");
    }
    init(capacity);
    this.maxCapacity = maxCapacity;
  }
  
  /**
   * Constructor for known initial capacity.
   * 
   * @param capacity the specified initial capacity.
   */
  public LongDynamicArray(final int capacity) {
    init(capacity);
  }

  /**
   * Clear the data structure.
   */
  public void clear() {
    init(INITIAL_CAPACITY);
  }
  
  /**
   * Clear the data structure with the specified initial capacity.
   */
  public void clear(final int capacity) {
    init(capacity);
  }

  /**
   * Append the specified integer to the end of the array.
   * 
   * @param integer the to-be-appended integer.
   */
  public void add(final long integer) {
    resize();
    array[length++] = integer;
  }

  /**
   * Add a new item to the array at the given position. Shifts any subsequent elements to the right
   * (add one to their indices).
   * 
   * @param index the position at which we append the specified item.
   * @param item the item to be added.
   */
  public void addLong(final int index, final long integer) {
    resize();
    System.arraycopy(array, index, array, index + 1, length - index);
    array[index] = integer;
    length++;
  }

  /**
   * Returns how many integers are currently stored in the array.
   * 
   * @return the number of stored items in the array.
   */
  public int size() {
    return length;
  }

//  /**
//   * Print all the items in the collection. Useful for debugging.
//   */
//  public void print() {
//    final int length = this.length;
//    if (length == 0) {
//      System.out.print("/");
//    }
//    if (length == 1) {
//      System.out.print("[" + array[0] + "]");
//    } else {
//      for (int i = 0; i < length; i++) {
//        if (i == 0) {
//          System.out.print("[" + array[0] + ", ");
//        } else if (i == length - 1) {
//          System.out.print(array[length - 1] + "]");
//        } else {
//          System.out.print(array[i] + ", ");
//        }
//      }
//    }
//  }

  /**
   * Returns the allocated capacity for the array.
   * 
   * @return the allocated capacity for the array.
   */
  public int capacity() {
    return array.length;
  }

  /**
   * Trim the backing array to its current size.
   */
  public void trimToSize() {
    if (length < array.length) {
      resize(length);
    }
  }

  /**
   * Makes room for a new integer at the specified position. Shifts any subsequent elements to the
   * right (add one to their indices).
   * 
   * @param index the index of the integer to be inserted.
   */
  public void insertLong(final int index) {
    resize();
    System.arraycopy(array, index, array, index + 1, length - index);
    length++;
  }

  /**
   * Removes the integer at the specified position in this list. Shifts any subsequent elements to
   * the left (subtracts one from their indices).
   * 
   * @param index the index of the to-be-removed item.
   */
  public void removeLong(final int index) {
    resize();
    System.arraycopy(array, index + 1, array, index, length - index - 1);
    length--;
  }

  private void resize() {
    int newCapacity;
    if (length == array.length) {
      newCapacity = array.length << 1;
      resize(newCapacity);
    } else if (length > 0 && length == array.length >> 2) {
      newCapacity = array.length >> 1;
      resize(newCapacity);
    }
  }

  private void resize(final int capacity) {
    long[] temp = new long[capacity];
    System.arraycopy(array, 0, temp, 0, length);
    array = temp;
  }

  /**
   * Returns an array containing all of the elements in this list in proper sequence (from first to
   * last element).
   * 
   * @return an array containing all of the elements in this list in proper sequence (from first to
   *         last element).
   */
  public long[] toArray() {
    long[] temp = new long[length];
    System.arraycopy(array, 0, temp, 0, length);
    return temp;
  }

  /**
   * Returns the number of bits used by the array.
   * 
   * @return the number of bits used by the array.
   */
  public int bits() {
    return array.length * Long.SIZE;
  }

  @Override
  public Iterator<Long> iterator() {
    return new LongDynamicArrayIterator<Long>();
  }
  
  private class LongDynamicArrayIterator<T> implements Iterator<Long> {
    private int N = length;
    private int next = 0;

    LongDynamicArrayIterator() {
      next = 0;
      N = length;
    }

    @Override
    public boolean hasNext() {
      return next < N;
    }

    @Override
    public Long next() {
      if (hasNext()) {
        return array[next++];
      }
      throw new NoSuchElementException();
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
