package edu.utdallas.prf;

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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class DummyPatchPrioritizationPlugin implements PatchPrioritizationPlugin {
    @Override
    public void visitPluginParameter(String key, String value) { }

    @Override
    public List<Patch> sort(final Collection<Patch> patches) throws PatchPrioritizationFailure {
        return new ArrayList<>(patches);
    }

    @Override
    public void init() {
        System.out.println("INFO: Dummy patch prioritization plugin initialized.");
    }

    @Override
    public void setFaultLocalizationInfo(FaultLocalizationInfo<? extends ProgramElement> faultLocalizationInfo) { }

    @Override
    public void setCoverageInfo(CoverageInfo<? extends ProgramElement> coverageInfo) { }

    @Override
    public void setProjectBaseDirectory(File projectBaseDirectory) { }

    @Override
    public void setProjectSourceDirectory(File projectSourceDirectory) { }

    @Override
    public void setProjectTestSourceDirectory(File projectTestSourceDirectory) { }

    @Override
    public void setProjectBuildDirectory(File projectBuildDirectory) { }

    @Override
    public void setCompatibleJREHomeDirectory(File compatibleJREHomeDirectory) { }

    @Override
    public String name() {
        return "dummy-patch-prioritization-plugin";
    }

    @Override
    public String description() {
        return "Dummy patch prioritization plugin";
    }

    @Override
    public void close() throws IOException {
        System.out.println("INFO: Closing dummy patch prioritization plugin");
    }
}
