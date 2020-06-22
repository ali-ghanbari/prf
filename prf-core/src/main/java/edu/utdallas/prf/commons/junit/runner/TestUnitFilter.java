package edu.utdallas.prf.commons.junit.runner;

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
import org.pitest.functional.predicate.Predicate;
import org.pitest.testapi.TestUnit;

import java.io.Serializable;
import java.util.Collection;

/**
 * A set of utility methods that produce common test unit filters.
 *
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public abstract class TestUnitFilter implements Predicate<TestUnit>, Serializable {
    private static final long serialVersionUID = 1L;

    public static TestUnitFilter all() {
        return new TestUnitFilter() {
            @Override
            public Boolean apply(TestUnit testUnit) {
                return Boolean.TRUE;
            }
        };
    }

    public static TestUnitFilter some(final Collection<String> testUnitNames) {
        return new TestUnitFilter() {
            @Override
            public Boolean apply(TestUnit testUnit) {
                final String testName = MemberNameUtils.sanitizeExtendedTestName(testUnit.getDescription().getName());
                return testUnitNames.contains(testName);
            }
        };
    }
}