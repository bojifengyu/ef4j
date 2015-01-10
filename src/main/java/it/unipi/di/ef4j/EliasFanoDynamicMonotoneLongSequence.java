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

import it.unimi.dsi.bits.BitVector;
import it.unimi.dsi.bits.Fast;
import it.unimi.dsi.bits.LongArrayBitVector;
import it.unimi.dsi.fastutil.longs.LongBigList;
import it.unimi.dsi.sux4j.bits.SimpleSelect;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.RandomAccess;
import java.util.ArrayList;

/**
 * The <tt>EliasFanoDynamicMonotoneLongSequence</tt> class represents a <em>dynamic</em> monotone
 * sequence of non-decreasing integers compressed with the <em>Elias-Fano integer encoding</em>.
 * 
 * <p>
 * It supports the <em>add</em>, <em>get</em>, <em>remove</em> and <em>next greater or equal</em>
 * operations, along with methods for inspecting how many bits the sequence is using, testing if the
 * sequence is empty, and iterating through the items in order.
 * </p>
 * 
 * @author Giulio Ermanno Pibiri
 */
public final class EliasFanoDynamicMonotoneLongSequence extends AbstractMonotoneLongSequence implements
    RandomAccess, Cloneable, Serializable {
  // Serial ID number.
  private transient static final long serialVersionUID = 8102012L;

  //Initial capacity of each index.
  protected transient static final int INITIAL_INDEX_CAPACITY = 2;
 
  // Backing Elias-Fano compressed sequence. It can behave in an append-only or dynamic way.
  protected EliasFanoAppendOnlyMonotoneLongSequence s;

  // Dynamic index that accumulates integers to be added or deleted from s.
  protected DynamicIndex di;

  // Flag to mean the dynamic-mode is ON/OFF.
  protected boolean dynamic;

  /**
   * Constructor for unknown initial capacity.
   * 
   * @param B the chosen bucket size.
   * @throws IllegalArgumentException if <tt>B</tt> is negative or zero.
   */
  public EliasFanoDynamicMonotoneLongSequence(final int B) {
    dynamic = false;
    s = new EliasFanoAppendOnlyMonotoneLongSequence(B);
  }

  /**
   * Constructor for known initial capacity.
   * 
   * @param B the chosen bucket size.
   * @param capacity the initial capacity.
   * @throws IllegalArgumentException if <tt>B</tt> is negative or zero.
   * @throws IllegalArgumentException if <tt>capacity</tt> is less than <tt>B</tt>.
   */
  public EliasFanoDynamicMonotoneLongSequence(final int B, final int capacity) {
    dynamic = false;
    s = new EliasFanoAppendOnlyMonotoneLongSequence(B, capacity);
  }

  /**
   * Dynamize the sequence: from an append-only sequence, it becomes a dynamic one.
   */
  public void dynamize() {
    if (!dynamic) {
      dynamic = true;
      di = new DynamicIndex();
    }
  }

  @Override
  public void clear() {
    s.clear();
    if (dynamic) {
      di = null;
      dynamic = false;
    }
    length = 0;
  }

  @Override
  public int bits() {
    return dynamic ? s.bits() + di.bits() : s.bits();
  }

  /**
   * Constructor for known initial capacity.
   * 
   * @return <tt>true</tt> if the sequence is dynamic; <tt>false</tt> otherwise.
   */
  public boolean isDynamic() {
    return dynamic;
  }

  @Override
  public void trimToSize() {
    s.trimToSize();
    if (dynamic) {
      di.trimToSize();
    }
  }

  @Override
  public boolean add(final Long integer) {
    if (dynamic) {
      di.add(integer);
    } else {
      s.add(integer);
    }
    length++;
    return true;
  }

  @Override
  public boolean remove(final long integer) {
    if (dynamic) {
      di.remove(integer);
      length--;
      return true;
    }
    return false;
  }

  @Override
  public Long get(final int index) {
    if (dynamic) {
      return di.get(index);
    }
    return s.get(index);
  }

  @Override
  public Long nextGEQ(final long integer) {
    int bucket = integer > 0 ? s.binarySearchOverInfo(integer, 0, s.buckets + 1) : 0;
    Iterator<Long> it;
    if (dynamic) {
      it = iterator(bucket);
    } else {
      it = s.iterator(bucket);
    }
    while (it.hasNext()) {
      long v = it.next();
      if (v >= integer) {
        return v;
      }
    }
    return -1L;
  }

  @Override
  public Iterator<Long> iterator() {
    if (dynamic) {
      return new EliasFanoDynamicMonotoneLongSequenceIterator<Long>();
    }
    return s.iterator();
  }

  private Iterator<Long> iterator(final int bucket) {
    return new EliasFanoDynamicMonotoneLongSequenceIterator<Long>(bucket);
  }

  @Override
  public Iterator<Long> iterator(final int from, final int to) {
    if (dynamic) {
      checkIndices(from, to);
      return new EliasFanoDynamicMonotoneLongSequenceIterator<Long>(from, to);
    }
    return s.iterator(from, to);
  }

  @Override
  public List<Long> subList(final int from, final int to) {
    checkIndices(from, to);
    
    final int B = (int) Math.sqrt(to - from + 1 << 3);
    EliasFanoDynamicMonotoneLongSequence subList = new EliasFanoDynamicMonotoneLongSequence(B);

    Iterator<Long> it = this.iterator(from, to);
    while (it.hasNext()) {
      subList.add(it.next());
    }
    return subList;
  }

  /**
   * Returns a copy of the object.
   * 
   * @return a copy of the object.
   */
  @Override
  public EliasFanoDynamicMonotoneLongSequence clone() {
    EliasFanoDynamicMonotoneLongSequence ds;
    if (dynamic) {
      ds = new EliasFanoDynamicMonotoneLongSequence(s, di);
    } else {
      ds = new EliasFanoDynamicMonotoneLongSequence(s);
    }
    ds.setLength(length);
    return ds;
  }

  private EliasFanoDynamicMonotoneLongSequence(EliasFanoAppendOnlyMonotoneLongSequence s) {
    dynamic = false;
    this.s = s.clone();
  }

  private EliasFanoDynamicMonotoneLongSequence(EliasFanoAppendOnlyMonotoneLongSequence s,
      DynamicIndex di) {
    dynamic = true;
    this.s = s.clone();
    this.di = di.clone();
  }

  private class EliasFanoDynamicMonotoneLongSequenceIterator<T> implements Iterator<Long> {
    int N = length;
    long a;
    long b;
    long c;
    long tmp;
    int next = 0;
    int bucket;
    Iterator<Long> itOverAdds;
    Iterator<Long> itOverDels;
    Iterator<Long> itOverBucket;
    final long LONG_MAX_VALUE = Long.MAX_VALUE;

    EliasFanoDynamicMonotoneLongSequenceIterator() {
      this.bucket = 0;
      init(0);
    }

    EliasFanoDynamicMonotoneLongSequenceIterator(final int bucket) {
      this.bucket = bucket;
      init(bucket);
    }

    void init(final int bucket) {
      itOverBucket =
          s.iterator(Integer.valueOf(bucket), bucket < s.buckets ? di.sizes.getInt(bucket) : s.N);
      itOverAdds = di.indices.get(bucket).additions.iterator();
      itOverDels = di.indices.get(bucket).deletions.iterator();
      a = next(itOverBucket);
      b = next(itOverAdds);
      c = next(itOverDels);
    }

    EliasFanoDynamicMonotoneLongSequenceIterator(final int from, final int to) {
      bucket = di.bs(from, 0, s.buckets + 1);
      init(bucket);
      while (next < from) {
        if (a < b && a < c) {
          next++;
          a = next(itOverBucket);
        } else if (b <= a && b < c) {
          next++;
          b = next(itOverAdds);
        } else if (b == c) {
          b = next(itOverAdds);
          c = next(itOverDels);
        } else {
          if (c == a) {
            a = next(itOverBucket);
            c = next(itOverDels);
          } else {
            c = next(itOverDels);
          }
        }
      }
      N = to + 1;
    }

    @Override
    public boolean hasNext() {
      return next < N;
    }

    @Override
    public Long next() {
      while (hasNext()) {
        while (a != LONG_MAX_VALUE || b != LONG_MAX_VALUE || c != LONG_MAX_VALUE) {
          if (a < b && a < c) {
            next++;
            tmp = a;
            a = next(itOverBucket);
            return tmp;
          } else if (b <= a && b < c) { // <= since we can add an integer more than one time
            next++;
            tmp = b;
            b = next(itOverAdds);
            return tmp;
          } else if (b == c) {
            b = next(itOverAdds);
            c = next(itOverDels);
          } else {
            if (c == a) {
              a = next(itOverBucket);
              c = next(itOverDels);
            } else {
              c = next(itOverDels);
            }
          }
        }
        bucket = bucket < s.buckets ? bucket + 1 : s.buckets;
        init(bucket);
      }
      throw new NoSuchElementException("Element not present.");
    }

    private long next(Iterator<Long> it) {
      return it.hasNext() ? it.next() : LONG_MAX_VALUE;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  // Record inner class useful during compression of a block.
  static private class CompressedBlock {
    LongArrayBitVector lowerBitsVector;
    BitVector upperBits;
  }

  // Record class that represents a bucket index.
  static private class Index {
    DynamicArray<Long> additions;
    DynamicArray<Long> deletions;
  }

  // DynamicIndex inner class.
  protected class DynamicIndex implements Cloneable {
    final int maxIndexCapacity;
    ArrayList<Index> indices;
    IntegerPrefixSumDynamicArray sizes;
    final int halfB;
    final int doubleB;

    DynamicIndex() {
      final int buckets = s.buckets;
      final int B = s.B;
      halfB = B >> 1;
      doubleB = B << 1;
      int c = B / (Fast.mostSignificantBit(length) << 1);

      if (c >> 1 < INITIAL_INDEX_CAPACITY) {
        throw new RuntimeException("B value is too small.");
      }

      if (c % 2 != 0) {
        c++;
      }

      maxIndexCapacity = c >> 1;
      indices = new ArrayList<Index>(buckets + 1);
      for (int i = 0; i <= buckets; i++) {
        indices.add(newIndex());
      }
      sizes = new IntegerPrefixSumDynamicArray(s.B, buckets);
    }

    long get(final int index) {
      if (index < 0 || index >= length) {
        throw new IndexOutOfBoundsException("" + index);
      }

      final int bucket = bs(index, 0, s.buckets + 1);
      final int sub = bucket > 0 ? sizes.get(bucket - 1) + indexSizeUpTo(bucket - 1) : 0;
      Iterator<Long> it = iterator(bucket);
      int from = 0;
      int to = index - sub;
      while (from++ < to) {
        it.next();
      }
      return it.next();
    }

    int bs(final long integer, final int i, final int j) {
      final int mid = (i + j) / 2;
      final long u1 =
          mid < s.buckets ? (sizes.get(mid) + indexSizeUpTo(mid))
              : (sizes.get(s.buckets - 1) + indexSizeUpTo(s.buckets - 1));

      if (integer == 0 || integer < sizes.get(0) + indexSizeUpTo(0)) {
        return 0;
      }
      if (integer == u1) {
        return mid;
      }
      if (integer < u1) {
        return bs(integer, i, mid);
      }
      final long u2 =
          mid < s.buckets - 1 ? (sizes.get(mid + 1) + indexSizeUpTo(mid + 1)) : (sizes
              .get(s.buckets - 1) + indexSizeUpTo(s.buckets - 1));
      if ((integer > u1 && integer < u2) || mid == s.buckets - 1) {
        return mid + 1;
      }
      return bs(integer, mid, j);
    }

    Index newIndex() {
      Index index = new Index();
      index.additions = new DynamicArray<Long>(INITIAL_INDEX_CAPACITY, maxIndexCapacity);
      index.deletions = new DynamicArray<Long>(INITIAL_INDEX_CAPACITY, maxIndexCapacity);
      return index;
    }

    void emptyIndex(Index index) {
      index.additions.clear(INITIAL_INDEX_CAPACITY);
      index.deletions.clear(INITIAL_INDEX_CAPACITY);
    }

    void trimToSize() {
      for (Index i : indices) {
        i.additions.trimToSize();
        i.deletions.trimToSize();
      }
      indices.trimToSize();
      sizes.trimToSize();
    }

    void add(final long integer) {
      final int bucket;
      Index index;

      if (integer >= s.last) { // just append in buffer
        bucket = s.buckets;
        index = indices.get(bucket);
        s.buffer[s.N++] = integer;
        s.last = integer;
      } else {
        bucket = s.binarySearchOverInfo(integer, 0, s.buckets + 1);
        index = indices.get(bucket);
        insert(index.additions, integer); // insertion in order in additions index
      }

      if (isAdditionsIndexFull(index) || isBufferFull()) {
        final int B = s.B;

        if (bucket != s.buckets) {
          final int curB = sizes.getInt(bucket);
          final int newB = curB + indexSize(index); // New bucket size
          long[] f = fusion(bucket, newB); // Fusion of bucket with index

          if (newB >= doubleB) { // split in two blocks
            s.info.insertLong(bucket + 1);
            CompressedBlock cb1 = compress(f, 0, B - 1, bucket);
            s.info.setLong(bucket + 1, f[B - 1] << 6);
            CompressedBlock cb2 = compress(f, B, newB - 1, bucket + 1);
            s.lowerBits.set(bucket, cb1.lowerBitsVector.bits());
            s.lowerBits.add(bucket + 1, cb2.lowerBitsVector.bits());
            s.selectors.set(bucket, new SimpleSelect(cb1.upperBits));
            s.selectors.add(bucket + 1, new SimpleSelect(cb2.upperBits));
            sizes.setInt(bucket, B);
            sizes.addInt(bucket + 1, newB - B);
            indices.add(bucket + 1, newIndex());
          } else {
            reconstruction(f, newB, bucket);
          }
        } else {
          final int newB = s.N + indexSize(index);

          if (newB < B) {
            fusion(B); // fusion of index with buffer
            s.N = newB;
          } else {
            long[] f = fusion(bucket, newB); // Merge and compress the buffer with the index.
                                             // The new B cannot cause any merge or split.
            sizes.addInt(bucket, newB);
            s.compress(f);
            s.N = 0;
            indices.add(newIndex());
          }
        }
        emptyIndex(index);
      }
    }

    void remove(final long integer) {
      if (integer == s.last && s.N > 0) {
        s.N--; // just remove last element from buffer
      } else {
        final int bucket = s.binarySearchOverInfo(integer, 0, s.buckets + 1);
        Index index = indices.get(bucket);
        insert(index.deletions, integer); // insertion in order in deletions index

        if (isDeletionsIndexFull(index)) {
          final int B = s.B;

          if (bucket != s.buckets) {
            final int curB = sizes.getInt(bucket);
            final int newB = curB + indexSize(index);
            long[] f = fusion(bucket, newB);

            if (newB <= halfB) {
              final int nextBlockDim =
                  (bucket == s.buckets ? s.N : sizes.getInt(bucket + 1))
                      + indexSize(indices.get(bucket + 1));
              final int finalBlockDim = newB + nextBlockDim;

              if (finalBlockDim < doubleB && nextBlockDim > 0) { // Merge with next block if we do
                                                                 // not incur in any splitting.
                merge(f, bucket, nextBlockDim);
              } else {
                reconstruction(f, newB, bucket);
              }
            } else {
              reconstruction(f, newB, bucket);
            }
          } else {
            final int newB = s.N + indexSize(index);
            fusion(B);
            s.N = newB;
          }
          emptyIndex(index);
        }
      }
    }

    void reconstruction(long[] f, final int to, final int bucket) {
      CompressedBlock cb = compress(f, 0, to - 1, bucket);
      s.lowerBits.set(bucket, cb.lowerBitsVector.bits());
      s.selectors.set(bucket, new SimpleSelect(cb.upperBits));
      sizes.setInt(bucket, to);
    }

    int indexSize(final Index index) {
      return index.additions.size() - index.deletions.size();
    }

    int indexSizeUpTo(final int index) {
      int sum = 0;
      for (int i = 0; i <= index; i++) {
        sum += indexSize(indices.get(i));
      }
      return sum;
    }

    CompressedBlock compress(final long[] array, final int from, final int to, final int bucket) {
      final int B = to - from + 1;
      final long lu = s.info.getLong(bucket);
      final long prevUpper = (lu & EliasFanoAppendOnlyMonotoneLongSequence.UPPER_BITS_MASK) >> 6;
      final long last = array[to];
      final long u = last - prevUpper;
      final long l = Math.max(0, Fast.mostSignificantBit(u / B));
      final long lowerBitsMask = (1L << l) - 1;

      LongArrayBitVector lowerBitsVector = LongArrayBitVector.getInstance();
      LongBigList lowerBitsList = lowerBitsVector.asLongBigList((int) l);
      lowerBitsList.size(B);
      BitVector upperBits = LongArrayBitVector.getInstance().length(B + (u >>> l) + 1);

      if (l != 0) {
        long v;
        for (int i = from; i <= to; i++) {
          v = array[i] - prevUpper;
          lowerBitsList.set(i - from, v & lowerBitsMask);
          upperBits.set((v >>> l) + i - from);
        }
      } else {
        for (int i = from; i <= to; i++) {
          upperBits.set(array[i] - prevUpper + i - from);
        }
      }

      s.info.setLong(bucket, (prevUpper << 6) | l);

      CompressedBlock cb = new CompressedBlock();
      cb.lowerBitsVector = lowerBitsVector;
      cb.upperBits = upperBits;

      return cb;
    }

    boolean isBufferFull() {
      return s.N + indexSize(indices.get(s.buckets)) == s.B;
    }

    boolean isAdditionsIndexFull(final Index index) {
      return index.additions.size() == maxIndexCapacity;
    }

    boolean isDeletionsIndexFull(final Index index) {
      return index.deletions.size() == maxIndexCapacity;
    }

    // Fusion of index with buffer.
    void fusion(final int newCapacity) {
      s.buffer = fusion(s.buckets, newCapacity);
    }

    long[] fusion(final int bucket, final int length) {
      long[] temp = new long[length];
      Iterator<Long> it = iterator(bucket);
      int i = 0;
      while (i < length) {
        temp[i++] = it.next();
      }
      return temp;
    }

    long[] fusion(final long[] s1, final long[] s2) {
      final int l1 = s1.length;
      final int l2 = s2.length;
      long[] temp = new long[l1 + l2];

      int i = 0;
      int j = 0;
      int k = 0;

      while (i < l1 && j < l2) {
        if (s1[i] < s2[j]) {
          temp[k++] = s1[i++];
        } else {
          temp[k++] = s2[j++];
        }
      }

      if (i == l1) {
        for (int l = j; l < l2; l++) {
          temp[k++] = s2[l];
        }
      } else {
        for (int l = i; l < l1; l++) {
          temp[k++] = s1[l];
        }
      }

      return temp;
    }

    void merge(long[] f, final int bucket, final int nextBlockDim) {
      long[] ff = fusion(bucket + 1, nextBlockDim);
      long[] fff = fusion(f, ff);
      final int finalLength = fff.length;

      CompressedBlock cb = compress(fff, 0, finalLength - 1, bucket);

      s.lowerBits.set(bucket, cb.lowerBitsVector.bits());
      s.selectors.set(bucket, new SimpleSelect(cb.upperBits));
      sizes.setInt(bucket, finalLength);

      if (bucket + 1 != s.buckets) {
        s.lowerBits.remove(bucket + 1);
        s.selectors.remove(bucket + 1);
        indices.remove(bucket + 1);
        sizes.removeInt(bucket + 1);
        s.info.removeLong(bucket + 1);
      } else {
        s.N = 0;
        emptyIndex(indices.get(s.buckets));
      }
    }

    void insert(DynamicArray<Long> index, final long integer) {
      final int l = index.size();
      int i = 0;
      while (i < l) {
        if (integer < index.get(i)) {
          index.add(i, integer);
          return;
        }
        i++;
      }
      index.add(integer);
    }

    int bits() {
      int bits = 0;
      for (Index index : indices) {
        bits += index.additions.capacity() + index.deletions.capacity();
      }
      return bits * Long.SIZE + sizes.bits();
    }

    @Override
    protected DynamicIndex clone() {
      return new DynamicIndex(maxIndexCapacity, halfB, doubleB, indices);
    }

    DynamicIndex(final int maxIndexCapacity, final int halfB, final int doubleB,
        ArrayList<Index> indices) {
      this.maxIndexCapacity = maxIndexCapacity;
      this.halfB = halfB;
      this.doubleB = doubleB;
      final int l = indices.size();
      ArrayList<Index> indicesClone = new ArrayList<Index>(l);
      for (int j = 0; j < l; j++) {
        Index index = indices.get(j);
        Index i = new Index();
        i.additions = clone(index.additions);
        i.deletions = clone(index.deletions);
        indicesClone.add(i);
      }
    }

    DynamicArray<Long> clone(DynamicArray<Long> array) {
      DynamicArray<Long> clone = new DynamicArray<Long>(INITIAL_INDEX_CAPACITY, maxIndexCapacity);
      final int l = array.size();
      for (int i = 0; i < l; i++) {
        clone.add(array.get(i));
      }
      return clone;
    }
  }

  public static final void main(String[] args) throws NumberFormatException, IOException {
    int B = 0;
    String fileName = "";
    String toBeAdded = "";

    if (args.length != 0) {
      B = Integer.parseInt(args[0]);
      fileName = args[1];
      toBeAdded = args[2];
    }

    EliasFanoDynamicMonotoneLongSequence ds = new EliasFanoDynamicMonotoneLongSequence(B);
    File file = new File(fileName);
    BufferedReader br = new BufferedReader(new FileReader(file));

    String str;
    while ((str = br.readLine()) != null) {
      ds.add(Long.parseLong(str));
    }
    br.close();

    ds.dynamize();

    BufferedReader bfr = new BufferedReader(new FileReader(new File(toBeAdded)));
    System.out.println("Adding...");
    while ((str = bfr.readLine()) != null) {
      ds.add(Long.parseLong(str));
    }
    bfr.close();

    for (int i = 0; i < 1000000; i++) {
    }

    Iterator<Long> it = ds.iterator();
    long start = System.currentTimeMillis();
    while (it.hasNext()) {
      it.next();
    }
    long end = System.currentTimeMillis();
    System.out.println(end - start);
  }
}
