package edu.utdallas.prf.profiler.cg;

/*
 * #%L
 * prf-maven-plugin
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

import edu.utdallas.prf.commons.collections.IntStack;
import edu.utdallas.prf.commons.collections.NonNegativeIntSet;
import edu.utdallas.prf.commons.relational.MayCallRel;
import edu.utdallas.prf.commons.relational.MethodsDom;

import java.util.Iterator;

import static edu.utdallas.memo.constants.ConstParams.UNIT_SIZE;

/**
 *
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public final class CallGraphRecorder {
    private static final IntStack CALL_STACK;

    private static long mainThreadId;

    private static final NonNegativeIntSet[][] CALL_GRAPH_EDGES;

    static {
        CALL_STACK = new IntStack();
        CALL_GRAPH_EDGES = new NonNegativeIntSet[UNIT_SIZE][];
        mainThreadId = -1L;
    }

    private CallGraphRecorder() { }

    private static boolean threadSafe() {
        final long currentThreadId = Thread.currentThread().getId();
        if (mainThreadId == -1L) {
            mainThreadId = currentThreadId;
        }
        return mainThreadId == currentThreadId;
    }

    public static void enterMethod(final int callee) {
        if (threadSafe()) {
            final Integer caller = CALL_STACK.peek();
            if (caller != null) {
                NonNegativeIntSet edgeSet = CALL_GRAPH_EDGES[caller / UNIT_SIZE][caller % UNIT_SIZE];
                edgeSet.add(callee);
            }
            CALL_STACK.push(callee);
        }
    }

    public static void leaveMethod() {
        if (threadSafe()) CALL_STACK.pop();
    }

    static void allocateList(final int methodId) {
        final int unitIndex = methodId / UNIT_SIZE;
        NonNegativeIntSet[] unit = CALL_GRAPH_EDGES[unitIndex];
        if (unit == null) {
            unit = new NonNegativeIntSet[UNIT_SIZE];
            CALL_GRAPH_EDGES[unitIndex] = unit;
        }
        unit[methodId % UNIT_SIZE] = new NonNegativeIntSet(UNIT_SIZE * Long.SIZE);
    }

    static MayCallRel getCallGraph(final MethodsDom methodsDom) {
        final MayCallRel mayCallRel = new MayCallRel(methodsDom);
        int callerMethodId = 0;
        for (final NonNegativeIntSet[] unit : CALL_GRAPH_EDGES) {
            if (unit == null) {
                callerMethodId += UNIT_SIZE;
            } else {
                for (final NonNegativeIntSet edgeSet : unit) {
                    if (edgeSet != null) {
                        final Iterator<Integer> it = edgeSet.createIterator();
                        while (it.hasNext()) {
                            final int calleeMethodId = it.next();
                            mayCallRel.add(callerMethodId, calleeMethodId);
                        }
                    }
                    callerMethodId++;
                }
            }
        }
        return mayCallRel;
    }
}