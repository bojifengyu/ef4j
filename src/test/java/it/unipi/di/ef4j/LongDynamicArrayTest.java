package it.unipi.di;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Unit tests for the <tt>LongDynamicArray</tt> data type.
 * 
 * @author Giulio Ermanno Pibiri
 */
public class LongDynamicArrayTest {
  private LongDynamicArray array;
  private long[] longs;
  private int length;

  private void randomInit() {
    // create an array of random length in between 20 and 70
    length = (int) (Math.random() * 50 + 20);
    longs = new long[length];
    for (int i = 0; i < length; i++) {
      longs[i] = (long) (Math.random() * 50);
    }
    array = new LongDynamicArray();
    for (long l : longs) {
      array.addLong(l);
    }
  }

  void print(LongDynamicArray array) {
    System.out.println();
    final int length = array.length();
    for (int i = 0; i < length; i++) {
      System.out.print(array.getLong(i) + " ");
    }
  }

  @Test
  public void testLongResizingArray() {
    LongDynamicArray array = new LongDynamicArray();
    assertEquals(array.array.length, 2);
    assertEquals(array.length, 0);
    assertEquals(LongDynamicArray.INITIAL_CAPACITY, 2);
  }

  @Test
  public void testLongResizingArrayInt() {
    final int capacity = (int) (Math.random() * 100);
    LongDynamicArray array = new LongDynamicArray(capacity);
    assertEquals(array.array.length, capacity);
    assertEquals(array.length, 0);
    assertEquals(LongDynamicArray.INITIAL_CAPACITY, 2);
  }

  @Test
  public void testClear() {
    randomInit();
    assertTrue(array.length > 0);
    array.clear();
    assertEquals(array.array.length, 2);
    assertEquals(array.length, 0);
    assertEquals(LongDynamicArray.INITIAL_CAPACITY, 2);
  }

  @Test
  public void testAddLong() {
    randomInit();
    for (long l : longs) {
      array.addLong(l);
    }
    final int length = longs.length;
    final int doubleLength = length << 1;
    for (int i = 0; i < length; i++) {
      assertEquals(array.getLong(i), longs[i]);
    }
    for (int i = length; i < doubleLength; i++) {
      assertEquals(array.getLong(i), longs[i - length]);
    }
  }

  @Test
  public void testSetLong() {
    randomInit();
    final int randomIndex = (int) (Math.random() * array.length);
    final long randomLong = (long) (Math.random() * 100);
    array.setLong(randomIndex, randomLong);
    assertEquals(array.getLong(randomIndex), randomLong);
  }

  @Test
  public void testGetLong() {
    randomInit();
    final int length = array.length;
    for (int i = 0; i < length; i++) {
      assertEquals(array.getLong(i), longs[i]);
    }
  }

  @Test
  public void testLength() {
    randomInit();
    assertEquals(length, array.length());
  }

  @Test
  public void testCapacity() {
    randomInit();
    assertTrue(array.capacity() > 0);
  }

  @Test
  public void testTrimToSize() {
    array = new LongDynamicArray();
    longs = new long[] {0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L};
    for (long l : longs) {
      array.addLong(l);
    }
    assertTrue(array.length() < array.capacity());
    final int length = longs.length;
    for (int i = 0; i < length; i++) {
      assertEquals(array.getLong(i), longs[i]);
    }
  }

  @Test
  public void testInsertLong() {
    array = new LongDynamicArray();
    longs = new long[] {0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L};
    for (long l : longs) {
      array.addLong(l);
    }
    array.insertLong(3);
    assertArrayEquals(array.toArray(), new long[] {0L, 1L, 2L, 3L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L});
    array.insertLong(1);
    assertArrayEquals(array.toArray(), new long[] {0L, 1L, 1L, 2L, 3L, 3L, 4L, 5L, 6L, 7L, 8L, 9L,
        10L});
    array.insertLong(7);
    assertArrayEquals(array.toArray(), new long[] {0L, 1L, 1L, 2L, 3L, 3L, 4L, 5L, 5L, 6L, 7L, 8L,
        9L, 10L});
    array.insertLong(13);
    assertArrayEquals(array.toArray(), new long[] {0L, 1L, 1L, 2L, 3L, 3L, 4L, 5L, 5L, 6L, 7L, 8L,
        9L, 10L, 10L});
    array.insertLong(0);
    assertArrayEquals(array.toArray(), new long[] {0L, 0L, 1L, 1L, 2L, 3L, 3L, 4L, 5L, 5L, 6L, 7L,
        8L, 9L, 10L, 10L});
    array.insertLong(1);
    assertArrayEquals(array.toArray(), new long[] {0L, 0L, 0L, 1L, 1L, 2L, 3L, 3L, 4L, 5L, 5L, 6L,
        7L, 8L, 9L, 10L, 10L});
  }

  @Test
  public void testRemoveLong() {
    array = new LongDynamicArray();
    longs = new long[] {0L, 0L, 0L, 1L, 1L, 2L, 3L, 3L, 4L, 5L, 5L, 6L, 7L, 8L, 9L, 10L, 10L};
    for (long l : longs) {
      array.addLong(l);
    }
    array.removeLong(1);
    assertArrayEquals(array.toArray(), new long[] {0L, 0L, 1L, 1L, 2L, 3L, 3L, 4L, 5L, 5L, 6L, 7L,
        8L, 9L, 10L, 10L});
    array.removeLong(0);
    assertArrayEquals(array.toArray(), new long[] {0L, 1L, 1L, 2L, 3L, 3L, 4L, 5L, 5L, 6L, 7L, 8L,
        9L, 10L, 10L});
    array.removeLong(13);
    assertArrayEquals(array.toArray(), new long[] {0L, 1L, 1L, 2L, 3L, 3L, 4L, 5L, 5L, 6L, 7L, 8L,
        9L, 10L});
    array.removeLong(7);
    assertArrayEquals(array.toArray(), new long[] {0L, 1L, 1L, 2L, 3L, 3L, 4L, 5L, 6L, 7L, 8L, 9L,
        10L});
    array.removeLong(1);
    assertArrayEquals(array.toArray(), new long[] {0L, 1L, 2L, 3L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L});
    array.removeLong(3);
  }
}
