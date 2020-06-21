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

import edu.utdallas.prf.PRFPluginBase;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public abstract class NamedPlugin {
    protected String name; // name of the plugin

    public NamedPlugin() { }

    public NamedPlugin(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public boolean matches(final PRFPluginBase plugin) {
        return this.name.equalsIgnoreCase(plugin.name());
    }
}
