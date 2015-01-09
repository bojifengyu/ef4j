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

/**
 * The <tt>AbstractAppendOnlyMonotoneLongSequence</tt> class represents defines a sequence of
 * <em>non-decreasing monotone</em> <tt>Long</tt> integers, built in an <em>append-only way</em>.
 * 
 * @author Giulio Ermanno Pibiri
 */
public abstract class AbstractAppendOnlyMonotoneLongSequence extends AbstractMonotoneLongSequence {
  /**
   * Unsupported operation since the sequence is append-only.
   */
  @Override
  public boolean remove(final long integer) {
    throw new UnsupportedOperationException();
  }

  /**
   * Unsupported operation since the sequence is append-only.
   */
  @Override
  public boolean remove(final Object o) {
    throw new UnsupportedOperationException();
  }

  /**
   * Unsupported operation since the sequence is append-only.
   */
  @Override
  public Long remove(final int index) {
    throw new UnsupportedOperationException();
  }

  /**
   * Unsupported operation since the sequence is append-only.
   */
  @Override
  public boolean removeAll(final Collection<?> c) {
    throw new UnsupportedOperationException();
  }
}