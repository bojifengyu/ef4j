package it.unipi.di;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

/**
 * Unit tests for the <tt>EliasFanoDynamicMonotoneLongSequence</tt> data type.
 * 
 * @author Giulio Ermanno Pibiri
 */
public class EliasFanoDynamicMonotoneLongSequenceTest {
  private int B;
  private int length;
  private Long[] monotoneSequence;
  private EliasFanoDynamicMonotoneLongSequence s;
  private int N;
  private long[] toAdd;

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
    s = new EliasFanoDynamicMonotoneLongSequence(B);

    int l = 0;
    for (int i = 0; i < length; i++) {
      s.add(monotoneSequence[i]);
      l++;
    }

    assertEquals(l, length);
  }

  private void buildAdditions() {
    final int length = s.size();
    N = length / 10;
    toAdd = new long[length + N];
    for (int i = 0; i < length; i++) {
      toAdd[i] = monotoneSequence[i];
    }
    final int l = length + N;
    final long last = monotoneSequence[length - 1];
    for (int i = length; i < l; i++) {
      toAdd[i] = (long) (Math.random() * (last + N));
    }
  }

  @Test
  public void firstConstructor() {
    // Generate a random B in between 4000 and 5000.
    final int B = (int) ((Math.random() * 1000) + 4000);

    s = new EliasFanoDynamicMonotoneLongSequence(B);

    assertNotNull(s.s);
    assertEquals(s.s.B, B);
    assertEquals(s.size(), 0);
    assertEquals(s.s.last, Long.MIN_VALUE);
    assertEquals(s.s.N, 0);
    assertTrue(s.s.lowerBits.isEmpty());
    assertTrue(s.s.selectors.isEmpty());
    assertEquals(s.s.info.capacity(), 2);
    assertEquals(s.s.info.array[0], 0L);
    assertEquals(s.s.buckets, 0);
    assertFalse(s.isDynamic());
    assertNull(s.di);
  }

  @Test
  public void secondConstructor() {
    // Generate a random length in between 2M and 3M.
    final int length = (int) ((Math.random() * 1000000) + 2000000);

    // Generate a random B in between 4000 and 5000.
    final int B = (int) ((Math.random() * 1000) + 4000);

    s = new EliasFanoDynamicMonotoneLongSequence(B, length);
    final int buckets = length / B;

    assertNotNull(s.s);
    assertEquals(s.s.B, B);
    assertEquals(s.size(), 0);
    assertEquals(s.s.last, Long.MIN_VALUE);
    assertEquals(s.s.N, 0);
    assertTrue(s.s.lowerBits.isEmpty());
    assertTrue(s.s.selectors.isEmpty());
    assertEquals(s.s.info.capacity(), buckets);
    assertEquals(s.s.info.array[0], 0L);
    assertEquals(s.s.buckets, 0);
    assertFalse(s.isDynamic());
    assertNull(s.di);
  }

  @Test
  public void testDynamize() {
    buildSequence();
    assertFalse(s.isDynamic());
    assertNull(s.di);
    s.dynamize();
    assertTrue(s.isDynamic());
    assertNotNull(s.di);
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
    s.dynamize();
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
    EliasFanoDynamicMonotoneLongSequence s = new EliasFanoDynamicMonotoneLongSequence(1000);
    assertTrue(s.isEmpty());
    buildSequence();
    s.clear();
    assertTrue(s.isEmpty());
  }

  @Test
  public void testClear() {
    buildSequence();
    s.clear();
    assertNotNull(s.s);
    assertEquals(s.s.B, B);
    assertEquals(s.size(), 0);
    assertEquals(s.s.size(), 0);
    assertEquals(s.s.last, Long.MIN_VALUE);
    assertEquals(s.s.N, 0);
    assertTrue(s.s.lowerBits.isEmpty());
    assertTrue(s.s.selectors.isEmpty());
    assertEquals(s.s.info.capacity(), 2);
    assertEquals(s.s.info.array[0], 0L);
    assertEquals(s.s.buckets, 0);
    assertFalse(s.isDynamic());
    assertNull(s.di);
  }

  @Test
  public void testGet() {
    buildSequence();
    s.dynamize();
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
    s.dynamize();
    j = 0;
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
    s.dynamize();
    it = s.iterator(0, s.size() - 1);
    j = 0;
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
    assertEquals(s.nextGEQ(s.s.last + 100), new Long(-1L));

    assertEquals((long) s.nextGEQ(s.s.last), s.s.last);
    s.add(s.s.last + 100);
    assertEquals((long) s.nextGEQ(s.s.last), s.s.last);
    s.add(s.s.last + 350);
    assertEquals((long) s.nextGEQ(s.s.last), s.s.last);

    s.dynamize();

    assertEquals(s.nextGEQ(-(int) (Math.random() * (s.size() - 1))), first);
    assertEquals(s.nextGEQ(0), first);
    assertEquals(s.nextGEQ(s.s.last + 100), new Long(-1L));

    assertEquals((long) s.nextGEQ(s.s.last), s.s.last);
    s.add(s.s.last + 100);
    assertEquals((long) s.nextGEQ(s.s.last), s.s.last);
    s.add(s.s.last + 350);
    assertEquals((long) s.nextGEQ(s.s.last), s.s.last);
  }

  @Test
  public void testSubList() {
    EliasFanoDynamicMonotoneLongSequence s = new EliasFanoDynamicMonotoneLongSequence(4);

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
    assertEquals((long) s.nextGEQ(7L), 7L);
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
    EliasFanoDynamicMonotoneLongSequence s = new EliasFanoDynamicMonotoneLongSequence(4);

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

    EliasFanoDynamicMonotoneLongSequence clone = s.clone();

    assertArrayEquals(s.toArray(), clone.toArray());

    s.add(s.s.last + 1);

    assertTrue(s.size() != clone.size());
  }

  @Test
  public void testAddIntLong() {
    buildSequence();
    s.dynamize();
    buildAdditions();
    final int length = s.size();
    for (int i = 0; i < N; i++) {
      s.add(toAdd[length + i]);
    }
    assertEquals(length + N, s.size());
    Arrays.sort(toAdd);
    final int l = length + N;
    
//    long start = System.currentTimeMillis();
//    for (int i = 0; i < l; i++) {
//      s.get(i);
//    }
//    long end = System.currentTimeMillis();
//    System.out.println(end - start);
    
    for (int i = 0; i < l; i++) {
      assertEquals(s.get(i).longValue(), toAdd[i]);
    }
  }

  @Test
  public void testAddAllCollectionOfQextendsLong() {
    buildSequence();
    ArrayList<Long> l = new ArrayList<Long>();
    long last = s.s.last;
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
  public void testRemoveIntLong() {
    buildSequence();
    s.dynamize();
    buildAdditions();
    final int length = s.size();
    for (int i = 0; i < N; i++) {
      s.add(toAdd[length + i]);
    }
    for (int i = 0; i < N; i++) {
      s.remove(toAdd[length + i]);
    }

    final int length1 = s.size();
    assertEquals(length, length1);
    
//    long start = System.currentTimeMillis();
//    for (int i = 0; i < length1; i++) {
//      s.get(i);
//    }
//    long end = System.currentTimeMillis();
//    System.out.println(end - start);

    for (int i = 0; i < length1; i++) {
      assertEquals(s.get(i).longValue(), toAdd[i]);
    }
  }

  @Test
  public void testRemoveAll() {
    buildSequence();
    s.dynamize();
    buildAdditions();
    int n = s.size();
    ArrayList<Long> c = new ArrayList<Long>();
    ArrayList<Long> deletions = new ArrayList<Long>(n);
    for (int i = 0; i < n; i++) {
      deletions.add(monotoneSequence[i]);
    }
    for (int i = 0; i < N; i++) {
      c.add(monotoneSequence[(int) (Math.random() * (n - 1))]);
    }
    s.removeAll(c);
  }

  @Test
  public void testContains() {
    buildSequence();
    final long last = s.s.last;

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
    long last = s.s.last;

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
    EliasFanoDynamicMonotoneLongSequence s = new EliasFanoDynamicMonotoneLongSequence(4);

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
    s.add(s.s.last);
    assertEquals(s.lastIndexOf(s.s.last), s.size() - 1);
  }

  @Test
  public void testAddAllIntCollectionOfQextendsLong() {
    buildSequence();

    ArrayList<Long> l = new ArrayList<Long>();
    l.add(0L);
    l.add(1L);
    l.add(2L);
    l.add(3L);
    l.add(4L);

    try {
      s.addAll((int) (Math.random() * (s.size() - 1)), l);
    } catch (UnsupportedOperationException e) {
      assertTrue(true);
    }
    assertFalse(false);
  }

  @Test
  public void testRemoveObject() {
    buildSequence();
    try {
      s.remove((long) (Math.random() * s.s.last));
    } catch (UnsupportedOperationException e) {
      assertTrue(true);
    }
    assertFalse(false);
  }

  @Test
  public void testRetainAll() {
    buildSequence();
    ArrayList<Long> l = new ArrayList<Long>();
    long last = s.s.last;
    l.add(last++);
    l.add(last++);
    l.add(last++);
    l.add(last++);
    l.add(last++);
    try {
      s.retainAll(l);
    } catch (UnsupportedOperationException e) {
      assertTrue(true);
    }
    assertFalse(false);
  }

  @Test
  public void testSet() {
    buildSequence();
    try {
      s.set((int) (Math.random() * (s.size() - 1)), (long) (Math.random() * s.s.last));
    } catch (UnsupportedOperationException e) {
      assertTrue(true);
    }
    assertFalse(false);
  }

  @Test
  public void testToArrayTArray() {
    buildSequence();
    try {
      s.toArray(new Long[100]);
    } catch (UnsupportedOperationException e) {
      assertTrue(true);
    }
    assertFalse(false);
  }

  @Test
  public void testListIterator() {
    buildSequence();
    try {
      s.listIterator();
    } catch (UnsupportedOperationException e) {
      assertTrue(true);
    }
    assertFalse(false);
  }

  @Test
  public void testListIteratorInt() {
    buildSequence();
    try {
      s.listIterator((int) (Math.random() * (s.size() - 1)));
    } catch (UnsupportedOperationException e) {
      assertTrue(true);
    }
    assertFalse(false);
  }
}
