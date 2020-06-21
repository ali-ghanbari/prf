package edu.utdallas.prf;

/*
 * #%L
 * prf-core
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

import java.io.Closeable;
import java.io.File;
import java.util.List;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public interface PatchGenerationPlugin extends PRFPluginBase, Closeable {
    // This method shall be called for each extra parameter intended to fine-tune the patch generation plugin
    void visitPluginParameter(String key, String value);

    // Returns a collection of generated patches
    List<Patch> generate() throws PatchGenerationPluginFailure;
}
