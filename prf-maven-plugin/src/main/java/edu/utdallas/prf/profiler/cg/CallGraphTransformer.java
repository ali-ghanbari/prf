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

import edu.utdallas.prf.commons.asm.FinallyBlockAdviceAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
class CallGraphTransformer extends FinallyBlockAdviceAdapter {
    private static final Type CALL_GRAPH_RECORDER = Type.getType(CallGraphRecorder.class);

    private final int methodIndex;

    public CallGraphTransformer(final MethodVisitor methodVisitor,
                                final int access,
                                final String name,
                                final String descriptor,
                                final int methodIndex) {
        super(ASM7, methodVisitor, access, name, descriptor);
        this.methodIndex = methodIndex;
    }

    @Override
    protected void insertPrelude() {
        push(this.methodIndex);
        invokeStatic(CALL_GRAPH_RECORDER, Method.getMethod("void enterMethod(int)"));
    }

    @Override
    protected void insertSequel(boolean normalExit) {
        invokeStatic(CALL_GRAPH_RECORDER, Method.getMethod("void leaveMethod()"));
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        CallGraphRecorder.allocateList(this.methodIndex);
    }
}
