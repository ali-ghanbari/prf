/*
 * Credit: copy and paste from PITest source code. Could not reuse from the
 * library because org.objectweb.asm.* was shaded in the deployed JAR.
 */
package edu.utdallas.prf.commons.asm;

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

import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.functional.Option;
import org.pitest.util.PitError;

/**
 * A ClassWriter that computes the common super class of two classes without
 * actually loading them with a ClassLoader.
 *
 * @author Eric Bruneton
 *
 *         Modified to match behaviour of default ClassWriter and cache already
 *         calculated values
 */
public class ComputeClassWriter extends ClassWriter {
    private final ClassByteArraySource bytes;

    private final Map<String, String>  cache;

    public ComputeClassWriter(final ClassByteArraySource bytes,
                              final Map<String, String> cache,
                              final int flags) {
        super(flags);
        this.bytes = bytes;
        this.cache = cache;
    }

    @Override
    protected String getCommonSuperClass(final String type1, final String type2) {
        final String key = type1 + "!_!" + type2;
        final String previous = this.cache.get(key);
        if (previous != null) {
            return previous;
        }

        final ClassReader info1 = typeInfo(type1);
        final ClassReader info2 = typeInfo(type2);

        final String result = getCommonSuperClass(type1, info1, type2, info2);
        this.cache.put(key, result);
        return result;

    }

    private String getCommonSuperClass(final String type1,
                                       final ClassReader info1,
                                       final String type2,
                                       final ClassReader info2) {
        if (isInterface(info1)) {
            if (typeImplements(type2, info2, type1)) {
                return type1;
            } else {
                if (isInterface(info2)) {
                    if (typeImplements(type1, info1, type2)) {
                        return type2;
                    } else {
                        return "java/lang/Object";
                    }
                }
            }
        }

        final StringBuilder b1 = typeAncestors(type1, info1);
        final StringBuilder b2 = typeAncestors(type2, info2);
        String result = "java/lang/Object";
        int end1 = b1.length();
        int end2 = b2.length();
        while (true) {
            final int start1 = b1.lastIndexOf(";", end1 - 1);
            final int start2 = b2.lastIndexOf(";", end2 - 1);
            if ((start1 != -1) && (start2 != -1)
                    && ((end1 - start1) == (end2 - start2))) {
                final String p1 = b1.substring(start1 + 1, end1);
                final String p2 = b2.substring(start2 + 1, end2);
                if (p1.equals(p2)) {
                    result = p1;
                    end1 = start1;
                    end2 = start2;
                } else {
                    return result;
                }
            } else {
                return result;
            }
        }

    }

    private static boolean isInterface(final ClassReader info1) {
        return (info1.getAccess() & Opcodes.ACC_INTERFACE) != 0;
    }

    /**
     * Returns the internal names of the ancestor classes of the given type.
     *
     * @param type
     *          the internal name of a class or interface.
     * @param info
     *          the ClassReader corresponding to 'type'.
     * @return a StringBuilder containing the ancestor classes of 'type',
     *         separated by ';'. The returned string has the following format:
     *         ";type1;type2 ... ;typeN", where type1 is 'type', and typeN is a
     *         direct subclass of Object. If 'type' is Object, the returned string
     *         is empty.
     */
    private StringBuilder typeAncestors(String type, ClassReader info) {
        final StringBuilder b = new StringBuilder();
        while (!"java/lang/Object".equals(type)) {
            b.append(';').append(type);
            type = info.getSuperName();
            info = typeInfo(type);
        }
        return b;
    }

    /**
     * Returns true if the given type implements the given interface.
     *
     * @param type
     *          the internal name of a class or interface.
     * @param info
     *          the ClassReader corresponding to 'type'.
     * @param itf
     *          the internal name of a interface.
     * @return true if 'type' implements directly or indirectly 'itf'
     */
    private boolean typeImplements(String type, ClassReader info, final String itf) {
        final String cleanItf = itf.replace(".", "/");
        while (!"java/lang/Object".equals(type)) {
            final String[] itfs = info.getInterfaces();
            for (final String itf2 : itfs) {
                if (itf2.equals(cleanItf)) {
                    return true;
                }
            }
            for (final String itf2 : itfs) {
                if (typeImplements(itf2, typeInfo(itf2), cleanItf)) {
                    return true;
                }
            }
            type = info.getSuperName();
            info = typeInfo(type);
        }
        return false;
    }

    /**
     * Returns a ClassReader corresponding to the given class or interface.
     *
     * @param type
     *          the internal name of a class or interface.
     * @return the ClassReader corresponding to 'type'.
     */
    private ClassReader typeInfo(final String type) {
        final Option<byte[]> maybeBytes = this.bytes.getBytes(type);
        if (maybeBytes.hasNone()) {
            throw new PitError("Could not find class defintiion for " + type);
        }
        return new ClassReader(maybeBytes.value());
    }
}