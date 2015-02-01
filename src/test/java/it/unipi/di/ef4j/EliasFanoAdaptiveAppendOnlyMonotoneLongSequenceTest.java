package it.unipi.di;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

/**
 * Unit tests for the <tt>EliasFanoAdaptiveAppendOnlyMonotoneLongSequence</tt> data type.
 * 
 * @author Giulio Ermanno Pibiri
 */
public class EliasFanoAdaptiveAppendOnlyMonotoneLongSequenceTest {
  private int length;
  private Long[] monotoneSequence;
  private EliasFanoAdaptiveAppendOnlyMonotoneLongSequence s;

  private Long[] monotoneSequenceGenerator(final int length, final int maxGap) {
    Long[] monotoneSequence = new Long[length];

    long prevInt = 0L;
    for (int i = 0; i < length; i++) {
      prevInt += (long) (Math.random() * (maxGap - 1) + 1);
      monotoneSequence[i] = prevInt;
    }
    return monotoneSequence;
  }

  private void buildSequence() {
    // Generate a random length in between 2.5M and 3.5M.
    length = (int) ((Math.random() * 1000000) + 2500000);

    // Generate a random maxGap in between 1000 and 2000.
    final int maxGap = (int) ((Math.random() * 1000) + 1000);

    monotoneSequence = monotoneSequenceGenerator(length, maxGap);

    s = new EliasFanoAdaptiveAppendOnlyMonotoneLongSequence();

    int l = 0;
    for (int i = 0; i < length; i++) {
      s.add(monotoneSequence[i]);
      l++;
    }

    assertEquals(l, length);
  }

  @Test
  public void firstConstructor() {
    s = new EliasFanoAdaptiveAppendOnlyMonotoneLongSequence();

    assertEquals(s.B, 32);
    assertEquals(s.B0, 32);
    assertEquals(s.n, 128);
    assertEquals(s.u, 0L);
    assertEquals(s.next, -1);
    assertEquals(s.n0, 2097152);
    assertEquals(s.msbn0, 21);
    assertEquals(s.chunks.size(), 1);
  }

  @Test
  public void secondConstructor() {
    final int B = 16;
    s = new EliasFanoAdaptiveAppendOnlyMonotoneLongSequence(B);

    assertEquals(s.B, 16);
    assertEquals(s.B0, 16);
    assertEquals(s.n, 32);
    assertEquals(s.u, 0L);
    assertEquals(s.next, -1);
    assertEquals(s.n0, 524288);
    assertEquals(s.msbn0, 19);
    assertEquals(s.chunks.size(), 1);
  }

  @Test
  public void testSize() {
    buildSequence();
    assertEquals(s.size(), length);
  }

  @Test
  public void testBits() {
    buildSequence();
    assertTrue(s.bits() > 0);
  }

  @Test
  public void testTrimToSize() {
    buildSequence();
    final int numOfBitsBefore = s.bits();
    s.trimToSize();
    final int numOfBitsAfter = s.bits();
    assertTrue(numOfBitsBefore > numOfBitsAfter);
  }

  @Test
  public void testIsEmpty() {
    EliasFanoAppendOnlyMonotoneLongSequence s = new EliasFanoAppendOnlyMonotoneLongSequence(1000);
    assertTrue(s.isEmpty());

    buildSequence();
    s.clear();
    assertTrue(s.isEmpty());
  }

  @Test
  public void testClear() {
    buildSequence();

    s.clear();

    assertEquals(s.B, 32);
    assertEquals(s.B0, 32);
    assertEquals(s.n, 128);
    assertEquals(s.u, 0L);
    assertEquals(s.next, -1);
    assertEquals(s.n0, 2097152);
    assertEquals(s.msbn0, 21);
    assertEquals(s.chunks.size(), 1);
  }

  @Test
  public void testGet() {
    buildSequence();
    for (int i = 0; i < s.size(); i++) {
      assertEquals(s.get(i), monotoneSequence[i]);
    }
  }

  @Test
  public void testIterator() {
    buildSequence();
    int j = 0;
    for (Long i : s) {
      assertEquals(i, monotoneSequence[j++]);
    }
  }

  @Test
  public void testIteratorIntInt() {
    buildSequence();
    Iterator<Long> it = s.iterator(0, s.size() - 1);
    int j = 0;
    while (it.hasNext()) {
      assertEquals(it.next(), monotoneSequence[j++]);
    }
  }

  @Test
  public void testNextGEQ() {
    buildSequence();

    final Long first = s.get(0);
    assertEquals(s.nextGEQ(-(int) (Math.random() * (s.size() - 1))), first);
    assertEquals(s.nextGEQ(0), first);
    long last = s.get(s.length - 1);
    assertEquals(s.nextGEQ(last + 100), new Long(-1L));

    final long integer = (long) (Math.random() * last);
    final long nextGEQ = s.nextGEQ(integer);
    final int position = s.indexOf(nextGEQ);

    assertTrue(nextGEQ > s.get(position - 1));
  }

  @Test
  public void testSubList() {
    EliasFanoAppendOnlyMonotoneLongSequence s = new EliasFanoAppendOnlyMonotoneLongSequence(4);

    s.add(0L);
    s.add(1L);
    s.add(2L);
    s.add(3L);
    s.add(4L);
    s.add(5L);
    s.add(6L);
    s.add(7L);
    s.add(8L);
    s.add(9L);

    List<Long> subList = s.subList(2, 6);

    assertArrayEquals(subList.toArray(), new Long[] {2L, 3L, 4L, 5L, 6L});
  }

  @Test
  public void testClone() {
    EliasFanoAdaptiveAppendOnlyMonotoneLongSequence s =
        new EliasFanoAdaptiveAppendOnlyMonotoneLongSequence(16);

    s.add(0L);
    s.add(1L);
    s.add(2L);
    s.add(3L);
    s.add(4L);
    s.add(5L);
    s.add(6L);
    s.add(7L);
    s.add(8L);
    s.add(9L);
    s.add(10L);
    s.add(11L);
    s.add(12L);
    s.add(13L);
    s.add(14L);
    s.add(15L);
    s.add(16L);
    s.add(17L);
    s.add(18L);
    s.add(19L);

    EliasFanoAdaptiveAppendOnlyMonotoneLongSequence clone = s.clone();

    assertArrayEquals(s.toArray(), clone.toArray());

    s.add(s.get(s.length - 1) + 1);

    assertTrue(s.size() != clone.size());
  }

  @Test
  public void testAddAllCollectionOfQextendsLong() {
    buildSequence();
    ArrayList<Long> l = new ArrayList<Long>();
    long last = s.get(s.length - 1);
    final long lastIntPosition = s.size() - 1;

    l.add(++last);
    l.add(++last);
    l.add(++last);
    l.add(++last);
    l.add(++last);

    s.addAll(l);

    assertEquals(s.indexOf(last--), lastIntPosition + 5);
    assertEquals(s.indexOf(last--), lastIntPosition + 4);
    assertEquals(s.indexOf(last--), lastIntPosition + 3);
    assertEquals(s.indexOf(last--), lastIntPosition + 2);
    assertEquals(s.indexOf(last--), lastIntPosition + 1);
  }

  @Test
  public void testContains() {
    buildSequence();
    long last = s.get(s.length - 1);

    s.add(++last);
    s.add(++last);
    s.add(++last);
    s.add(++last);
    s.add(++last);

    assertTrue(s.contains(last--));
    assertTrue(s.contains(last--));
    assertTrue(s.contains(last--));
    assertTrue(s.contains(last--));
    assertTrue(s.contains(last--));
  }

  @Test
  public void testContainsAll() {
    buildSequence();
    ArrayList<Long> l = new ArrayList<Long>();
    long last = s.get(s.length - 1);

    l.add(++last);
    l.add(++last);
    l.add(++last);
    l.add(++last);
    l.add(++last);

    s.addAll(l);

    assertTrue(s.containsAll(l));
  }

  @Test
  public void testToArray() {
    EliasFanoAppendOnlyMonotoneLongSequence s = new EliasFanoAppendOnlyMonotoneLongSequence(4);

    s.add(0L);
    s.add(1L);
    s.add(2L);
    s.add(3L);
    s.add(4L);
    s.add(5L);
    s.add(6L);
    s.add(7L);
    s.add(8L);
    s.add(9L);

    assertArrayEquals(s.toArray(), new Long[] {0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L});
  }

  @Test
  public void testLastIndexOf() {
    buildSequence();
    s.add(s.get(s.length - 1));
    assertEquals(s.lastIndexOf(s.get(s.length - 1)), s.size() - 1);
  }
}
