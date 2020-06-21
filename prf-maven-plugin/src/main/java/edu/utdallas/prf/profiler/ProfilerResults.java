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

import java.util.Map;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public interface ProfilerResults {
    /**
     * Returns a hash table mapping each test case name to the time in took to execute
     * in milliseconds.
     *
     * @return A hash table mapping test case names to their execution time
     */
    Map<String, Long> getTestsTiming();

    /**
     * Returns an array of failing test case names.
     *
     * @return An array of failing test case names
     */
    String[] getFailingTestNames();

    FaultLocalizationInfo<? extends ProgramElement> getFLInfo();

    CoverageInfo<? extends ProgramElement> getCoverageInfo();
}
