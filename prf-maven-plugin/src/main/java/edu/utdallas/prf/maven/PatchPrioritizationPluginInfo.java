package edu.utdallas.prf.maven;

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

import java.util.Map;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class PatchPrioritizationPluginInfo extends NamedPlugin {
    private Map<String, String> parameters;

    public PatchPrioritizationPluginInfo() { }

    public PatchPrioritizationPluginInfo(final String name,
                                         final Map<String, String> parameters) {
        this.name = name;
        this.parameters = parameters;
    }

    public Map<String, String> getParameters() {
        return this.parameters;
    }

    public void setParameters(final Map<String, String> parameters) {
        this.parameters = parameters;
    }
}
