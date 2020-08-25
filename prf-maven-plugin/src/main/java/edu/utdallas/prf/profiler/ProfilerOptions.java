package edu.utdallas.prf.profiler;

/*
 * #%L
 * prf-maven-plugin
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

import edu.utdallas.prf.profiler.cg.CGOptions;
import edu.utdallas.prf.profiler.fl.FLOptions;
import edu.utdallas.prf.profiler.fl.FLStrategy;
import org.apache.commons.lang3.Validate;

import java.io.Serializable;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class ProfilerOptions implements Serializable {
    private static final long serialVersionUID = 1L;

    final boolean collectCoverage;

    final FLOptions flOptions;

    final FLStrategy flStrategy;

    final CGOptions cgOptions;

    public ProfilerOptions(final boolean collectCoverage,
                           final FLOptions flOptions,
                           final FLStrategy flStrategy,
                           final CGOptions cgOptions) {
        Validate.isTrue(flStrategy == null || flStrategy instanceof Serializable);
        this.collectCoverage = collectCoverage;
        this.flOptions = flOptions;
        this.flStrategy = flStrategy;
        this.cgOptions = cgOptions;
    }
}
