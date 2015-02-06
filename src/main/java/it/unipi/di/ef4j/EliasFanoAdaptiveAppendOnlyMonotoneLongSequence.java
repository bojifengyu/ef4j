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

import it.unimi.dsi.bits.Fast;

/**
 * The <tt>EliasFanoAdaptiveAppendOnlyMonotoneLongSequence</tt> class represents a monotone sequence
 * of non-decreasing integers compressed with the <em>Elias-Fano integer encoding</em>. The
 * implementation maintains a collection of chunks that are <tt>AppendOnlyEliasFano</tt> structures,
 * along with an <em>adaptive strategy</em> to properly change bucket size when the sequence is
 * growing in dimension.
 * 
 * <p>
 * It supports the <em>append</em>, <em>get</em> and <em>next greater or equal</em> operations,
 * along with methods for inspecting how many bits the sequence is using, testing if the sequence is
 * empty, and iterating through the items in order.
 * </p>
 * 
 * @author Giulio Ermanno Pibiri
 */
public final class EliasFanoAdaptiveAppendOnlyMonotoneLongSequence extends
    AbstractAppendOnlyMonotoneLongSequence implements Cloneable, Serializable {
  // Serial ID number.
  private transient static final long serialVersionUID = 26092009L;

  // Initial bucket size.
  protected int B0;

  // Current bucket size.
  protected int B;

  // Number of integers to be added before changing B.
  protected int n;

  // Threshold for which we change strategy of choosing bucket size.
  protected int n0;

  // Most significant bit of n0.
  protected int msbn0;

  // Controls next value of bucket size.
  protected int next;

  // Resizing array of chunks, each of these being an append-only Elias-Fano data structure.
  protected DynamicArray<Chunk> chunks;

  // Pointer to current chunk.
  protected Chunk chunk;

  // Current chunk upper bound.
  protected long u;

  /**
   * Constructor with unspecified initial bucket size. Default initial bucket size is 32.
   */
  public EliasFanoAdaptiveAppendOnlyMonotoneLongSequence() {
    B = B0 = 32;
    n = 128;
    u = 0L;

    chunks = new DynamicArray<Chunk>();
    next = -1;

    // First chunk that will be reconstructed.
    chunk = new Chunk();
    chunk.s = new EliasFanoAppendOnlyMonotoneLongSequence(32);
    chunk.prevUpper = 0L;
    chunks.add(chunk);

    n0 = 2097152;
    msbn0 = 21;
  }

  /**
   * Constructor with specified initial bucket size.
   * 
   * @param B the specified initial bucket size.
   * @throws IllegalArgumentException if <tt>B</tt> is less than 16.
   */
  public EliasFanoAdaptiveAppendOnlyMonotoneLongSequence(final int B) {
    if (B < 16) {
      throw new IllegalArgumentException("Inital choice of bucket size cannot be less than 16.");
    }

    this.B = B0 = B;
    n = B * B >> 3;
    u = 0L;

    chunks = new DynamicArray<Chunk>();
    next = -1;

    // First chunk that will be reconstructed.
    chunk = new Chunk();
    chunk.s = new EliasFanoAppendOnlyMonotoneLongSequence(B);
    chunk.prevUpper = 0L;
    chunks.add(chunk);

    final int B7 = B << 7;
    n0 = B7 * B7 >> 3;
    msbn0 = Fast.mostSignificantBit(n0);
  }

  @Override
  public void clear() {
    length = 0;
    B = B0;
    n = B0 * B0 >> 3;
    u = 0L;

    chunks.clear();
    next = -1;

    chunk = new Chunk();
    chunk.s = new EliasFanoAppendOnlyMonotoneLongSequence(B0);
    chunk.prevUpper = 0L;
    chunks.add(chunk);

    final int B7 = B0 << 7;
    n0 = B7 * B7 >> 3;
    msbn0 = Fast.mostSignificantBit(n0);
  }

  @Override
  public Iterator<Long> iterator() {
    return new EliasFanoAdaptiveAppendOnlyMonotoneLongSequenceIterator<Long>();
  }

  @Override
  public Iterator<Long> iterator(final int from, final int to) {
    checkIndices(from, to);
    return new EliasFanoAdaptiveAppendOnlyMonotoneLongSequenceIterator<Long>(from, to);
  }

  // Iterator inner class.
  private class EliasFanoAdaptiveAppendOnlyMonotoneLongSequenceIterator<T> implements
      Iterator<Long> {
    int next = 0;
    int chunkId = 1;
    Chunk c = chunks.get(0);
    EliasFanoAppendOnlyMonotoneLongSequence chunk = c.s;
    long u = c.prevUpper;
    Iterator<Long> it = chunk.iterator();
    final int N;

    EliasFanoAdaptiveAppendOnlyMonotoneLongSequenceIterator() {
      N = length;
    }

    EliasFanoAdaptiveAppendOnlyMonotoneLongSequenceIterator(final int from, final int to) {
      next = from;
      chunkId = chunk(from);
      Chunk c = chunks.get(chunkId++);
      chunk = c.s;
      u = c.prevUpper;
      it =
          chunk.iterator(from <= chunk.length ? from : from % (length - chunk.length),
              to <= chunk.length ? to : chunk.length - 1);
      N = to + 1;
    }

    @Override
    public boolean hasNext() {
      return next < N;
    }

    @Override
    public Long next() {
      while (hasNext()) {
        while (it.hasNext()) {
          next++;
          return it.next() + u;
        }
        Chunk c = chunks.get(chunkId++);
        chunk = c.s;
        it = chunk.iterator();
        u = c.prevUpper;
        next++;
        return it.next() + u;
      }
      throw new NoSuchElementException("Element not present.");
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  @Override
  public boolean add(final Long integer) {
    if (length > n) {
      next++;
      if (next < 7) {
        B = B << 1;
        n = B * B >> 3;
        EliasFanoAppendOnlyMonotoneLongSequence tmp =
            new EliasFanoAppendOnlyMonotoneLongSequence(B, chunk.s.length << 1);
        for (Long i : chunk.s) {
          tmp.add(i);
        }
        chunk.s = tmp;
        chunks.set(0, chunk);
      } else {
        u = chunk.s.buffer[chunk.s.N - 1];
        n = n << 1;
        B = (int) Math.sqrt(n << 2);
        chunk = new Chunk();
        chunk.s = new EliasFanoAppendOnlyMonotoneLongSequence(B);
        chunks.add(chunk);
        chunk.prevUpper = u;
      }
    }
    chunk.s.add(integer - u);
    length++;
    return true;
  }

  // Routine that identifies the chunk id given an index.
  private int chunk(final int index) {
    final int d = Fast.mostSignificantBit(index) - msbn0;
    final int MASK = d >> 31;
    final int x = d + ((d + MASK) ^ MASK) >> 1;
    return x + ((n0 << x) - index >>> 31);
  }

  @Override
  public Long get(final int index) {
    final int id = chunk(index);
    Chunk c = chunks.get(id);
    return c.s.get(index - (((id - 1) >>> 31) ^ 1) * ((n0 << id - 1) + 1)) + c.prevUpper;
  }

  @Override
  public Long nextGEQ(final long integer) {
    final int chunk = binarySearchOverPrevUpper(integer);
    Chunk c = chunks.get(chunk);
    final long u = c.prevUpper;
    final long result = c.s.nextGEQ(integer - u);
    return result == -1L ? -1L : result + u;
  }
  
  // Binary search over previous upper bounds.
  private int binarySearchOverPrevUpper(final long integer) {
    if (integer <= chunks.get(0).prevUpper) {
      return 0;
    }
    final long last = chunks.get(chunks.length - 1).prevUpper;
    if (integer >= last) {
      return chunks.length - 1;
    }
    
    int lo = 0;
    int hi = chunks.length; 
    
    while (lo <= hi) {
      final int mid = lo + (hi - lo) / 2;
      final long u1 = chunks.get(mid).prevUpper;
      if (integer == u1) {
        return mid - 1;
      }
      final long u2 = mid < chunks.length - 1 ? chunks.get(mid + 1).prevUpper : last;
      if (integer >= u2 && u2 < last) {
        lo = mid + 1;
      }
      else if (u1 < integer && integer < u2) {
        return mid;
      }
      else {
        hi = mid - 1;
      }
    }
    
    return -1;
  }

  @Override
  public List<Long> subList(final int from, final int to) {
    checkIndices(from, to);

    EliasFanoAdaptiveAppendOnlyMonotoneLongSequence subList =
        new EliasFanoAdaptiveAppendOnlyMonotoneLongSequence();

    Iterator<Long> it = this.iterator(from, to);
    while (it.hasNext()) {
      subList.add(it.next());
    }
    return subList;
  }

  @Override
  public int bits() {
    int bits = 0;
    for (Chunk c : chunks) {
      bits += c.s.bits();
    }
    return bits;
  }

  @Override
  public void trimToSize() {
    for (Chunk c : chunks) {
      c.s.trimToSize();
    }
  }

  // Chunk private record class.
  static private class Chunk {
    EliasFanoAppendOnlyMonotoneLongSequence s;
    long prevUpper;
  }

  // private constructor
  private EliasFanoAdaptiveAppendOnlyMonotoneLongSequence(int B, int length, int n, int n0,
      int next, long u, DynamicArray<Chunk> chunks, Chunk chunk) {
    this.B = B;
    this.length = length;
    this.n = n;
    this.n0 = n0;
    this.msbn0 = Fast.mostSignificantBit(n0);
    this.next = next;
    this.u = u;

    final int numOfChunks = chunks.size();

    DynamicArray<Chunk> chunksClone = new DynamicArray<Chunk>(numOfChunks);

    for (Chunk c : chunks) {
      Chunk clone = new Chunk();
      clone.s = c.s.clone();
      clone.prevUpper = c.prevUpper;
      chunksClone.add(clone);
    }

    this.chunks = chunksClone;
    Chunk c = new Chunk();
    c.prevUpper = chunk.prevUpper;
    c.s = chunk.s.clone();
    this.chunk = c;
  }

  /**
   * Returns a copy of the object.
   * 
   * @return a copy of the object.
   */
  @Override
  public EliasFanoAdaptiveAppendOnlyMonotoneLongSequence clone() {
    return new EliasFanoAdaptiveAppendOnlyMonotoneLongSequence(B, length, n, n0, next, u, chunks,
        chunk);
  }
}
