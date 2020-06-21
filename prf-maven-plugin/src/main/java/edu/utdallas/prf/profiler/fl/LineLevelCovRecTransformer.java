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

import edu.utdallas.prf.commons.asm.MethodBodyUtils;
import edu.utdallas.prf.commons.relational.StringDomain;
import javassist.Modifier;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.pitest.bytecode.FrameOptions;

import static org.objectweb.asm.Opcodes.ASM7;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.io.File;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class LineLevelCovRecTransformer extends CovRecTransformer {
    private static final String COVERAGE_RECORDER = Type.getInternalName(CoverageRecorder.class);

    private final StringDomain filesDom;

    public LineLevelCovRecTransformer(final String whiteListPrefix) {
        super(whiteListPrefix);
        this.filesDom = new StringDomain("F");
    }

    @Override
    protected byte[] transform(String className, byte[] classBytes) {
        final ClassReader reader = new ClassReader(classBytes);
        final ClassWriter writer = new ClassWriter(FrameOptions.pickFlags(classBytes));
        final ClassVisitor visitor = new LLCovRecClassVisitor(writer);
        reader.accept(visitor, ClassReader.EXPAND_FRAMES);
        return writer.toByteArray();
    }

    @Override
    public StringDomain getDomain() {
        return this.filesDom;
    }

    class LLCovRecClassVisitor extends ClassVisitor {
        private String classInternalName;

        private boolean isInterface;

        private int sourceFileIndex;

        public LLCovRecClassVisitor(final ClassVisitor classVisitor) {
            super(ASM7, classVisitor);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            this.classInternalName = name;
            this.isInterface = Modifier.isInterface(access);
            super.visit(version, access, name, signature, superName, interfaces);
        }

        private String getPackageName() {
            final int li = this.classInternalName.lastIndexOf('/');
            if (li < 0) {
                return null;
            }
            return this.classInternalName.substring(0, li);
        }

        @Override
        public void visitSource(String source, String debug) {
            final String sourceFileName;
            final String packageName = getPackageName();
            if (packageName == null) {
                sourceFileName = source;
            } else {
                sourceFileName = String.format("%s%c%s",
                        StringUtils.join(packageName.split("/"), File.separator),
                        File.separatorChar,
                        source);
            }
            this.sourceFileIndex = LineLevelCovRecTransformer.this.filesDom.getOrAdd(sourceFileName);
            super.visitSource(source, debug);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            final MethodVisitor defaultMethodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);
            if (this.isInterface || java.lang.reflect.Modifier.isAbstract(access) || java.lang.reflect.Modifier.isNative(access) || name.matches("<clinit>|<init>")) {
                return defaultMethodVisitor;
            }
            return new LLCovRecMethodVisitor(defaultMethodVisitor);
        }

        class LLCovRecMethodVisitor extends MethodVisitor {
            private int currentLineNo;

            private int processedLineNo;

            public LLCovRecMethodVisitor(final MethodVisitor methodVisitor) {
                super(ASM7, methodVisitor);
            }

            @Override
            public void visitLabel(Label label) {
                super.visitLabel(label);
                if (this.currentLineNo != this.processedLineNo) {
                    MethodBodyUtils.pushInteger(this.mv, sourceFileIndex);
                    MethodBodyUtils.pushInteger(this.mv, this.currentLineNo);
                    super.visitMethodInsn(INVOKESTATIC, COVERAGE_RECORDER, "markSourceLine", "(II)V", false);
                    this.processedLineNo = this.currentLineNo;
                }
            }

            @Override
            public void visitLineNumber(int line, Label start) {
                this.currentLineNo = line;
                super.visitLineNumber(line, start);
            }

            @Override
            public void visitMaxs(int maxStack, int maxLocals) {
                super.visitMaxs(2 + maxStack, maxLocals);
            }
        }
    }
}
