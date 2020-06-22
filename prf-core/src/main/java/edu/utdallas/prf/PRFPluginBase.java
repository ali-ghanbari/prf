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

import java.io.File;

/**
 * !!Internal use only!!
 *
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
interface PRFPluginBase {
    // This method is called before any other methods
    void init();

    // Depending on the fault localization info. available, this could be file-, method-, or line-level FL info.
    void setFaultLocalizationInfo(FaultLocalizationInfo<? extends ProgramElement> faultLocalizationInfo);

    void setCoverageInfo(CoverageInfo<? extends ProgramElement> coverageInfo);

    // ${project.baseDir} for Maven-based projects
    void setProjectBaseDirectory(File projectBaseDirectory);

    // ${project.baseDir}/src/main/java for Maven-based Java projects
    void setProjectSourceDirectory(File projectSourceDirectory);

    // ${project.baseDir}/src/test/java for Maven-based Java projects
    void setProjectTestSourceDirectory(File projectTestSourceDirectory);

    // ${project.baseDir}/target/classes for Maven-based projects
    void setProjectBuildDirectory(File projectBuildDirectory);

    // Home directory of JDK compatible with system under repair
    void setCompatibleJREHomeDirectory(File compatibleJREHomeDirectory);

    // This method shall be called for each extra parameter intended to fine-tune the plugin
    void visitPluginParameter(String key, String value);

    // This should return the (globally unique) name of the plugin
    String name();

    // This should return a (short!) description of the plugin
    String description();
}
