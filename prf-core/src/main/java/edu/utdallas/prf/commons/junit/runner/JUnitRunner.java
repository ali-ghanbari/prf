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

import edu.utdallas.prf.PraPRTestComparator;
import edu.utdallas.prf.commons.misc.MemberNameUtils;
import org.pitest.functional.predicate.Predicate;
import org.pitest.testapi.ResultCollector;
import org.pitest.testapi.TestUnit;
import org.pitest.testapi.execute.ExitingResultCollector;
import org.pitest.util.IsolationUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static edu.utdallas.prf.commons.junit.JUnitUtils.discoverTestUnits;

/**
 * A set of utility methods for running JUnit test cases.
 * The methods allows running entire test class or test cases selectively.
 *
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class JUnitRunner {
    private static final ExecutorService EXECUTOR_SERVICE;

    static {
        EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
    }

    private final List<String> failingTestNames;

    private List<TestUnit> testUnits;

    private final ResultCollector resultCollector;

    public JUnitRunner(final Collection<String> classNames,
                       final boolean earlyExit) {
        this.testUnits = discoverTestUnits(classNames);
        this.failingTestNames = new ArrayList<>();
        ResultCollector collector = new DefaultResultCollector(this.failingTestNames);
        if (earlyExit) {
            collector = new ExitingResultCollector(collector);
        }
        this.resultCollector = collector;
    }

    public JUnitRunner(final Collection<String> classNames,
                       final PraPRTestComparator comparator,
                       final boolean earlyExit) {
        this(IsolationUtils.getContextClassLoader(), classNames, comparator, earlyExit);
    }

    public JUnitRunner(final ClassLoader loader,
                       final Collection<String> classNames,
                       final PraPRTestComparator comparator,
                       final boolean earlyExit) {
        this.testUnits = discoverTestUnits(classNames, loader);
        Collections.sort(this.testUnits, comparator);
        this.failingTestNames = new ArrayList<>();
        ResultCollector collector = new DefaultResultCollector(this.failingTestNames);
        if (earlyExit) {
            collector = new ExitingResultCollector(collector);
        }
        this.resultCollector = collector;
    }

    public List<String> getFailingTestNames() {
        return this.failingTestNames;
    }

    public List<TestUnit> getTestUnits() {
        return this.testUnits;
    }

    public void setTestUnits(final List<TestUnit> testUnits) {
        this.testUnits = testUnits;
    }

    public TestExecutionStatus run() {
        return run(TestUnitFilter.all());
    }

    public TestExecutionStatus run(final Map<String, Long> testsTiming,
                                   final long timeoutConstant,
                                   final double timeoutPercent) {
        return run(IsolationUtils.getContextClassLoader(),
                TestUnitFilter.all(),
                testsTiming,
                timeoutConstant,
                timeoutPercent);
    }

    public TestExecutionStatus run(final Predicate<TestUnit> shouldRun) {
        for (final TestUnit testUnit : this.testUnits) {
            if (!shouldRun.apply(testUnit)) {
                continue;
            }
            testUnit.execute(this.resultCollector);
            if (this.resultCollector.shouldExit()) {
                System.out.println("WARNING: Running test cases is terminated.");
                return TestExecutionStatus.EARLY_EXIT;
            }
        }
        return TestExecutionStatus.OK;
    }

    public TestExecutionStatus run(final ClassLoader loader,
                                   final Predicate<TestUnit> shouldRun,
                                   final Map<String, Long> testsTiming,
                                   final long timeoutConstant,
                                   final double timeoutPercent) {
        for (final TestUnit testUnit : this.testUnits) {
            if (!shouldRun.apply(testUnit)) {
                continue;
            }
            final Runnable task = new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.currentThread().setContextClassLoader(loader);
                        testUnit.execute(JUnitRunner.this.resultCollector);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            };
            try {
                String testName = testUnit.getDescription().getName();
                testName = MemberNameUtils.sanitizeExtendedTestName(testName);
                final double explodedTestTime = testsTiming.get(testName).doubleValue() * (1.D + timeoutPercent);
                final long timeoutThreshold = timeoutConstant + (long) explodedTestTime;
                Thread.currentThread().setContextClassLoader(loader);
                EXECUTOR_SERVICE.submit(task).get(timeoutThreshold, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                System.out.println("WARNING: Running test cases is terminated due to TIME_OUT.");
                return TestExecutionStatus.TIMED_OUT;
            } catch (Exception e) {
                System.out.println("WARNING: Running test cases is terminated.");
                return TestExecutionStatus.EARLY_EXIT;
            }
            if (this.resultCollector.shouldExit()) {
                System.out.println("WARNING: Running test cases is terminated.");
                return TestExecutionStatus.EARLY_EXIT;
            }
        }
        return TestExecutionStatus.OK;
    }
}