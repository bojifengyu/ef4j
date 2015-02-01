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
 * The <tt>DynamicArray</tt> class represents a resizing generic collection of items, whose
 * dimension can be <em>upper bounded</em> by a specific value. This is an utility class used in
 * Elias-Fano succinct data structures but it could have an independent interest too.
 *
 * @author Giulio Ermanno Pibiri
 */
public final class DynamicArray<E> implements Iterable<E> {
  // Array of stored elements.
  protected E[] array;

  // Number of inserted elements.
  protected int length;
  
  // Initial array capacity.
  static final int INIT_CAPACITY = 2;

  /**
   * Constructor for unknown initial capacity. Default initial capacity is fixed to 2.
   */
  @SuppressWarnings("unchecked")
  public DynamicArray() {
    array = (E[]) new Object[INIT_CAPACITY];
    length = 0;
  }

  /**
   * Constructor for known initial capacity and maximum allowed capacity.
   * 
   * @param capacity the specified initial capacity.
   * @param maxCapacity the specified maximum allowed capacity.
   * @throws illegalArgumentException if the maximum capacity is less than the initial one.
   */
  @SuppressWarnings("unchecked")
  public DynamicArray(final int capacity) {
    array = (E[]) new Object[capacity];
    length = 0;
  }

  /**
   * Constructor for an initial array specification.
   * 
   * @param array the specified initial array.
   * @throws NullPointerException if the specified array is <tt>null</tt>.
   */
  public DynamicArray(final E[] array) {
    this.array = array;
    length = array.length;
  }

  /**
   * Clear the data structure.
   */
  @SuppressWarnings("unchecked")
  public void clear() {
    array = (E[]) new Object[INIT_CAPACITY];
    length = 0;
  }

  /**
   * Clear the data structure with the specified initial capacity.
   */
  @SuppressWarnings("unchecked")
  public void clear(final int capacity) {
    array = (E[]) new Object[capacity];
    length = 0;
  }

  /**
   * Iterator over the collection.
   * 
   * @return an iterator over the collection.
   * @throws UnsupportedOperationException in <tt>remove</tt> operation.
   */
  @Override
  public Iterator<E> iterator() {
    return new AppendOnlyArrayIterator();
  }

  /**
   * Iterator over a specified range of the collection.
   * 
   * @param from the starting position.
   * @param to the ending position.
   * @return the integers of the sequence in proper order from <tt>from</tt> to <tt>to</tt>
   *         included.
   * @throws UnsupportedOperationException in <tt>remove</tt> operation.
   * @throws IllegalArgumentException if <tt>from</tt> > <tt>to</tt>.
   * @throws IndexOutOfBoundsException if bounds are incorrect.
   * @see java.util.Iterator
   */
  public Iterator<E> iterator(final int from, final int to) {
    if (from >= length) {
      throw new IndexOutOfBoundsException("" + from);
    }
    if (to >= length) {
      throw new IndexOutOfBoundsException("" + to);
    }
    if (to < from) {
      throw new IllegalArgumentException(to + " < " + from);
    }
    return new AppendOnlyArrayIterator(from, to);
  }

  private class AppendOnlyArrayIterator implements Iterator<E> {
    private int N;
    private int next;

    AppendOnlyArrayIterator() {
      next = 0;
      N = length;
    }

    AppendOnlyArrayIterator(final int from, final int to) {
      next = from;
      N = to + 1;
    }

    @Override
    public boolean hasNext() {
      return next < N;
    }

    @Override
    public E next() {
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

  /**
   * Trim the backing array to its current size.
   */
  public void trimToSize() {
    if (length < array.length) {
      resize(length);
    }
  }

  /**
   * Check whether the array is empty or not.
   * 
   * @return <tt>true</tt> if the array is empty; <tt>false</tt> otherwise.
   */
  public boolean isEmpty() {
    return length == 0;
  }

  /**
   * Returns how many items are currently stored in the array.
   * 
   * @return the number of stored items in the array.
   */
  public int size() {
    return length;
  }

  /**
   * Returns the allocated capacity for the array.
   * 
   * @return the allocated capacity for the array.
   */
  public int capacity() {
    return array.length;
  }

  /**
   * Add a new item to the end of the array. <tt>null</tt> items are permitted.
   * 
   * @param item the item to be added.
   */
  public boolean add(final E item) {
    resize();
    array[length++] = item;
    return true;
  }

  /**
   * Add a new item to the array at the given position. Shifts any subsequent elements to the right
   * (add one to their indices).
   * 
   * @param index the position at which we append the specified item.
   * @param item the item to be added.
   * @throws IndexOutOfBoundsException if <tt>index</tt> if greater or equal to the number of items
   *         in the array.
   */
  public boolean add(final int index, final E item) {
    resize();
    System.arraycopy(array, index, array, index + 1, length - index);
    array[index] = item;
    length++;
    return true;
  }

  /**
   * Removes the element at the specified position in this list. Shifts any subsequent elements to
   * the left (subtracts one from their indices).
   * 
   * @param index the index of the to-be-removed item.
   * @throws IndexOutOfBoundsException if <tt>index</tt> if greater or equal to the number of items
   *         in the array.
   */
  public boolean remove(final int index) {
    resize();
    System.arraycopy(array, index + 1, array, index, length - index - 1);
    length--;
    array[length] = null; // avoids loitering
    return true;
  }

  // This routine causes a resize of the array if the current capacity hase been reached.
  private void resize() {
    if (length == array.length) {
      resize(array.length << 1);
    } else if (length > 0 && length == array.length >> 2) {
      resize(array.length >> 1);
    }
  }

  // Resizing with a specified capacity.
  @SuppressWarnings("unchecked")
  private void resize(final int capacity) {
    E[] temp = (E[]) new Object[capacity];
    System.arraycopy(array, 0, temp, 0, length);
    array = temp;
  }

  /**
   * Set an item in a specified position to a given value.
   * 
   * @param index the position of the item to be set.
   * @param item the value to assign to the item in position <tt>index</tt>.
   * @throws IndexOutOfBoundsException if <tt>index</tt> if greater or equal to the number of items
   *         in the array.
   */
  public void set(final int index, final E item) {
    array[index] = item;
  }

  /**
   * Returns the item at a specified position.
   * 
   * @param index the index of the item to be retrieved.
   * @return the item at position <tt>index</tt>.
   * @throws IndexOutOfBoundsException if <tt>index</tt> if greater or equal to the number of items
   *         in the array.
   */
  public E get(final int index) {
    return array[index];
  }

  /**
   * Returns an array containing all of the elements in this list in proper sequence (from first to
   * last element).
   * 
   * @return an array containing all of the elements in this list in proper sequence (from first to
   *         last element).
   */
  @SuppressWarnings("unchecked")
  public E[] toArray() {
    E[] temp = (E[]) new Object[length];
    System.arraycopy(array, 0, temp, 0, length);
    return temp;
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
}
