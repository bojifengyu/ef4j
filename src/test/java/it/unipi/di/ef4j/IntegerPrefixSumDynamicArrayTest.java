package it.unipi.di;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Unit tests for the <tt>IntegerPrefixSumDynamicArray</tt> data type.
 * 
 * @author Giulio Ermanno Pibiri
 */
public class IntegerPrefixSumDynamicArrayTest {
  private IntegerPrefixSumDynamicArray array;
  private int[] ps;
  private int length;
  private int initValue;

  private void randomInit() {
    // generate a random B in between 10 and 25
    initValue = (int) (Math.random() * 10 + 15);
    // generate a random length in between 10 and 15
    length = (int) (Math.random() * 10 + 5);
    ps = new int[length];
    array = new IntegerPrefixSumDynamicArray(initValue, length);
    int prefixSum = 0;
    for (int i = 0; i < length; i++) {
      ps[i] = prefixSum += initValue;
    }
  }

  void print(IntegerPrefixSumDynamicArray array) {
    System.out.println();
    final int length = array.size();
    for (int i = 0; i < length; i++) {
      System.out.print(array.array[i] + " ");
    }
  }

  @Test
  public void testIntegerPrefixSumDynamicArray() {
    randomInit();
    assertArrayEquals(array.toArray(), ps);
  }

  @Test
  public void testGet() {
    randomInit();
    final int l = length;
    for (int i = 0; i < l; i++) {
      assertEquals(array.array[i], ps[i]);
    }
  }

  @Test
  public void testGetInt() {
    randomInit();
    final int l = length;
    for (int i = 0; i < l; i++) {
      assertEquals(array.getInt(i), initValue);
    }
  }

  @Test
  public void testSetInt() {
    array = new IntegerPrefixSumDynamicArray(10, 5);
    array.setInt(0, 5);
    assertArrayEquals(array.toArray(), new int[] {5, 15, 25, 35, 45});
    array.setInt(1, 17);
    assertArrayEquals(array.toArray(), new int[] {5, 22, 32, 42, 52});
    array.setInt(4, 65);
    assertArrayEquals(array.toArray(), new int[] {5, 22, 32, 42, 107});
    array.setInt(2, 28);
    assertArrayEquals(array.toArray(), new int[] {5, 22, 50, 60, 125});
  }

  @Test
  public void testAddRemoveInt() {
    array = new IntegerPrefixSumDynamicArray(2, 5);
    array.addInt(2, 5);
    assertArrayEquals(array.toArray(), new int[] {2, 4, 9, 11, 13, 15});
    array.addInt(0, 3);
    assertArrayEquals(array.toArray(), new int[] {3, 5, 7, 12, 14, 16, 18});
    array.addInt(0, 3);
    assertArrayEquals(array.toArray(), new int[] {3, 6, 8, 10, 15, 17, 19, 21});
    array.addInt(4, 12);
    assertArrayEquals(array.toArray(), new int[] {3, 6, 8, 10, 22, 27, 29, 31, 33});
    array.addInt(7, 34);
    assertArrayEquals(array.toArray(), new int[] {3, 6, 8, 10, 22, 27, 29, 63, 65, 67});
    array.removeInt(7);
    assertArrayEquals(array.toArray(), new int[] {3, 6, 8, 10, 22, 27, 29, 31, 33});
    array.removeInt(4);
    assertArrayEquals(array.toArray(), new int[] {3, 6, 8, 10, 15, 17, 19, 21});
    array.removeInt(0);
    assertArrayEquals(array.toArray(), new int[] {3, 5, 7, 12, 14, 16, 18});
    array.removeInt(0);
    assertArrayEquals(array.toArray(), new int[] {2, 4, 9, 11, 13, 15});
    array.removeInt(2);
    assertArrayEquals(array.toArray(), new int[] {2, 4, 6, 8, 10});
    array.addInt(5, 3);
    assertArrayEquals(array.toArray(), new int[] {2, 4, 6, 8, 10, 13});
    array.addInt(6, 3);
    assertArrayEquals(array.toArray(), new int[] {2, 4, 6, 8, 10, 13, 16});
    array.addInt(7, 3);
    assertArrayEquals(array.toArray(), new int[] {2, 4, 6, 8, 10, 13, 16, 19});
    array.addInt(8, 5);
    assertArrayEquals(array.toArray(), new int[] {2, 4, 6, 8, 10, 13, 16, 19, 24});
  }

  @Test
  public void testBits() {
    randomInit();
    assertEquals(array.bits(), array.size() * Integer.SIZE);
  }

  @Test
  public void testTrimToSize() {
    array = new IntegerPrefixSumDynamicArray(10, 5);
    assertTrue(array.size() <= array.capacity());
    array.trimToSize();
    assertArrayEquals(array.toArray(), new int[] {10, 20, 30, 40, 50});
  }

  @Test
  public void testLength() {
    randomInit();
    assertEquals(length, array.size());
  }
}
