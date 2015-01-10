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

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

import it.unimi.dsi.bits.BitVector;
import it.unimi.dsi.bits.Fast;
import it.unimi.dsi.bits.LongArrayBitVector;
import it.unimi.dsi.fastutil.longs.LongBigList;
import it.unimi.dsi.sux4j.bits.SimpleSelect;

/**
 * The <tt>EliasFanoAppendOnlyMonotoneLongSequence</tt> class represents a monotone sequence of
 * non-decreasing integers compressed with the <em>Elias-Fano integer encoding</em>. The
 * implementation maintains a collection of buckets of size B integers that are all statically
 * compressed with the Elias-Fano strategy. It is responsibility of the user to provide the proper
 * value of B to maximize compression. This is possible if we know how many elements the sequence is
 * expected to contain.
 * 
 * <p>
 * It supports the <em>append</em>, <em>get</em> and <em>next greater or equal</em> operations,
 * along with methods for inspecting how many bits the sequence is using, testing if the sequence is
 * empty, and iterating through the items in order.
 * </p>
 * 
 * @author Giulio Ermanno Pibiri
 */
public final class EliasFanoAppendOnlyMonotoneLongSequence extends AbstractAppendOnlyMonotoneLongSequence
    implements RandomAccess, Cloneable, Serializable {
  // Serial ID number.
  private transient static final long serialVersionUID = 13071990L;

  // Size of a bucket.
  protected int B;

  // Buffer of the to-be-compressed elements.
  protected long[] buffer;

  // Number of inserted elements in buffer.
  protected int N;

  // Number of created buckets.
  protected int buckets;

  // Last inserted element.
  protected long last;

  // Array of lower bits' bitmaps stored explicitly: one for each bucket.
  protected DynamicArray<long[]> lowerBits;

  // Array of selectors' references: one for each bucket.
  protected DynamicArray<SimpleSelect> selectors;

  // Array storing for each bucket, in an interleaved way, the number of lower bits and maximum
  // element.
  protected LongDynamicArray info;

  // Bitmask to extract lower bits.
  protected transient static final long LOWER_BITS_MASK = (1L << 6) - 1;

  // Bitmask to extract upper bounds.
  protected transient static final long UPPER_BITS_MASK = ~LOWER_BITS_MASK;

  /**
   * Constructor for unknown initial capacity.
   * 
   * @param B the chosen bucket size.
   * @throws IllegalArgumentException if <tt>B</tt> is zero.
   */
  public EliasFanoAppendOnlyMonotoneLongSequence(final int B) {
    if (B == 0) {
      throw new IllegalArgumentException("Bucket size must be greater than 0.");
    }

    this.B = B;
    last = Long.MIN_VALUE;
    buffer = new long[B];
    N = 0;
    lowerBits = new DynamicArray<long[]>();
    selectors = new DynamicArray<SimpleSelect>();
    info = new LongDynamicArray();
    info.addLong(0L);
    buckets = 0;
  }

  /**
   * Constructor for known initial capacity.
   * 
   * @param B the chosen bucket size.
   * @param capacity the initial capacity.
   * @throws IllegalArgumentException if <tt>B</tt> is zero.
   * @throws IllegalArgumentException if <tt>capacity</tt> is less than <tt>B</tt>.
   */
  public EliasFanoAppendOnlyMonotoneLongSequence(final int B, final int capacity) {
    if (B == 0) {
      throw new IllegalArgumentException("Bucket size must be greater than 0.");
    }
    if (capacity < B) {
      throw new IllegalArgumentException(
          "Initial capacity must be at least equal to the bucket size.");
    }

    this.B = B;
    last = Long.MIN_VALUE;
    buffer = new long[B];
    N = 0;
    final int b = capacity / B;
    lowerBits = new DynamicArray<long[]>(b, Integer.MAX_VALUE);
    selectors = new DynamicArray<SimpleSelect>(b, Integer.MAX_VALUE);
    info = new LongDynamicArray(b);
    info.addLong(0L);
    buckets = 0;
  }
  
  @Override
  public void clear() {
    length = 0;
    last = Long.MIN_VALUE;
    N = 0;
    lowerBits.clear();
    selectors.clear();
    info.clear();
    info.addLong(0L);
    buckets = 0;
  }

  @Override
  public Iterator<Long> iterator() {
    return new EliasFanoAppendOnlyMonotoneLongSequenceIterator<Long>();
  }

  @Override
  public Iterator<Long> iterator(final int from, final int to) {
    checkIndices(from, to);
    return new EliasFanoAppendOnlyMonotoneLongSequenceIterator<Long>(from, to);
  }

  protected Iterator<Long> iterator(final int bucket) {
    return new EliasFanoAppendOnlyMonotoneLongSequenceIterator<Long>(bucket);
  }

  protected Iterator<Long> iterator(final Integer bucket, final int bucketSize) {
    return new EliasFanoAppendOnlyMonotoneLongSequenceIterator<Long>(bucket, bucketSize);
  }

  private class EliasFanoAppendOnlyMonotoneLongSequenceIterator<T> implements Iterator<Long> {
    int next = 0;
    int bucket = 0;
    int offset = 0;
    int ones = 0;
    final int LONG_SIZE = Long.SIZE;
    final int N;
    long l;
    long u;
    long lowerBitsMask;
    long upperBits;
    long nextOne;
    SimpleSelect selector;
    long[] lowerBitsVector;

    EliasFanoAppendOnlyMonotoneLongSequenceIterator() {
      N = length;
    }

    EliasFanoAppendOnlyMonotoneLongSequenceIterator(final int from, final int to) {
      bucket = from / B;
      offset = from % B;
      next = from;

      if (offset != 0) {
        if (bucket < buckets) {
          selector = selectors.get(bucket);
          final long lu = info.getLong(bucket);
          l = lu & LOWER_BITS_MASK;
          u = (lu & UPPER_BITS_MASK) >> 6;
          lowerBitsMask = (1L << l) - 1;
          lowerBitsVector = lowerBits.get(bucket);

          ones = offset;
          nextOne = selector.select((offset == 0 ? 1 : offset) - 1);
        }
        bucket++;
      }

      N = to + 1;
    }

    EliasFanoAppendOnlyMonotoneLongSequenceIterator(final Integer bucket, final int bucketSize) {
      this.bucket = bucket;
      N = bucketSize;
    }

    EliasFanoAppendOnlyMonotoneLongSequenceIterator(final int bucket) {
      this.bucket = bucket;
      N = B;
    }

    @Override
    public boolean hasNext() {
      return next < N;
    }

    @Override
    public Long next() {
      if (hasNext()) {
        if (next % B == 0) {
          offset = 0;
          nextOne = -1;
          ones = 0;

          if (bucket < buckets) {
            selector = selectors.get(bucket);
            final long lu = info.getLong(bucket);
            l = lu & LOWER_BITS_MASK;
            u = (lu & UPPER_BITS_MASK) >> 6;
            lowerBitsMask = (1L << l) - 1;
            lowerBitsVector = lowerBits.get(bucket);
          }
          bucket++;
        }

        if (bucket == buckets + 1) {
          return buffer[next++ % B];
        }

        nextOne = selector.bitVector().nextOne(nextOne + 1);
        upperBits = nextOne - ones++;

        if (l == 0) {
          next++;
          offset++;
          return upperBits + u;
        }

        final long lowerBitsPosition = offset * l;
        final int startWord = (int) (lowerBitsPosition / LONG_SIZE);
        final int startBit = (int) (lowerBitsPosition % LONG_SIZE);
        final long totalOffset = startBit + l;
        final long result = lowerBitsVector[startWord] >>> startBit;

        next++;
        offset++;

        return (upperBits << l | (totalOffset <= LONG_SIZE ? result : result
            | lowerBitsVector[startWord + 1] << -startBit)
            & lowerBitsMask)
            + u;
      }
      throw new NoSuchElementException("Element not present.");
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  // Compression routine.
  protected void compress(final long[] buffer) {
    final int B = buffer.length;
    final long lu = info.getLong(buckets);
    final long prevUpper = (lu & UPPER_BITS_MASK) >> 6;
    final long last = buffer[B - 1];
    final long u = last - prevUpper;
    final long l = Math.max(0, Fast.mostSignificantBit(u / B));
    final long lowerBitsMask = (1L << l) - 1;

    LongArrayBitVector lowerBitsVector = LongArrayBitVector.getInstance();
    LongBigList lowerBitsList = lowerBitsVector.asLongBigList((int) l);
    lowerBitsList.size(B);
    BitVector upperBits = LongArrayBitVector.getInstance().length(B + (u >>> l) + 1);

    if (l != 0) {
      long v;
      for (int i = 0; i < B; i++) {
        v = buffer[i] - prevUpper;
        lowerBitsList.set(i, v & lowerBitsMask);
        upperBits.set((v >>> l) + i);
      }
    } else {
      for (int i = 0; i < B; i++) {
        upperBits.set(buffer[i] - prevUpper + i);
      }
    }

    lowerBits.add(lowerBitsVector.bits());
    selectors.add(new SimpleSelect(upperBits));
    info.setLong(buckets++, (prevUpper << 6) | l);
    info.addLong(last << 6);
  }

  @Override
  public boolean add(final Long integer) {
    if (last > integer) {
      throw new IllegalArgumentException("The list of values is not monotone: " + last + " > "
          + integer + ".");
    }

    if (N == B) {
      compress(buffer);
      N = 0;
    }

    buffer[N++] = integer;
    last = integer;
    length++;

    return true;
  }

  @Override
  public Long get(final int index) {
    if (index < 0 || index >= length) {
      throw new IndexOutOfBoundsException("" + index);
    }

    final int LONG_SIZE = Long.SIZE;
    final int B = this.B;
    final int bucket = index / B;
    final int offset = index % B;

    if (bucket == selectors.size()) {
      return buffer[offset];
    }

    final long lu = info.getLong(bucket);
    final long l = lu & LOWER_BITS_MASK;
    final long u = (lu & UPPER_BITS_MASK) >> 6;
    final long lowerBitsMask = (1L << l) - 1;
    final long upperBits = selectors.get(bucket).select(offset) - offset;

    if (l == 0) {
      return upperBits + u;
    }

    final long lowerBitsPosition = offset * l;
    final int startWord = (int) (lowerBitsPosition / LONG_SIZE);
    final int startBit = (int) (lowerBitsPosition % LONG_SIZE);
    final long totalOffset = startBit + l;
    final long result = lowerBits.get(bucket)[startWord] >>> startBit;

    return (upperBits << l | (totalOffset <= LONG_SIZE ? result : result
        | lowerBits.get(bucket)[startWord + 1] << -startBit)
        & lowerBitsMask)
        + u;
  }

  @Override
  public Long nextGEQ(final long integer) {
    final int pos = integer > 0 ? binarySearchOverInfo(integer, 0, buckets + 1) : 0;
    Iterator<Long> it = this.iterator(pos);
    while (it.hasNext()) {
      long v = it.next();
      if (v >= integer) {
        return v;
      }
    }
    return -1L;
  }

  // Binary search over info array.
  protected int binarySearchOverInfo(final long integer, final int i, final int j) {
    final int mid = (i + j) / 2;
    final long u1 = (info.getLong(mid) & UPPER_BITS_MASK) >> 6;

    if (integer == u1) {
      return u1 == 0 ? mid : mid - 1;
    }

    final long u2 = mid < buckets ? (info.getLong(mid + 1) & UPPER_BITS_MASK) >> 6 : last;

    if (integer > u1 && integer < u2) {
      return mid;
    }

    if (integer >= u2 && u2 < last) {
      return binarySearchOverInfo(integer, mid, j);
    }

    if (integer >= u2 && u2 == last) {
      return buckets;
    }

    return binarySearchOverInfo(integer, i, mid);
  }

  @Override
  public List<Long> subList(final int from, final int to) {
    checkIndices(from, to);
    final int B = (int) Math.ceil(Math.sqrt(length << 3));
    final int length = to - from >= B ? to - from : B;

    EliasFanoAppendOnlyMonotoneLongSequence subList =
        new EliasFanoAppendOnlyMonotoneLongSequence(B, length);

    Iterator<Long> it = this.iterator(from, to);
    while (it.hasNext()) {
      subList.add(it.next());
    }
    return subList;
  }

  @Override
  public int bits() {
    int bits = 0;
    final int buckets = this.buckets;
    final int LONG_SIZE = Long.SIZE;
    for (int i = 0; i < buckets; i++) {
      bits +=
          lowerBits.get(i).length * LONG_SIZE + selectors.get(i).numBits()
              + selectors.get(i).bitVector().length();
    }
    return bits + info.bits() + B * LONG_SIZE + selectors.capacity() * 64;
  }

  @Override
  public void trimToSize() {
    if (N < B) {
      long[] temp = new long[N];
      System.arraycopy(buffer, 0, temp, 0, N);
      buffer = temp;
    }
    selectors.trimToSize();
    lowerBits.trimToSize();
    info.trimToSize();
  }

  // Private constructor used in AppendOnlyEliasFano.clone().
  private EliasFanoAppendOnlyMonotoneLongSequence(int B, int length, int N, int buckets, long last,
      long[] buffer, LongDynamicArray info, DynamicArray<long[]> lowerBits,
      DynamicArray<SimpleSelect> selectors) {
    this.B = B;
    this.length = length;
    this.N = N;
    this.buckets = buckets;
    this.last = last;

    DynamicArray<long[]> lowerBitsClone = new DynamicArray<long[]>(buckets, Integer.MAX_VALUE);
    DynamicArray<SimpleSelect> selectorsClone =
        new DynamicArray<SimpleSelect>(buckets, Integer.MAX_VALUE);
    LongDynamicArray infoClone = new LongDynamicArray(buckets);

    for (int i = 0; i < buckets; i++) {
      lowerBitsClone.add(lowerBits.get(i).clone());
      selectorsClone.add(new SimpleSelect(selectors.get(i).bitVector().copy()));
      infoClone.addLong(info.getLong(i));
    }
    this.lowerBits = lowerBitsClone;
    this.selectors = selectorsClone;
    this.info = infoClone;
    this.buffer = buffer.clone();
  }

  /**
   * Returns a copy of the object.
   * 
   * @return a copy of the object.
   */
  @Override
  public EliasFanoAppendOnlyMonotoneLongSequence clone() {
    return new EliasFanoAppendOnlyMonotoneLongSequence(B, length, N, buckets, last, buffer, info,
        lowerBits, selectors);
  }
}
