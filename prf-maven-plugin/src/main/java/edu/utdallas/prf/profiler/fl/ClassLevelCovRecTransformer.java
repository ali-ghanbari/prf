package edu.utdallas.prf.profiler.fl;

/*
 * #%L
 * prf-plugin
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

import edu.utdallas.prf.commons.relational.StringDomain;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.Method;
import org.pitest.bytecode.FrameOptions;
import org.pitest.functional.predicate.Predicate;

import java.lang.reflect.Modifier;

import static org.objectweb.asm.Opcodes.ASM7;

/**
 *
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class ClassLevelCovRecTransformer extends CovRecTransformer {
    private static final Type COVERAGE_RECORDER = Type.getType(CoverageRecorder.class);

    private final StringDomain classesDom;

    public ClassLevelCovRecTransformer(final Predicate<String> appClassFilter) {
        super(appClassFilter);
        this.classesDom = new StringDomain("C");
    }

    @Override
    protected byte[] transform(String className, byte[] classBytes) {
        final ClassReader reader = new ClassReader(classBytes);
        final ClassWriter writer = new ClassWriter(FrameOptions.pickFlags(classBytes));
        final ClassVisitor visitor = new CLCovRecClassVisitor(writer);
        reader.accept(visitor, ClassReader.EXPAND_FRAMES);
        return writer.toByteArray();
    }

    @Override
    public StringDomain getDomain() {
        return this.classesDom;
    }

    class CLCovRecClassVisitor extends ClassVisitor {
        private int classIndex;

        private boolean isInterface;

        public CLCovRecClassVisitor(final ClassVisitor classVisitor) {
            super(ASM7, classVisitor);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            this.classIndex = classesDom.getOrAdd(name.replace('/', '.'));
            this.isInterface = Modifier.isInterface(access);
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            final MethodVisitor defaultMethodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);
            if (this.isInterface || Modifier.isAbstract(access) || Modifier.isNative(access) || "<clinit>".equals(name)) {
                return defaultMethodVisitor;
            }
            return new CLCovRecMethodVisitor(defaultMethodVisitor, access, name, descriptor);
        }

        class CLCovRecMethodVisitor extends AdviceAdapter {
            public CLCovRecMethodVisitor(final MethodVisitor mv,
                                         final int access,
                                         final String name,
                                         final String desc) {
                super(ASM7, mv, access, name, desc);
            }

            @Override
            protected void onMethodEnter() {
                push(classIndex);
                invokeStatic(COVERAGE_RECORDER, Method.getMethod("void markClass(int)"));
            }
        }
    }
}
