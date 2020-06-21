package edu.utdallas.prf.profiler;

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

import edu.utdallas.prf.CoverageInfo;
import edu.utdallas.prf.FaultLocalizationInfo;
import edu.utdallas.prf.ProgramElement;
import edu.utdallas.prf.commons.junit.runner.JUnitRunner;
import edu.utdallas.prf.commons.misc.MemberNameUtils;
import edu.utdallas.prf.profiler.fl.CoverageRecorder;
import edu.utdallas.prf.profiler.fl.FLOptions;
import edu.utdallas.prf.profiler.fl.CovRecTransformer;
import edu.utdallas.prf.profiler.fl.ClassLevelCovRecTransformer;
import edu.utdallas.prf.profiler.fl.LineLevelCovRecTransformer;
import edu.utdallas.prf.profiler.fl.MethodLevelCovRecTransformer;
import org.pitest.boot.HotSwapAgent;
import org.pitest.process.ProcessArgs;
import org.pitest.testapi.Description;
import org.pitest.testapi.ResultCollector;
import org.pitest.testapi.TestUnit;
import org.pitest.util.ExitCode;
import org.pitest.util.SafeDataInputStream;

import java.net.Socket;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class Profiler {
    public static void main(String[] args) throws Exception {
        System.out.println("Profiler is HERE!");
        final int port = Integer.parseInt(args[0]);
        try (Socket socket = new Socket("localhost", port)) {
            final SafeDataInputStream dis = new SafeDataInputStream(socket.getInputStream());

            final ProfilerArguments arguments = dis.read(ProfilerArguments.class);
            final ProfilerOptions options = arguments.options;

            final FLOptions flOptions = options.flOptions;
            CoverageRecorder.setFLOptions(flOptions);
            CovRecTransformer transformer = installFLTransformer(flOptions, arguments.whiteListPrefix);
            if (transformer == null && options.collectCoverage) {
                CoverageRecorder.setFLOptions(FLOptions.LINE_LEVEL);
                transformer = new LineLevelCovRecTransformer(arguments.whiteListPrefix);
                HotSwapAgent.addTransformer(transformer);
            }

            final ProfilerReporter reporter = new ProfilerReporter(socket.getOutputStream());

            final JUnitRunner runner = new JUnitRunner(arguments.testClassNames, false);
            runner.setTestUnits(decorateTestUnits(runner.getTestUnits(), reporter));
            runner.run();

            reporter.reportFailingTestNames(runner.getFailingTestNames());
            if (transformer != null) {
                CoverageRecorder.reportFLInfo(reporter, transformer.getDomain(), runner.getFailingTestNames(), options.flStrategy);
                if (options.collectCoverage) {
                    CoverageRecorder.reportCoverageInfo(reporter, transformer.getDomain());
                }
            }

            System.out.println("Profiler is DONE!");
            reporter.done(ExitCode.OK);
        }
    }

    private static CovRecTransformer installFLTransformer(final FLOptions flOptions,
                                                          final String whiteListPrefix) {
        CovRecTransformer transformer = null;
        switch (flOptions) {
            case CLASS_LEVEL:
                System.out.println("INFO: Class level fault localization activated");
                transformer = new ClassLevelCovRecTransformer(whiteListPrefix);
                break;
            case METHOD_LEVEL:
                System.out.println("INFO: Method level fault localization activated");
                transformer = new MethodLevelCovRecTransformer(whiteListPrefix);
                break;
            case LINE_LEVEL:
                System.out.println("INFO: Line level fault localization activated");
                transformer = new LineLevelCovRecTransformer(whiteListPrefix);
        }
        if (transformer != null) {
            HotSwapAgent.addTransformer(transformer);
        }
        return transformer;
    }

    private static List<TestUnit> decorateTestUnits(final List<TestUnit> testUnits,
                                                    final ProfilerReporter reporter) {
        final List<TestUnit> decoratedTests = new LinkedList<>();
        for (final TestUnit testUnit : testUnits) {
            decoratedTests.add(new TestUnit() {
                @Override
                public void execute(ResultCollector rc) {
                    String testName = testUnit.getDescription().getName();
                    testName = MemberNameUtils.sanitizeExtendedTestName(testName);
                    CoverageRecorder.setCurrentTest(testName);
                    final long start = System.currentTimeMillis();
                    testUnit.execute(rc);
                    final long elapsed = System.currentTimeMillis() - start;
                    reporter.reportTestTime(testName, elapsed);

                }

                @Override
                public Description getDescription() {
                    return testUnit.getDescription();
                }
            });
        }
        return decoratedTests;
    }

    public static ProfilerResults runProfiler(final ProcessArgs defaultProcessArgs,
                                              final String whiteListPrefix,
                                              final Collection<String> testClassNames,
                                              final ProfilerOptions options) {
        final ProfilerArguments arguments = new ProfilerArguments(whiteListPrefix, testClassNames, options);
        final ProfilerProcess process = new ProfilerProcess(defaultProcessArgs, arguments);
        try {
            process.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        process.waitToDie();
        return new ProfilerResults() {
            @Override
            public Map<String, Long> getTestsTiming() {
                return process.getTestsTiming();
            }

            @Override
            public String[] getFailingTestNames() {
                return process.getFailingTestNames();
            }

            @Override
            public FaultLocalizationInfo<? extends ProgramElement> getFLInfo() {
                return process.getFLInfo();
            }

            @Override
            public CoverageInfo<? extends ProgramElement> getCoverageInfo() {
                return process.getCoverageInfo();
            }
        };
    }
}