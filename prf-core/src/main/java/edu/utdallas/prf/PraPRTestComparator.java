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

import edu.utdallas.prf.commons.misc.MemberNameUtils;
import org.apache.commons.lang3.Validate;
import org.pitest.testapi.TestUnit;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class PraPRTestComparator implements Comparator<TestUnit>, Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<String, Long> testsTiming;

    private final Set<String> failingTests;

    public PraPRTestComparator(final Map<String, Long> testsTiming,
                               final String[] failingTests) {
        Validate.isInstanceOf(Serializable.class, testsTiming);
        this.testsTiming = testsTiming;
        this.failingTests = new HashSet<>();
        Collections.addAll(this.failingTests, failingTests);
    }

    public PraPRTestComparator(final Map<String, Long> testsTiming,
                               final Collection<String> failingTests) {
        Validate.isInstanceOf(Serializable.class, testsTiming);
        this.testsTiming = testsTiming;
        this.failingTests = new HashSet<>(failingTests);
    }

    public Map<String, Long> getTestsTiming() {
        return this.testsTiming;
    }

    public Set<String> getFailingTests() {
        return this.failingTests;
    }

    @Override
    public int compare(TestUnit t1, TestUnit t2) {
        String n1 = t1.getDescription().getName();
        n1 = MemberNameUtils.sanitizeExtendedTestName(n1);
        String n2 = t2.getDescription().getName();
        n2 = MemberNameUtils.sanitizeExtendedTestName(n2);
        final boolean f1 = this.failingTests.contains(n1);
        final boolean f2 = this.failingTests.contains(n2);
        if (f1 ^ f2) {
            return f1 ? -1 : 1;
        }
        Long time1 = this.testsTiming.get(n1);
        if (time1 == null) {
            System.out.printf("WARNING: Missing time measurement for '%s'. Long.MAX_VALUE is being used.%n", n1);
            time1 = Long.MAX_VALUE;
        }
        Long time2 = this.testsTiming.get(n2);
        if (time2 == null) {
            System.out.printf("WARNING: Missing time measurement for '%s'. Long.MAX_VALUE is being used.%n", n2);
            time2 = Long.MAX_VALUE;
        }
        return Long.compare(time1, time2);
    }
}