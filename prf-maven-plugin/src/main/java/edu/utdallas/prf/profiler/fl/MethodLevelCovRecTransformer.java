package edu.utdallas.prf.profiler.fl;

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
import edu.utdallas.prf.commons.asm.MethodBodyUtils;
import edu.utdallas.prf.commons.asm.MethodUtils;
import edu.utdallas.prf.commons.misc.MemberNameUtils;
import edu.utdallas.prf.commons.relational.StringDomain;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.pitest.bytecode.FrameOptions;

import java.lang.reflect.Modifier;

import static org.objectweb.asm.Opcodes.ASM7;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class MethodLevelCovRecTransformer extends CovRecTransformer {
    private static final String COVERAGE_RECORDER = Type.getInternalName(CoverageRecorder.class);

    private byte[] classBytes;

    private final StringDomain methodsDom;

    public MethodLevelCovRecTransformer(String whiteListPrefix) {
        super(whiteListPrefix);
        this.methodsDom = new StringDomain("M");
    }

    @Override
    protected byte[] transform(String className, byte[] classBytes) {
        this.classBytes = classBytes;
        final ClassReader reader = new ClassReader(classBytes);
        final ClassWriter writer = new ClassWriter(FrameOptions.pickFlags(classBytes));
        final ClassVisitor visitor = new MLCovRecClassVisitor(writer);
        reader.accept(visitor, ClassReader.EXPAND_FRAMES);
        return writer.toByteArray();
    }

    @Override
    public StringDomain getDomain() {
        return this.methodsDom;
    }

    class MLCovRecClassVisitor extends ClassVisitor {
        private String className;

        private boolean isInterface;

        public MLCovRecClassVisitor(final ClassVisitor classVisitor) {
            super(ASM7, classVisitor);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            this.className = name;
            this.isInterface = Modifier.isInterface(access);
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            final MethodVisitor defaultMethodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);
            if (this.isInterface || Modifier.isAbstract(access) || Modifier.isNative(access) || "<clinit>".equals(name)) {
                return defaultMethodVisitor;
            }
            final String methodName = MemberNameUtils.composeMethodFullName(this.className, name, descriptor);
            final int methodIndex = MethodLevelCovRecTransformer.this.methodsDom.getOrAdd(methodName);
            int skips = 0;
            if ("<init>".equals(name)) {
                skips = MethodUtils.getFirstSpecialInvoke(MethodLevelCovRecTransformer.this.classBytes, descriptor);
            }
            return new MLFLMethodVisitor(defaultMethodVisitor, skips, methodIndex);
        }

        class MLFLMethodVisitor extends FinallyBlockAdviceAdapter {
            private final int methodsIndex;

            public MLFLMethodVisitor(MethodVisitor methodVisitor,
                                     int invokeSpecialSkips,
                                     int methodIndex) {
                super(ASM7, methodVisitor, invokeSpecialSkips);
                this.methodsIndex = methodIndex;
            }

            @Override
            protected void onMethodEnter() {
                MethodBodyUtils.pushInteger(this.mv, this.methodsIndex);
                super.visitMethodInsn(INVOKESTATIC, COVERAGE_RECORDER, "markMethod", "(I)V", false);
            }

            @Override
            protected void onMethodExit(boolean normalExit) { }

            @Override
            public void visitMaxs(int maxStack, int maxLocals) {
                super.visitMaxs(1 + maxStack, maxLocals);
            }
        }
    }
}
