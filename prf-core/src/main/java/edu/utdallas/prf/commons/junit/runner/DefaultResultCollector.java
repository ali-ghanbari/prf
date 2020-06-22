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
import org.pitest.testapi.Description;
import org.pitest.testapi.ResultCollector;

import java.util.List;

/**
 * A result collector that prints out the name of the test cases as they are executed.
 * This result collector also records the observed results of test cases (e.g., failed/passed).
 *
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
class DefaultResultCollector implements ResultCollector {
    private final List<String> failingTestNames;

    public DefaultResultCollector(final List<String> failingTestNames) {
        this.failingTestNames = failingTestNames;
    }

    @Override
    public void notifyEnd(Description description, Throwable t) {
        if (t != null) {
            final String failingTestName = MemberNameUtils.sanitizeExtendedTestName(description.getName());
            this.failingTestNames.add(failingTestName);
            System.out.flush();
            System.err.println();
            System.out.println(t.getMessage());
            t.printStackTrace();
            System.err.println();
            System.err.flush();
        }
    }

    @Override
    public void notifyEnd(Description description) {
        // nothing
    }

    @Override
    public void notifyStart(Description description) {
        final String testName = MemberNameUtils.sanitizeExtendedTestName(description.getName());
        System.out.println("RUNNING: " + testName + "... ");
    }

    @Override
    public void notifySkipped(Description description) {
        final String testName = MemberNameUtils.sanitizeExtendedTestName(description.getName());
        System.out.println("SKIPPED: " + testName);
    }

    @Override
    public boolean shouldExit() {
        return false;
    }
}