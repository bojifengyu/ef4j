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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * The <tt>AbstractMonotoneLongSequence</tt> class represents defines a sequence of
 * <em>non-decreasing monotone</em> <tt>Long</tt> integers.
 * 
 * @author Giulio Ermanno Pibiri
 */
public abstract class AbstractMonotoneLongSequence implements List<Long> {
  // Current length of the sequence.
  protected int length = 0;

  /**
   * Set the length of the sequence to the specified value.
   * 
   * @param length the length of the sequence.
   */
  protected final void setLength(final int length) {
    this.length = length;
  }

  /**
   * Clear the sequence.
   */
  @Override
  public abstract void clear();

  /**
   * Iterator over the whole sequence.
   * 
   * @return an iterator over the whole sequence.
   * @see java.util.Iterator
   */
  @Override
  public abstract Iterator<Long> iterator();

  /**
   * Iterator over a specified range of the sequence.
   * 
   * @param from the starting position.
   * @param to the ending position.
   * @return the integers of the sequence in proper order from <tt>from</tt> to <tt>to</tt>
   *         included.
   * @see java.util.Iterator
   */
  public abstract Iterator<Long> iterator(final int from, final int to);

  /**
   * Checks for proper indices.
   * 
   * @param from the starting position.
   * @param to the ending position.
   * @throws IllegalArgumentException if <tt>from</tt> > <tt>to</tt>.
   * @throws IndexOutOfBoundsException if bounds are incorrect.
   */
  public final void checkIndices(final int from, final int to) {
    if (from >= length) {
      throw new IndexOutOfBoundsException("" + from);
    }
    if (to >= length) {
      throw new IndexOutOfBoundsException("" + to);
    }
    if (from > to) {
      throw new IllegalArgumentException(to + " < " + from);
    }
  }
  
  /**
   * Add new integer to the sequence in proper position.
   * 
   * @param integer the to-be-appended value.
   */
  @Override
  public abstract boolean add(final Long integer);

  /**
   * Add all integers in the specified collection if possible.
   * 
   * @param c the collection of integers to be added.
   */
  @Override
  public final boolean addAll(final Collection<? extends Long> c) {
    for (Long i : c) {
      add(i);
    }
    return true;
  }

  /**
   * Returns the integer at position <tt>index</tt>.
   * 
   * @param index the index of the integer to be retrieved.
   * @return the integer at position <tt>index</tt>.
   * @throws IndexOutOfBoundsException if <tt>index</tt> is larger than the actual length of the
   *         sequence.
   */
  @Override
  public abstract Long get(final int index);

  /**
   * Returns the smallest element of the sequence that is greater than or equal to the given
   * integer. Returns <tt>-1</tt> if such value is not found.
   * 
   * @param integer the integer for which we want to compute its smallest greater or equal value in
   *        the sequence.
   * @return the smallest integer greater or equal to the one specified if it exists; <tt>-1</tt>
   *         otherwise.
   */
  public abstract Long nextGEQ(final long integer);

  /**
   * Remove from the sequence the specified integer if it exists.
   * 
   * @return <tt>true</tt> if the removal succeded; <tt>false</tt> otherwise.
   */
  public abstract boolean remove(final long integer);

  /**
   * Return <tt>true</tt> if all integers in the collection are removed from sequence.
   * 
   * @param c the collection whose integers have to be removed from the sequence.
   * @return <tt>true</tt> if all integers in <tt>c</tt> are removed; <tt>false</tt> otherwise.
   */
  @Override
  public boolean removeAll(final Collection<?> c) {
    for (Object o : c) {
      if (!remove((long) (Long) o)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Return <tt>true</tt> if the sequence contains the specified integer.
   * 
   * @param o the integer to be tested for presence in the sequence.
   * @return <tt>true</tt> if the integer is present; <tt>false</tt> otherwise.
   */
  @Override
  public final boolean contains(final Object o) {
    final long integer = (Long) o;
    return nextGEQ(integer) == integer;
  }

  /**
   * Return <tt>true</tt> if the sequence contains all the integers in the specified collection.
   * 
   * @param c the collection whose integers have to be tested for presence in the sequence.
   * @return <tt>true</tt> if all integers in <tt>c</tt> are present; <tt>false</tt> otherwise.
   */
  @Override
  public final boolean containsAll(final Collection<?> c) {
    for (Object o : c) {
      if (!this.contains(o)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Trim all backing inner datastructures to their actual sizes.
   */
  public abstract void trimToSize();

  /**
   * Returns an array containing all of the elements in this list in proper sequence (from first to
   * last element).
   * 
   * @return an array containing all of the elements in this list in proper sequence (from first to
   *         last element).
   */
  @Override
  public final Long[] toArray() {
    Long[] temp = new Long[length];
    int i = 0;
    for (Long integer : this) {
      temp[i++] = integer;
    }
    return temp;
  }

  /**
   * Returns the sub list specified by the given range, from <tt>from</tt> to <tt>to</tt> included.
   * 
   * @param from the starting position.
   * @param to the ending position.
   * @return the integers of the sequence in proper order from <tt>from</tt> to <tt>to</tt>
   *         included.
   * @throws IllegalArgumentException if <tt>from</tt> > <tt>to</tt>.
   * @throws IndexOutOfBoundsException if bounds are incorrect.
   */
  @Override
  public abstract List<Long> subList(final int from, final int to);

  /**
   * Returns the number of bits used by the sequence.
   * 
   * @return the number of bits used by the sequence.
   */
  public abstract int bits();

  /**
   * Returns <tt>true</tt> if the sequence is empty; <tt>false</tt> otherwise.
   * 
   * @return <tt>true</tt> if the sequence is empty; <tt>false</tt> otherwise.
   */
  @Override
  public final boolean isEmpty() {
    return length == 0;
  }

  /**
   * Returns the current length of the sequence.
   * 
   * @return the current length of the sequence.
   */
  @Override
  public final int size() {
    return length;
  }

  /**
   * Returns the index of the first occurrence of the specified object if it is present in the
   * sequence; -1 otherwise.
   * 
   * @return the index of the first occurrence of the specified object if it is present in the
   *         sequence; -1 otherwise.
   */
  @Override
  public final int indexOf(Object o) {
    Iterator<Long> it = this.iterator();
    int index = 0;
    while (it.hasNext()) {
      if (o.equals(it.next())) {
        return index;
      }
      index++;
    }
    return -1;
  }

  /**
   * Returns the index of the last occurrence of the specified object if it is present in the
   * sequence; -1 otherwise.
   * 
   * @return the index of the last occurrence of the specified object if it is present in the
   *         sequence; -1 otherwise.
   */
  @Override
  public final int lastIndexOf(Object o) {
    final int index = indexOf(o);
    if (index == -1) {
      return -1;
    }
    if (index + 1 < length) {
      final long v = get(index + 1);

      if (o.equals(v)) {
        return index + 1;
      }
    }
    return index;
  }

  /*
   * 
   * 
   * The following operations are NOT supported.
   */

  /**
   * Unsupported operation.
   */
  @Override
  public boolean remove(final Object o) {
    throw new UnsupportedOperationException();
  }

  /**
   * Unsupported operation.
   */
  @Override
  public Long remove(final int index) {
    throw new UnsupportedOperationException();
  }

  /**
   * Unsupported operation since the sequence is monotone.
   */
  @Override
  public void add(final int index, final Long integer) {
    throw new UnsupportedOperationException();
  }

  /**
   * Unsupported operation since the sequence is monotone.
   */
  @Override
  public boolean addAll(final int index, final Collection<? extends Long> c) {
    throw new UnsupportedOperationException();
  }

  /**
   * Unsupported operation.
   */
  @Override
  public boolean retainAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  /**
   * Unsupported operation since the sequence is monotone.
   */
  @Override
  public Long set(final int index, final Long integer) {
    throw new UnsupportedOperationException();
  }

  /**
   * Unsupported operation.
   */
  @Override
  public <T> T[] toArray(T[] a) {
    throw new UnsupportedOperationException();
  }

  /**
   * Unsupported operation since the sequence is monotone.
   */
  @Override
  public ListIterator<Long> listIterator() {
    throw new UnsupportedOperationException();
  }

  /**
   * Unsupported operation since the sequence is monotone.
   */
  @Override
  public ListIterator<Long> listIterator(final int from) {
    throw new UnsupportedOperationException();
  }
}
