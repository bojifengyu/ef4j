package it.unipi.di;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

/**
 * Unit tests for the <tt>EliasFanoAppendOnlyMonotoneLongSequence</tt> data type.
 * 
 * @author Giulio Ermanno Pibiri
 */
public class EliasFanoAppendOnlyMonotoneLongSequenceTest {
  private int B;
  private int length;
  private Long[] monotoneSequence;
  private EliasFanoAppendOnlyMonotoneLongSequence s;

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
    // Generate a random length in between 2M and 3M.
    length = (int) ((Math.random() * 1000000) + 2000000);

    // Generate a random maxGap in between 1000 and 2000.
    final int maxGap = (int) ((Math.random() * 1000) + 1000);

    monotoneSequence = monotoneSequenceGenerator(length, maxGap);

    B = (int) Math.sqrt(length << 3);
    s = new EliasFanoAppendOnlyMonotoneLongSequence(B);

    int l = 0;
    for (int i = 0; i < length; i++) {
      s.add(monotoneSequence[i]);
      l++;
    }

    assertEquals(l, length);
  }

  @Test
  public void firstConstructor() {
    // Generate a random B in between 4000 and 5000.
    final int B = (int) ((Math.random() * 1000) + 4000);

    s = new EliasFanoAppendOnlyMonotoneLongSequence(B);

    assertEquals(s.B, B);
    assertEquals(s.size(), 0);
    assertEquals(s.last, Long.MIN_VALUE);
    assertEquals(s.N, 0);
    assertTrue(s.lowerBits.isEmpty());
    assertTrue(s.selectors.isEmpty());
    assertEquals(s.info.size(), 1);
    assertEquals(s.info.capacity(), 2);
    assertEquals(s.info.array[0], 0L);
    assertEquals(s.buckets, 0);
  }

  @Test
  public void secondConstructor() {
    // Generate a random length in between 2M and 3M.
    final int length = (int) ((Math.random() * 1000000) + 2000000);

    // Generate a random B in between 4000 and 5000.
    final int B = (int) ((Math.random() * 1000) + 4000);

    final int buckets = length / B;
    s = new EliasFanoAppendOnlyMonotoneLongSequence(B, length);

    assertEquals(s.B, B);
    assertEquals(s.size(), 0);
    assertEquals(s.last, Long.MIN_VALUE);
    assertEquals(s.N, 0);
    assertEquals(s.lowerBits.capacity(), buckets);
    assertEquals(s.selectors.capacity(), buckets);
    assertEquals(s.info.capacity(), buckets);
    assertEquals(s.info.array[0], 0L);
    assertEquals(s.buckets, 0);
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

    assertEquals(s.size(), 0);
    assertEquals(s.last, Long.MIN_VALUE);
    assertEquals(s.N, 0);
    assertTrue(s.lowerBits.isEmpty());
    assertTrue(s.selectors.isEmpty());
    assertEquals(s.info.size(), 1);
    assertEquals(s.info.capacity(), 2);
    assertEquals(s.info.array[0], 0L);
    assertEquals(s.buckets, 0);
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
    assertEquals(s.nextGEQ(s.last + 100), new Long(-1L));


    assertEquals((long) s.nextGEQ(s.last), s.last);
    s.add(s.last + 100);
    assertEquals((long) s.nextGEQ(s.last), s.last);
    s.add(s.last + 350);
    assertEquals((long) s.nextGEQ(s.last), s.last);
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

    assertEquals((long) s.nextGEQ(0L), 0L);
    assertEquals((long) s.nextGEQ(4L), 4L);
    assertEquals((long) s.nextGEQ(3L), 3L);
    assertEquals((long) s.nextGEQ(10L), -1);

    s.add(23L);
    s.add(34L);
    s.add(34L);
    s.add(36L);
    s.add(39L);
    assertEquals((long) s.nextGEQ(36L), 36L);
  }

  @Test
  public void testClone() {
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

    EliasFanoAppendOnlyMonotoneLongSequence clone = s.clone();

    assertArrayEquals(s.toArray(), clone.toArray());

    s.add(s.last + 1);

    assertTrue(s.size() != clone.size());
  }

  @Test
  public void testAddAllCollectionOfQextendsLong() {
    buildSequence();
    ArrayList<Long> l = new ArrayList<Long>();
    long last = s.last;
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
    final long last = s.last;

    final long i1 = last + 12;
    final long i2 = last + 23;
    final long i3 = last + 54;

    s.add(i1);
    s.add(i2);
    s.add(i3);

    assertEquals((long) s.nextGEQ(i1), i1);
    assertEquals((long) s.nextGEQ(i2), i2);
    assertEquals((long) s.nextGEQ(i3), i3);

    assertTrue(s.contains(i3));
    assertTrue(s.contains(i1));
    assertTrue(s.contains(i2));
  }

  @Test
  public void testContainsAll() {
    buildSequence();
    ArrayList<Long> l = new ArrayList<Long>();
    long last = s.last;

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
    s.add(s.last);
    assertEquals(s.lastIndexOf(s.last), s.size() - 1);
  }
}
