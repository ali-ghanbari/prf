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

import edu.utdallas.prf.ClassLevelCoverage;
import edu.utdallas.prf.CoverageInfo;
import edu.utdallas.prf.FaultLocalizationInfo;
import edu.utdallas.prf.ClassLevelFL;
import edu.utdallas.prf.LineLevelCoverage;
import edu.utdallas.prf.LineLevelFL;
import edu.utdallas.prf.MethodLevelCoverage;
import edu.utdallas.prf.MethodLevelFL;
import edu.utdallas.prf.ProgramElement;
import edu.utdallas.prf.commons.process.ControlId;
import org.pitest.functional.SideEffect1;
import org.pitest.util.CommunicationThread;
import org.pitest.util.ReceiveStrategy;
import org.pitest.util.SafeDataInputStream;
import org.pitest.util.SafeDataOutputStream;

import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
class ProfilerCommunicationThread extends CommunicationThread {
    private final DataReceiver receiver;

    public ProfilerCommunicationThread(final ServerSocket socket,
                                       final ProfilerArguments arguments) {
        this(socket, new DataSender(arguments), new DataReceiver());
    }

    public ProfilerCommunicationThread(final ServerSocket socket,
                                       final DataSender sender,
                                       final DataReceiver receiver) {
        super(socket,sender, receiver);
        this.receiver = receiver;
    }

    public  Map<String, Long> getTestsTiming() {
        return this.receiver.testsTiming;
    }

    public String[] getFailingTestNames() {
        return this.receiver.failingTestNames;
    }

    public FaultLocalizationInfo<? extends ProgramElement> getFLInfo() {
        return this.receiver.flInfo;
    }

    public CoverageInfo<? extends ProgramElement> getCoverageInfo() {
        return this.receiver.coverageInfo;
    }

    private static class DataSender implements SideEffect1<SafeDataOutputStream> {
        final ProfilerArguments arguments;

        public DataSender(final ProfilerArguments arguments) {
            this.arguments = arguments;
        }

        @Override
        public void apply(final SafeDataOutputStream dos) {
            dos.write(this.arguments);
        }
    }

    private static class DataReceiver implements ReceiveStrategy {
        final Map<String, Long> testsTiming;

        String[] failingTestNames;

        FaultLocalizationInfo<? extends ProgramElement> flInfo;

        CoverageInfo<? extends ProgramElement> coverageInfo;

        public DataReceiver() {
            this.testsTiming = new HashMap<>();
        }

        @Override
        public void apply(final byte control, final SafeDataInputStream dis) {
            switch (control) {
                case ControlId.REPORT_TEST_TIME:
                    final String testName = dis.readString();
                    final Long timeElapsed = dis.readLong();
                    this.testsTiming.put(testName, timeElapsed);
                    break;
                case ControlId.REPORT_FAILING_TEST_NAMES:
                    this.failingTestNames = dis.read(String[].class);
                    break;
                case ControlId.REPORT_CLASS_LEVEL_FL:
                    this.flInfo = dis.read(ClassLevelFL.class);
                    break;
                case ControlId.REPORT_METHOD_LEVEL_FL:
                    this.flInfo = dis.read(MethodLevelFL.class);
                    break;
                case ControlId.REPORT_LINE_LEVEL_FL:
                    this.flInfo = dis.read(LineLevelFL.class);
                    break;
                case ControlId.REPORT_CLASS_LEVEL_COV:
                    this.coverageInfo = dis.read(ClassLevelCoverage.class);
                    break;
                case ControlId.REPORT_METHOD_LEVEL_COV:
                    this.coverageInfo = dis.read(MethodLevelCoverage.class);
                    break;
                case ControlId.REPORT_LINE_LEVEL_COV:
                    this.coverageInfo = dis.read(LineLevelCoverage.class);
            }
        }
    }
}