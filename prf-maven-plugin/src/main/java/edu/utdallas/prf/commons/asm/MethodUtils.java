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

import org.apache.commons.lang3.mutable.MutableInt;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.Stack;

import static org.objectweb.asm.Opcodes.ASM7;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.NEW;

/**
 * Utility methods used to calculate useful information about methods of classes in bytecode
 * form.
 *
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public final class MethodUtils {
    private MethodUtils() {

    }

    /**
     * Calculates the number of INVOKESPECIAL instructions that must be skipped before
     * a call to super or this constructor.
     * The method returns -1 in case no such call exists.
     *
     * @param classByteArray Class file byte array
     * @param ctorDescriptor Descriptor of the constructor to be examined.
     * @return Number n where n >= 0 iff n INVOKESPECIAL instructions must be skipped
     *         so as to reach the call to super or this constructor, and n < -1 iff
     *         the method lacks a such call.
     */
    public static int getFirstSpecialInvoke(final byte[] classByteArray,
                                            final String ctorDescriptor) {
        final ClassReader classReader = new ClassReader(classByteArray);
        final AnalyzerClassVisitor classVisitor = new AnalyzerClassVisitor("<init>", ctorDescriptor);
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);
        return classVisitor.skip.intValue();
    }

    private static final class AnalyzerClassVisitor extends ClassVisitor {
        private static final String OBJECT_CLASS;

        static {
            OBJECT_CLASS = Type.getInternalName(Object.class);
        }

        private final String methodName;

        private final String methodDescriptor;

        final MutableInt skip;

        public AnalyzerClassVisitor(final String methodName, final String methodDescriptor) {
            super(ASM7);
            this.methodName = methodName;
            this.methodDescriptor = methodDescriptor;
            this.skip = new MutableInt();
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            this.skip.setValue(OBJECT_CLASS.equals(name) ? -1 : 0);
            super.visit(version, access, name, signature, superName, interfaces);
        }

        private boolean shouldAnalyze(final String name, final String descriptor) {
            return name.equals(this.methodName) && descriptor.equals(this.methodDescriptor);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            final MethodVisitor methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);
            if (this.skip.intValue() >= 0 && shouldAnalyze(name, descriptor)) {
                return new AnalyzerMethodVisitor(methodVisitor, this.skip);
            }
            return methodVisitor;
        }
    }

    private static final class AnalyzerMethodVisitor extends MethodVisitor {
        private final Stack<String> instantiatedTypes;

        private final MutableInt skip;

        private boolean found;

        public AnalyzerMethodVisitor(final MethodVisitor methodVisitor,
                                     final MutableInt skip) {
            super(ASM7, methodVisitor);
            this.instantiatedTypes = new Stack<>();
            this.skip = skip;
            this.found = false;
        }

        @Override
        public void visitTypeInsn(int opcode, String type) {
            if (opcode == NEW && !this.found) {
                this.instantiatedTypes.push(type);
            }
            super.visitTypeInsn(opcode, type);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            if (opcode == INVOKESPECIAL && name.equals("<init>")) {
                if (!(this.found = this.instantiatedTypes.isEmpty())) {
                    final String instantiatedType = this.instantiatedTypes.pop();
                    if (instantiatedType.equals(owner)) {
                        this.skip.increment();
                    } else {
                        throw new IllegalStateException("Malformed class file.");
                    }
                }
            }
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }
    }
}