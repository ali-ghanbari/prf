package edu.utdallas.prf;

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

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Objects;

/**
 * A class bundling all the information about a given patch, e.g., compiled class file,
 * patched class name, source file, etc.
 *
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class PatchLocation implements Serializable {
    private static final long serialVersionUID = 1L;

    private final File patchBaseDirectory;

    private final File classFile;

    private final String className;

    private final String methodFullName;

    private final int lineNumber;

    private final double suspVal;

    public PatchLocation(final File classFile,
                         final String className,
                         final String methodFullName,
                         final int lineNumber,
                         final double suspVal) {
        this.patchBaseDirectory = null;
        this.classFile = classFile;
        this.className = className;
        this.methodFullName = methodFullName;
        this.lineNumber = lineNumber;
        this.suspVal = suspVal;
    }

    public PatchLocation(final File classFile,
                         final String methodFullName,
                         final int lineNumber,
                         final double suspVal) {
        this.patchBaseDirectory = null;
        this.classFile = classFile;
        this.className = retrieveClassName();
        this.methodFullName = methodFullName;
        this.lineNumber = lineNumber;
        this.suspVal = suspVal;
    }

    public PatchLocation(final File patchBaseDirectory,
                         final File classFile,
                         final String methodFullName,
                         final int lineNumber,
                         final double suspVal) {
        this.patchBaseDirectory = patchBaseDirectory;
        this.classFile = classFile;
        this.className = retrieveClassName();
        this.methodFullName = methodFullName;
        this.lineNumber = lineNumber;
        this.suspVal = suspVal;
    }

    public File getSourceFile() {
        final String sourceName = retrieveSourceName();
        if (sourceName == null) {
            return null;
        }
        return new File(this.patchBaseDirectory, sourceName);
    }

    public File getClassFile() {
        return this.classFile;
    }

    public double getSuspVal() {
        return this.suspVal;
    }

    public String getClassName() {
        return this.className;
    }

    public String getMethodFullName() {
        return this.methodFullName;
    }

    public int getLineNumber() {
        return this.lineNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PatchLocation)) {
            return false;
        }
        PatchLocation that = (PatchLocation) o;
        return this.lineNumber == that.lineNumber &&
                Objects.equals(this.className, that.className) &&
                Objects.equals(this.methodFullName, that.methodFullName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.className, this.methodFullName, this.lineNumber);
    }

    private String retrieveClassName() {
        try (final InputStream fis = new FileInputStream(this.classFile);
             final InputStream bis = new BufferedInputStream(fis)) {
            final InfoExtractor extractor = new InfoExtractor();
            final ClassReader cr = new ClassReader(bis);
            cr.accept(extractor, 0);
            return extractor.className;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private String retrieveSourceName() {
        try (final InputStream fis = new FileInputStream(this.classFile);
             final InputStream bis = new BufferedInputStream(fis)) {
            final InfoExtractor extractor = new InfoExtractor();
            final ClassReader cr = new ClassReader(bis);
            cr.accept(extractor, 0);
            return extractor.sourceName;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    static class InfoExtractor extends ClassVisitor {
        String sourceName;

        String className;

        public InfoExtractor() {
            super(Opcodes.ASM7);
        }

        @Override
        public void visitSource(String source, String debug) {
            this.sourceName = source;
            super.visitSource(source, debug);
        }

        @Override
        public void visit(final int version,
                          final int access,
                          final String name,
                          final String signature,
                          final String superName,
                          final String[] interfaces) {
            this.className = name.replace('/', '.');
            super.visit(version, access, name, signature, superName, interfaces);
        }
    }
}
