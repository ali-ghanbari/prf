package edu.utdallas.prf.commons.collections;

/*
 * #%L
 * prf-core
 * %%
 * Copyright (C) 2020 The University of Texas at Dallas
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.carrotsearch.hppc.BitSet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class NonNegativeIntSet extends BitSet implements Serializable {
    private static final long serialVersionUID = 1L;

    private int size;

    public NonNegativeIntSet() {
        this.size = 0;
    }

    public NonNegativeIntSet(final int initialCapacity) {
        super(initialCapacity);
        this.size = 0;
    }

    public NonNegativeIntSet(final int... elements) {
        super(Objects.requireNonNull(elements).length);
        addAll(elements);
    }

    public NonNegativeIntSet(final Collection<Integer> elements) {
        super(Objects.requireNonNull(elements).size());
        addAll(elements);
    }

    public Iterator<Integer> createIterator() {
        return new Iterator<Integer>() {
            private int current = 0;

            private int next = 0;

            @Override
            public boolean hasNext() {
                this.next = NonNegativeIntSet.super.nextSetBit(this.current);
                return this.next >= 0;
            }

            @Override
            public Integer next() {
                this.current = this.next;
                return this.current++;
            }

            @Override
            public void remove() {
                NonNegativeIntSet.super.clear(this.current);
            }
        };
    }

    public boolean contains(final int element) {
        if (element < 0) {
            return false;
        }
        return super.get(element);
    }

    public boolean containsAll(final int... elements) {
        Objects.requireNonNull(elements);
        for (final int element : elements) {
            if (element < 0 || !super.get(element)) {
                return false;
            }
        }
        return true;
    }

    public boolean containsAll(final Collection<Integer> elements) {
        Objects.requireNonNull(elements);
        for (final Integer element : elements) {
            if (element == null || element < 0 || !super.get(element)) {
                return false;
            }
        }
        return true;
    }

    public void add(final int element) {
        if (element < 0) {
            throw new IllegalArgumentException("Non-negative integer argument is expected");
        }
        super.set(element);
        this.size++;
    }

    public void addAll(final int... elements) {
        Objects.requireNonNull(elements);
        for (final int element : elements) {
            if (element < 0) {
                throw new IllegalArgumentException("Elements must be non-negative");
            }
            super.set(element);
            this.size++;
        }
    }

    public void addAll(final Collection<Integer> elements) {
        Objects.requireNonNull(elements);
        for (final Integer element : elements) {
            if (element == null || element < 0) {
                throw new IllegalArgumentException("Elements must be non-null and non-negative");
            }
            super.set(element);
            this.size++;
        }
    }

    public void remove(final int element) {
        if (element >= 0) {
            if (super.get(element)) {
                this.size--;
            }
            super.clear(element);
        }
    }

    public void removeAll(final int... elements) {
        Objects.requireNonNull(elements);
        for (final int element : elements) {
            if (element >= 0) {
                if (super.get(element)) {
                    this.size--;
                }
                super.clear(element);
            }
        }
    }

    public void removeAll(final Collection<Integer> elements) {
        Objects.requireNonNull(elements);
        for (final Integer element : elements) {
            if (element != null && element >= 0) {
                if (super.get(element)) {
                    this.size--;
                }
                super.clear(element);
            }
        }
    }

    @Override
    public long size() {
        return this.size;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(this.bits);
        out.writeObject(this.wlen);
        out.writeObject(this.size);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.bits = (long[]) in.readObject();
        this.wlen = in.readInt();
        this.size = in.readInt();
    }
}
