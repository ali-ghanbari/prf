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

import edu.utdallas.prf.commons.asm.ComputeClassWriter;
import edu.utdallas.prf.commons.asm.FinallyBlockAdviceAdapter;
import edu.utdallas.prf.commons.relational.MethodsDom;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.pitest.bytecode.FrameOptions;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.functional.predicate.Predicate;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;

import static edu.utdallas.prf.commons.misc.MemberNameUtils.composeMethodFullName;
import static org.objectweb.asm.Opcodes.ASM7;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class CallGraphTransformer implements ClassFileTransformer {
    private static final Type CALL_GRAPH_RECORDER = Type.getType(CallGraphRecorder.class);

    private static final Map<String, String> CACHE = new HashMap<>();

    private final ClassByteArraySource byteArraySource;

    private final Predicate<String> appClassFilter;

    private final MethodsDom methodsDom;

    public CallGraphTransformer(final ClassByteArraySource byteArraySource,
                                final Predicate<String> appClassFilter) {
        this.byteArraySource = byteArraySource;
        this.appClassFilter = appClassFilter;
        this.methodsDom = new MethodsDom();
    }

    private boolean isAppClass(final String className) {
        return this.appClassFilter.apply(className.replace('/', '.'));
    }

    @Override
    public byte[] transform(final ClassLoader loader,
                            final String className,
                            final Class<?> classBeingRedefined,
                            final ProtectionDomain protectionDomain,
                            final byte[] classfileBuffer) throws IllegalClassFormatException {
        if (!isAppClass(className)) {
            return null; // no transformation
        }
        final ClassReader reader = new ClassReader(classfileBuffer);
        final ClassWriter writer = new ComputeClassWriter(this.byteArraySource, CACHE, FrameOptions.pickFlags(classfileBuffer));
        final ClassVisitor visitor = new ClassTransformer(writer);
        reader.accept(visitor, ClassReader.EXPAND_FRAMES);
        return writer.toByteArray();
    }

    protected class ClassTransformer extends ClassVisitor {
        private String ownerClassName;

        private boolean isInterface;

        public ClassTransformer(final ClassVisitor classVisitor) {
            super(ASM7, classVisitor);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            this.isInterface = Modifier.isInterface(access);
            this.ownerClassName = name;
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            final MethodVisitor defaultMethodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);
            if (this.isInterface || Modifier.isAbstract(access) || Modifier.isNative(access)) {
                return defaultMethodVisitor;
            }
            final String methodFullName = composeMethodFullName(this.ownerClassName, name, descriptor);
            final int methodIndex = methodsDom.getOrAdd(methodFullName);
            return new MethodTransformer(defaultMethodVisitor, access, name, descriptor, methodIndex);
        }
    }

    protected class MethodTransformer extends FinallyBlockAdviceAdapter {
        private final int methodIndex;

        public MethodTransformer(final MethodVisitor methodVisitor,
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
}
