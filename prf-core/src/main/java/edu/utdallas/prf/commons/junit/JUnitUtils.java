package edu.utdallas.prf.commons.junit;

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

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.Option;
import org.pitest.junit.DescriptionFilter;
import org.pitest.junit.adapter.AdaptedJUnitTestUnit;
import org.pitest.testapi.TestUnit;
import org.pitest.util.IsolationUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A set of versatile functions for finding JUnit test cases.
 * These utility functions support JUnit 3.XX and 4.YY.
 *
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class JUnitUtils {
    private static final Map<Class<?>, Set<Method>> VISITED;

    static {
        VISITED = new HashMap<>();
    }

    public static List<TestUnit> discoverTestUnits(final Collection<String> classNames) {
        return discoverTestUnits(classNames, IsolationUtils.getContextClassLoader());
    }

    public static List<TestUnit> discoverTestUnits(final Collection<String> classNames,
                                                   final ClassLoader loader) {
        final List<TestUnit> testUnits = new LinkedList<>();
        final Collection<Class<?>> classes = FCollection.map(classNames, classNameToClass(loader));
        // find JUnit 3.XX and 4.YY tests
        testUnits.addAll(findJUnit3XXTestUnits(classes));
        testUnits.addAll(findJUnit4YYTestUnits(classes));

        for (final Map.Entry<Class<?>, Set<Method>> entry : VISITED.entrySet()) {
            entry.getValue().clear();
        }

        VISITED.clear();
        return testUnits;
    }

    private static F<String, Class<?>> classNameToClass(final ClassLoader loader) {
        return new F<String, Class<?>>() {
            @Override
            public Class<?> apply(final String className) {
                try {
                    return loader.loadClass(className);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e.getCause());
                }
            }
        };
    }

    private static boolean shouldAdd(final Class<?> testSuite, final Method testCase) {
        Set<Method> methods = VISITED.get(testSuite);
        if (methods == null) {
            methods = new HashSet<>();
            VISITED.put(testSuite, methods);
        }
        return methods.add(testCase);
    }

    private static Collection<? extends TestUnit> findJUnit3XXTestUnits(Collection<Class<?>> classes) {
        final List<TestUnit> testUnits = new LinkedList<>();
        for (final Class<?> clazz : classes) {
            if (isAbstract(clazz)) {
                continue;
            }
            if (isJUnit3XXTestSuite(clazz)) {
                testUnits.addAll(findJUnit3XXTestUnits(clazz));
            }
        }
        return testUnits;
    }

    private static boolean isJUnit3XXTestSuite(Class<?> clazz) {
        do {
            clazz = clazz.getSuperclass();
            if (clazz == TestCase.class) {
                return true;
            }
        } while (clazz != null);
        return false;
    }

    private static Collection<? extends TestUnit> findJUnit3XXTestUnits(final Class<?> testSuite) {
        final List<TestUnit> testUnits = new LinkedList<>();
        for (final Method method : testSuite.getMethods()) {
            final int mod = method.getModifiers();
            if (Modifier.isAbstract(mod) || Modifier.isNative(mod)) {
                continue;
            }
            if (method.getReturnType() == Void.TYPE) {
                if (method.getName().startsWith("test") && shouldAdd(testSuite, method)) {
                    testUnits.add(createTestUnit(testSuite, method));
                }
            }
        }
        return testUnits;
    }

    private static Collection<? extends TestUnit> findJUnit4YYTestUnits(Collection<Class<?>> classes) {
        final List<TestUnit> testUnits = new LinkedList<>();
        for (final Class<?> clazz : classes) {
            if (isAbstract(clazz)) {
                continue;
            }
            for (final Method method : clazz.getMethods()) {
                final int mod = method.getModifiers();
                if (Modifier.isAbstract(mod) || Modifier.isNative(mod) || !Modifier.isPublic(mod)) {
                    continue;
                }
                final Test annotation = method.getAnnotation(Test.class);
                if (annotation != null && shouldAdd(clazz, method)) {
                    testUnits.add(createTestUnit(clazz, method));
                }
            }
        }
        return testUnits;
    }

    private static TestUnit createTestUnit(final Class<?> testSuite, final Method testMethod) {
        final Description testDescription = Description.createTestDescription(testSuite, testMethod.getName());
        final Filter filter = DescriptionFilter.matchMethodDescription(testDescription);
        return new AdaptedJUnitTestUnit(testSuite, Option.some(filter));
    }

    private static boolean isAbstract(final Class<?> clazz) {
        final int mod = clazz.getModifiers();
        return Modifier.isInterface(mod) || Modifier.isAbstract(mod);
    }
}