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

/**
 * The <tt>LongDynamicArray</tt> class represents a dynamic array of <tt>long</tt>s. It avoids the
 * space/time overhead introduced by the wrapper <tt>java.lang.Long</tt> class. This is an
 * <em>utility class</em>, used in Elias-Fano succinct data structures but it could have an
 * independent interest too.
 * 
 * @author Giulio Ermanno Pibiri
 */
public class LongDynamicArray {
  // Array of stored longs.
  protected long[] array;

  // Number of inserted longs.
  protected int length;

  // Default initial capacity.
  protected static final int INITIAL_CAPACITY = 2;

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
   * Append the specified integer to the end of the array.
   * 
   * @param integer the to-be-appended integer.
   */
  public void addLong(final long integer) {
    resize();
    array[length++] = integer;
  }

  /**
   * Set the integer in the specified position to the given value.
   * 
   * @param index the position of the to-be-set integer.
   * @param integer the value to be set.
   * @throws IndexOutOfBoundsException if <tt>index</tt> if greater or equal to the number of items
   *         in the array.
   */
  public void setLong(final int index, final long integer) {
    checkIndex(index);
    array[index] = integer;
  }

  /**
   * Returns the integer at a specified position.
   * 
   * @param index the index of the integer to be retrieved.
   * @return the integer at position <tt>index</tt>.
   * @throws IndexOutOfBoundsException if <tt>index</tt> if greater or equal to the number of
   *         integers in the array.
   */
  public long getLong(final int index) {
    checkIndex(index);
    return array[index];
  }

  /**
   * Returns how many integers are currently stored in the array.
   * 
   * @return the number of stored items in the array.
   */
  public int length() {
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
   * @throws IndexOutOfBoundsException if <tt>index</tt> if greater or equal to the number of
   *         integers in the array.
   */
  public void insertLong(final int index) {
    checkIndex(index);
    resize();
    final int l = length - 1;
    for (int i = l; i >= index; i--) {
      array[i + 1] = array[i];
    }
    length++;
  }

  /**
   * Removes the integer at the specified position in this list. Shifts any subsequent elements to
   * the left (subtracts one from their indices).
   * 
   * @param index the index of the to-be-removed item.
   * @throws IndexOutOfBoundsException if <tt>index</tt> if greater or equal to the number of
   *         integers in the array.
   */
  public void removeLong(final int index) {
    checkIndex(index);
    resize();
    final int l = length - 1;
    for (int i = index; i < l; i++) {
      array[i] = array[i + 1];
    }
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
    final int l = length;
    for (int i = 0; i < l; i++) {
      temp[i] = array[i];
    }
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
    final int length = this.length;
    long[] temp = new long[length];
    for (int i = 0; i < length; i++) {
      temp[i] = array[i];
    }
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

  private void checkIndex(final int index) {
    if (index >= array.length) {
      throw new IndexOutOfBoundsException("" + index);
    }
  }
}
