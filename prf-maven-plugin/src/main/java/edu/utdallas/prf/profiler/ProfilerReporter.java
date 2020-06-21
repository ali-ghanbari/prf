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
import edu.utdallas.prf.ClassLevelFL;
import edu.utdallas.prf.LineLevelCoverage;
import edu.utdallas.prf.LineLevelFL;
import edu.utdallas.prf.MethodLevelCoverage;
import edu.utdallas.prf.MethodLevelFL;
import edu.utdallas.prf.commons.process.AbstractReporter;
import edu.utdallas.prf.commons.process.ControlId;

import java.io.OutputStream;
import java.util.Collection;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class ProfilerReporter extends AbstractReporter {
    public ProfilerReporter(final OutputStream os) {
        super(os);
    }

    public synchronized void reportTestTime(final String testName,
                                            final long timeElapsed) {
        this.dos.writeByte(ControlId.REPORT_TEST_TIME);
        this.dos.writeString(testName);
        this.dos.writeLong(timeElapsed);
        this.dos.flush();
    }

    public synchronized void reportFailingTestNames(final Collection<String> failingTestNames) {
        this.dos.writeByte(ControlId.REPORT_FAILING_TEST_NAMES);
        this.dos.write(failingTestNames.toArray(new String[0]));
    }

    public synchronized void reportClassLevelFL(final ClassLevelFL classLevelFL) {
        this.dos.writeByte(ControlId.REPORT_CLASS_LEVEL_FL);
        this.dos.write(classLevelFL);
    }

    public synchronized void reportMethodLevelFL(final MethodLevelFL methodLevelFL) {
        this.dos.writeByte(ControlId.REPORT_METHOD_LEVEL_FL);
        this.dos.write(methodLevelFL);
    }

    public synchronized void reportLineLevelFL(final LineLevelFL lineLevelFL) {
        this.dos.writeByte(ControlId.REPORT_LINE_LEVEL_FL);
        this.dos.write(lineLevelFL);
    }

    public synchronized void reportClassLevelCov(final ClassLevelCoverage classLevelCov) {
        this.dos.writeByte(ControlId.REPORT_CLASS_LEVEL_COV);
        this.dos.write(classLevelCov);
    }

    public synchronized void reportMethodLevelCov(final MethodLevelCoverage methodLevelCov) {
        this.dos.writeByte(ControlId.REPORT_METHOD_LEVEL_COV);
        this.dos.write(methodLevelCov);
    }

    public synchronized void reportLineLevelCov(final LineLevelCoverage lineLevelCov) {
        this.dos.writeByte(ControlId.REPORT_LINE_LEVEL_COV);
        this.dos.write(lineLevelCov);
    }
}