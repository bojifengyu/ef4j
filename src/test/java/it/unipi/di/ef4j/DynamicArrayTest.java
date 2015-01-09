package it.unipi.di;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

import java.util.ArrayList;
import java.util.Iterator;

import org.junit.Test;

/**
 * Unit tests the <tt>DynamicArray</tt> data type.
 * 
 * @author Giulio Ermanno Pibiri
 */
public class DynamicArrayTest {

  // Generate a random length in between 100 and 200.
  private final int length = (int) ((Math.random() * 100) + 100);
  private DynamicArray<Integer> array;

  private void buildArray() {
    final int length = this.length;
    array = new DynamicArray<Integer>();
    for (int i = 0; i < length; i++) {
      array.add(i);
    }
  }

  @Test
  public void firstConstructor() {
    DynamicArray<Integer> array = new DynamicArray<Integer>();
    assertEquals(array.capacity(), 2);
    assertEquals(array.size(), 0);
  }

  @Test
  public void secondConstructor() {
    final int capacity = (int) (Math.random() * 100);
    DynamicArray<Integer> array = new DynamicArray<Integer>(capacity, capacity * 100);
    assertEquals(array.capacity(), capacity);
    assertEquals(array.length, 0);
    assertEquals(array.maxCapacity, capacity * 100);
  }

  @Test
  public void thirdConstructor() {
    Integer[] ints = new Integer[5];

    ints[0] = 0;
    ints[1] = 1;
    ints[2] = 2;
    ints[3] = 3;
    ints[4] = 4;

    DynamicArray<Integer> array = new DynamicArray<Integer>(ints, Integer.MAX_VALUE);
    assertArrayEquals(array.array, ints);
    assertEquals(array.length, ints.length);
  }

  @Test
  public void testClear() {
    buildArray();
    array.clear();
    assertEquals(array.capacity(), 2);
    assertEquals(array.length, 0);
  }

  @Test
  public void testAdd() {
    Integer[] ints = new Integer[5];

    ints[0] = 0;
    ints[1] = 1;
    ints[2] = 2;
    ints[3] = 3;
    ints[4] = 4;

    DynamicArray<Integer> array = new DynamicArray<Integer>(ints, Integer.MAX_VALUE);
    array.add(1, 13);
    assertArrayEquals(array.toArray(), new Integer[] {0, 13, 1, 2, 3, 4});
    array.add(0, 7);
    assertArrayEquals(array.toArray(), new Integer[] {7, 0, 13, 1, 2, 3, 4});
    array.add(3, 7);
    assertArrayEquals(array.toArray(), new Integer[] {7, 0, 13, 7, 1, 2, 3, 4});
    array.add(4, 120);
    assertArrayEquals(array.toArray(), new Integer[] {7, 0, 13, 7, 120, 1, 2, 3, 4});
  }

  @Test
  public void testTrimToSize() {
    buildArray();
    final int capacityBefore = array.capacity();
    array.trimToSize();
    final int capacityAfter = array.capacity();
    assertTrue(capacityBefore > capacityAfter);
    assertEquals(capacityAfter, array.length);
  }

  @Test
  public void testIsEmpty() {
    DynamicArray<Long> longs = new DynamicArray<Long>();
    assertTrue(longs.isEmpty());
  }

  @Test
  public void testSize() {
    buildArray();
    assertEquals(array.length, length);
  }

  @Test
  public void testSet() {
    buildArray();
    final int index = (int) (Math.random() * (length - 1));
    final Integer item = (int) (Math.random() * 100);
    array.set(index, item);
    assertEquals(array.get(index), item);
  }

  @Test
  public void testToArray() {
    Integer[] array = new Integer[] {0, 1, 2, 3, 4};
    this.array = new DynamicArray<Integer>(array, 1200);
    assertArrayEquals(this.array.toArray(), array);
  }

  @Test
  public void testIterator() {
    buildArray();
    Integer j = 0;
    for (Integer i : array) {
      assertEquals(i, j++);
    }
  }

  @Test
  public void testIteratorIntInt() {
    buildArray();
    final int from = (int) (Math.random() * (length - 1));
    final int to = (int) (Math.random() * (length - from) + from);
    Iterator<Integer> it = array.iterator(from, to);
    Integer j = from;
    while (it.hasNext()) {
      assertEquals(it.next(), j++);
    }
  }

  @Test
  public void testAddAll() {
    buildArray();
    ArrayList<Integer> toBeAdded = new ArrayList<Integer>();
    Integer last = array.get(array.length - 1);

    toBeAdded.add(++last);
    toBeAdded.add(++last);
    toBeAdded.add(++last);
    toBeAdded.add(++last);
    toBeAdded.add(++last);

    array.addAll(toBeAdded);

    assertEquals(array.get(length + 4), last--);
    assertEquals(array.get(length + 3), last--);
    assertEquals(array.get(length + 2), last--);
    assertEquals(array.get(length + 1), last--);
    assertEquals(array.get(length), last--);
  }

  @Test
  public void testContains() {
    buildArray();
    Integer last = array.get(array.length - 1);

    array.add(++last);
    array.add(++last);
    array.add(++last);
    array.add(++last);
    array.add(++last);

    assertTrue(array.contains(last--));
    assertTrue(array.contains(last--));
    assertTrue(array.contains(last--));
    assertTrue(array.contains(last--));
    assertTrue(array.contains(last--));
  }

  @Test
  public void testContainsAll() {
    buildArray();

    ArrayList<Integer> l = new ArrayList<Integer>();
    Integer last = array.get(array.length - 1);

    l.add(++last);
    l.add(++last);
    l.add(++last);
    l.add(++last);
    l.add(++last);

    array.addAll(l);

    assertTrue(array.containsAll(l));
  }

  @Test
  public void testRemoveIndex() {
    Integer[] a = new Integer[] {2, 4, 3, 1, 5, 6, 10};
    DynamicArray<Integer> array = new DynamicArray<Integer>(a);
    array.remove(0);
    assertArrayEquals(array.toArray(), new Integer[] {4, 3, 1, 5, 6, 10});
    array.remove(3);
    assertArrayEquals(array.toArray(), new Integer[] {4, 3, 1, 6, 10});
    array.remove(1);
    assertArrayEquals(array.toArray(), new Integer[] {4, 1, 6, 10});
    array.remove(3);
    assertArrayEquals(array.toArray(), new Integer[] {4, 1, 6});
    assertEquals(array.length, 3);
  }

  @Test
  public void testRemoveObject() {
    buildArray();
    try {
      array.remove(new Object());
    } catch (UnsupportedOperationException e) {
      assertTrue(true);
    }
    assertFalse(false);
  }

  @Test
  public void testRemoveAll() {
    buildArray();

    ArrayList<Integer> l = new ArrayList<Integer>();
    Integer last = array.get(array.length - 1);

    l.add(++last);
    l.add(++last);
    l.add(++last);
    l.add(++last);
    l.add(++last);

    try {
      array.removeAll(l);
    } catch (UnsupportedOperationException e) {
      assertTrue(true);
    }
    assertFalse(false);
  }

  @Test
  public void testRetainAll() {
    buildArray();

    ArrayList<Integer> l = new ArrayList<Integer>();
    Integer last = array.get(array.length - 1);

    l.add(++last);
    l.add(++last);
    l.add(++last);
    l.add(++last);
    l.add(++last);

    try {
      array.retainAll(l);
    } catch (UnsupportedOperationException e) {
      assertTrue(true);
    }
    assertFalse(false);
  }

  @Test
  public void testToArrayTArray() {
    buildArray();
    try {
      array.toArray(new Integer[100]);
    } catch (UnsupportedOperationException e) {
      assertTrue(true);
    }
    assertFalse(false);
  }
}
