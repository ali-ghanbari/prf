package edu.utdallas.prf.commons.asm;

/*
 * #%L
 * objsim
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

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.DRETURN;
import static org.objectweb.asm.Opcodes.FRETURN;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.LRETURN;
import static org.objectweb.asm.Opcodes.RETURN;

/**
 * A minimal method visitor for adding after advices in the form of finally blocks.
 * In case of constructor methods that call their overloaded constructors or super
 * constructors, the method behaves appropriately.
 *
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public abstract class FinallyBlockAdviceAdapter extends MethodVisitor {
    private final Label startFinally;

    private int invokeSpecialSkips;

    protected FinallyBlockAdviceAdapter(final int api, final MethodVisitor methodVisitor) {
        this(api, methodVisitor, 0);
    }

    protected FinallyBlockAdviceAdapter(final int api,
                                        final MethodVisitor methodVisitor,
                                        final int invokeSpecialSkips) {
        super(api, methodVisitor);
        this.startFinally = new Label();
        this.invokeSpecialSkips = invokeSpecialSkips;
    }

    @Override
    public void visitCode() {
        super.visitCode();
        if (this.invokeSpecialSkips == 0) {
            insertPrelude();
            this.invokeSpecialSkips = -1;
        }
    }

    private void insertPrelude() {
        onMethodEnter();
        super.visitLabel(this.startFinally);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        if (opcode == INVOKESPECIAL) {
            if (this.invokeSpecialSkips > 0) {
                this.invokeSpecialSkips--;
            }
            if (this.invokeSpecialSkips == 0) {
                insertPrelude();
                this.invokeSpecialSkips = -1;
            }
        }
    }

    protected abstract void onMethodEnter();

    protected abstract void onMethodExit(boolean normalExit);

    private boolean isReturnInst(int opcode) {
        switch (opcode) {
            case RETURN:
            case IRETURN:
            case ARETURN:
            case LRETURN:
            case FRETURN:
            case DRETURN:
                return true;
        }
        return false;
    }

    @Override
    public void visitInsn(int opcode) {
        if (isReturnInst(opcode)) {
            onMethodExit(true);
        }
        super.visitInsn(opcode);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        final Label endFinally = new Label();
        super.visitTryCatchBlock(this.startFinally, endFinally, endFinally, null);
        super.visitLabel(endFinally);
        onMethodExit(false);
        super.visitInsn(ATHROW);
        super.visitMaxs(maxStack, maxLocals);
    }
}