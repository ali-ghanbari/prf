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

import edu.utdallas.prf.profiler.fl.FLOptions;
import edu.utdallas.prf.profiler.fl.FLStrategy;
import org.apache.commons.lang3.Validate;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
class ProfilerArguments implements Serializable {
    private static final long serialVersionUID = 1L;

    final String whiteListPrefix;

    final Collection<String> testClassNames;

    final ProfilerOptions options;

    protected ProfilerArguments(final String whiteListPrefix,
                                final Collection<String> testClassNames,
                                final ProfilerOptions options) {
        Validate.isInstanceOf(Serializable.class, testClassNames);
        this.whiteListPrefix = whiteListPrefix;
        this.testClassNames = testClassNames;
        this.options = options;
    }
}