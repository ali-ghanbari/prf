package edu.utdallas.prf.commons.misc;

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

import org.objectweb.asm.Type;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class MemberNameUtils {
    private MemberNameUtils() { }

    public static String sanitizeExtendedTestName(String name) {
        name = name.substring(1 + name.indexOf(' '));
        final int indexOfLP = name.indexOf('(');
        if (indexOfLP >= 0) {
            final String testCaseName = name.substring(0, indexOfLP);
            name = name.substring(1 + indexOfLP, name.length() - 1) + "." + testCaseName;
        }
        return name;
    }

    public static String sanitizeTestName(String name) {
        //SETLab style: test.class.name:test_name
        name = name.replace(':', '.');
        //Defects4J style: test.class.name::test_name
        name = name.replace("src/test", ".");
        final int indexOfLP = name.indexOf('(');
        if (indexOfLP >= 0) {
            name = name.substring(0, indexOfLP);
        }
        return name;
    }

    public static String composeMethodFullName(final String owner, final String name, final String descriptor) {
        return owner.replace('/', '.') +
                '.' +
                name +
                '(' +
                joinTypeClassNames(Type.getArgumentTypes(descriptor), ",") +
                ')';
    }

    /**
     * Joins class names an array of types using a separator.
     * Empty string shall be returned for empty array.
     *
     * @param types Array of types
     * @param separator Separator string
     * @return Resulting string
     */
    public static String joinTypeClassNames(final Type[] types, final String separator) {
        if (types == null || separator == null) {
            throw new IllegalArgumentException("Input array or separator cannot be null");
        }
        StringBuilder pt = new StringBuilder();
        final int iMax = types.length - 1;
        for (int i = 0; iMax >= 0; i++) {
            pt.append(types[i].getClassName());
            if (i == iMax) {
                return pt.toString();
            }
            pt.append(separator);
        }
        return "";
    }
}