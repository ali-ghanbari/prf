package edu.utdallas.prf.commons.relational;

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

import edu.utdallas.prf.commons.collections.NonNegativeIntSet;
import edu.utdallas.relational.Domain;
import edu.utdallas.relational.IntPair;
import edu.utdallas.relational.Relation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A BDD-based <code>String</code>-typed binary relation.
 * A default BDD variable order of D0xD1 is adopted is adopted
 *
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class BinaryRelation implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final int UNIT_SIZE = 1024;

    protected final Relation relation;

    protected BinaryRelation(BinaryRelation binaryRel) {
        this.relation = binaryRel.relation;
    }

    protected BinaryRelation(Relation relation) {
        this.relation = relation;
    }

    // creates a fresh instance
    public BinaryRelation(String name, String orderer, StringDomain leftDomain, StringDomain rightDomain) {
        this.relation = createUninitializedRelation(name, orderer, leftDomain, rightDomain);
        this.relation.zero();
    }

    // creates a fresh instance
    public BinaryRelation(String name, StringDomain leftDomain, StringDomain rightDomain) {
        this(name, "x", leftDomain, rightDomain);
    }

    private static Relation createUninitializedRelation(String name,
                                                        String orderer,
                                                        StringDomain leftDomain,
                                                        StringDomain rightDomain) {
        orderer = orderer.trim();
        if (!orderer.matches("[x_]")) {
            throw new IllegalArgumentException();
        }
        final Relation relation = new Relation();
        relation.setName(name);
        relation.setDoms(new Domain[]{leftDomain, rightDomain});
        final String leftDomName = leftDomain.getName() + "0";
        final String rightDomName = rightDomain.getName()
                + (leftDomain.getName().equals(rightDomain.getName()) ? "1" : "0");
        relation.setSign(new String[]{leftDomName, rightDomName}, leftDomName + orderer + rightDomName);
        return relation;
    }

    // creates a binary relation by loading it from disk
    public static BinaryRelation load(String name, StringDomain leftDomain, StringDomain rightDomain) {
        return load(name, "x", leftDomain, rightDomain);
    }

    public static BinaryRelation load(String name, String orderer, StringDomain leftDomain, StringDomain rightDomain) {
        final Relation relation = createUninitializedRelation(name, orderer, leftDomain, rightDomain);
        relation.load(".");
        return new BinaryRelation(relation);
    }

    public static BinaryRelation load(final byte[] data, String name, String orderer, StringDomain leftDomain, StringDomain rightDomain) {
        final Relation relation = createUninitializedRelation(name, orderer, leftDomain, rightDomain);
        try {
            relation.load(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new BinaryRelation(relation);
    }

    public void visit(BinaryRelationVisitor visitor) {
        for (final IntPair pair : this.relation.getAry2IntTuples()) {
            visitor.visit(pair.idx0, pair.idx1);
        }
    }

    public boolean contains(int idx1, int idx2) {
        return this.relation.contains(idx1, idx2);
    }

    public boolean contains(String val1, String val2) {
        return this.relation.contains(val1, val2);
    }

    public void add(int idx1, int idx2) {
        this.relation.add(idx1, idx2);
    }

    public void add(String val1, String val2) {
        this.relation.add(val1, val2);
    }

    public String getName() {
        return this.relation.getName();
    }

    public void save(String dirName) {
        this.relation.save(dirName);
    }

    public int size() {
        return this.relation.size();
    }

    public void close() {this.relation.close();}

    public StringDomain getLeftDomain() {
        return (StringDomain) this.relation.getDoms()[0];
    }

    public StringDomain getRightDomain() {
        return (StringDomain) this.relation.getDoms()[1];
    }

    public byte[] toByteArray() {
        try {
            return relation.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Map<Integer, List<Integer>> decompressList() {
        final Map<Integer, List<Integer>> map = new HashMap<>(UNIT_SIZE);
        for (final IntPair pair : this.relation.getAry2IntTuples()) {
            final int leftIndex = pair.idx0;
            final int rightIndex = pair.idx1;
            List<Integer> list = map.get(leftIndex);
            if (list == null) {
                list = new ArrayList<>(getRightDomain().size());
                map.put(leftIndex, list);
            }
            list.add(rightIndex);
        }
        return map;
    }

    public Map<Integer, NonNegativeIntSet> decompressSet() {
        final Map<Integer, NonNegativeIntSet> map = new HashMap<>(UNIT_SIZE);
        for (final IntPair pair : this.relation.getAry2IntTuples()) {
            final int leftIndex = pair.idx0;
            final int rightIndex = pair.idx1;
            NonNegativeIntSet set = map.get(leftIndex);
            if (set == null) {
                set = new NonNegativeIntSet(getRightDomain().size());
                map.put(leftIndex, set);
            }
            set.add(rightIndex);
        }
        return map;
    }

    public void load(String path) {
        this.load(path);
    }
}