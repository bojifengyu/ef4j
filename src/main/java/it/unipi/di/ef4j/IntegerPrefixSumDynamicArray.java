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
 * The <tt>IntegerPrefixSumDynamicArray</tt> class represents a dynamic array of <tt>ints</tt>s,
 * stored in a prefix-sum fashion. It also avoids the space/time overhead introduced by the wrapper
 * <tt>java.lang.Integer</tt> class. This is an <em>utility class</em> used in Elias-Fano succinct
 * data structures but it could have an independent interest too.
 *
 * @author Giulio Ermanno Pibiri
 */
public class IntegerPrefixSumDynamicArray {
  // Array of stored ints.
  private int[] array;

  // Number of currently stored ints.
  private int length;


  /**
   * Constructor.
   * 
   * @param initValue the initial value to which all ints are set in a prefix-sum fashion.
   * @param length the initial length of the array.
   */
  public IntegerPrefixSumDynamicArray(final int initValue, final int length) {
    this.length = length;
    array = new int[length];
    int prefixSum = 0;
    for (int i = 0; i < length; i++) {
      array[i] = prefixSum += initValue;
    }
  }

  private void checkIndex(final int index) {
    if (index >= array.length) {
      throw new IndexOutOfBoundsException("" + index);
    }
  }

  /**
   * Returns the integer at a specified position as it is stored in the array, i.e. not subtracting
   * the previous one.
   * 
   * @param index the index of the integer to be retrieved.
   * @return the integer at position <tt>index</tt>.
   * @throws IndexOutOfBoundsException if <tt>index</tt> if greater or equal to the number of
   *         integers in the array.
   */
  public int get(final int index) {
    checkIndex(index);
    return array[index];
  }

  /**
   * Returns the integer at a specified position.
   * 
   * @param index the index of the integer to be retrieved.
   * @return the integer at position <tt>index</tt>.
   * @throws IndexOutOfBoundsException if <tt>index</tt> if greater or equal to the number of
   *         integers in the array.
   */
  public int getInt(final int index) {
    checkIndex(index);
    return index > 0 ? array[index] - array[index - 1] : array[0];
  }

  /**
   * Set the integer in the specified position to the given value.
   * 
   * @param index the position of the to-be-set integer.
   * @param integer the value to be set.
   * @throws IndexOutOfBoundsException if <tt>index</tt> if greater or equal to the number of items
   *         in the array.
   */
  public void setInt(final int index, final int integer) {
    final int diff = integer - getInt(index);
    final int l = length;
    for (int i = index; i < l; i++) {
      array[i] += diff;
    }
  }

  /**
   * Add an integer at the specified position. Shifts any subsequent elements to the right (add one
   * to their indices).
   * 
   * @param index the position of the to-be-added integer.
   * @param integer the integer value to add.
   * @throws IndexOutOfBoundsException if <tt>index</tt> if greater or equal to the number of items
   *         in the array.
   */
  public void addInt(final int index, final int integer) {
    checkIndex(index);
    resize();
    add(index, (index > 0 ? array[index - 1] : 0) + integer);
    final int l = length;
    for (int i = index + 1; i <= l; i++) {
      array[i] += integer;
    }
    length++;
  }

  private void add(final int index, final int integer) {
    final int l = length - 1;
    for (int i = l; i >= index; i--) {
      array[i + 1] = array[i];
    }
    array[index] = integer;
  }

  /**
   * Removes the integer at the specified position in the array. Shifts any subsequent elements to
   * the left (subtracts one from their indices).
   * 
   * @param index the index of the to-be-removed item.
   * @throws IndexOutOfBoundsException if <tt>index</tt> if greater or equal to the number of
   *         integers in the array.
   */
  public void removeInt(final int index) {
    checkIndex(index);
    resize();
    final int B = getInt(index);
    remove(index);
    final int l = array.length;
    for (int i = index; i < l; i++) {
      array[i] -= B;
    }
    length--;
  }

  private void remove(final int index) {
    final int l = length - 1;
    for (int i = index; i < l; i++) {
      array[i] = array[i + 1];
    }
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
    int[] temp = new int[capacity];
    final int l = length;
    for (int i = 0; i < l; i++) {
      temp[i] = array[i];
    }
    array = temp;
  }

  /**
   * Returns the number of bits used by the array.
   * 
   * @return the number of bits used by the array.
   */
  public int bits() {
    return array.length * Integer.SIZE;
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
   * Returns an array containing all of the elements in this list in proper sequence (from first to
   * last element).
   * 
   * @return an array containing all of the elements in this list in proper sequence (from first to
   *         last element).
   */
  public int[] toArray() {
    final int length = this.length;
    int[] temp = new int[length];
    for (int i = 0; i < length; i++) {
      temp[i] = array[i];
    }
    return temp;
  }
}
