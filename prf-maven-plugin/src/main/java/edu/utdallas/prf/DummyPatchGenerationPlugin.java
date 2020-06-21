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

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class DummyPatchGenerationPlugin implements PatchGenerationPlugin {
    private File projectBaseDirectory;

    @Override
    public void init() {
        System.out.println("INFO: Dummy patch generation plugin initialized.");
    }

    @Override
    public void setFaultLocalizationInfo(FaultLocalizationInfo<? extends ProgramElement> faultLocalizationInfo) { }

    @Override
    public void setProjectBaseDirectory(File projectBaseDirectory) {
        this.projectBaseDirectory = projectBaseDirectory;
    }

    @Override
    public void setProjectSourceDirectory(File projectSourceDirectory) { }

    @Override
    public void setProjectTestSourceDirectory(File projectTestSourceDirectory) { }

    @Override
    public void setProjectBuildDirectory(File projectBuildDirectory) { }

    @Override
    public void setCompatibleJREHomeDirectory(File compatibleJREHomeDirectory) { }

    @Override
    public void visitPluginParameter(String key, String value) { }

    @Override
    public List<Patch> generate() throws PatchGenerationPluginFailure {
        final File patchesPool = new File(this.projectBaseDirectory, "patches-pool");
        if (!patchesPool.isDirectory()) {
            throw new PatchGenerationPluginFailure("Patches pool is non-existent");
        }
        return loadPatches(patchesPool);
    }

    private static List<Patch> loadPatches(final File patchesPool) {
        final File[] patchDirs = patchesPool.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        final List<Patch> patches = new LinkedList<>();
        if (patchDirs != null) {
            for (final File patchDir : patchDirs) {
                final List<PatchLocation> locations = new LinkedList<>();
                for (final File classFile : FileUtils.listFiles(patchDir, new String[]{"class"}, true)) {
                    locations.add(new PatchLocation(patchDir, classFile, null, 0, 0D));
                }
                if (!locations.isEmpty()) {
                    patches.add(new Patch(locations));
                }
            }
        }
        return patches;
    }

    @Override
    public String name() {
        return "dummy-patch-generation-plugin";
    }

    @Override
    public String description() {
        return "Dummy patch generation plugin";
    }

    @Override
    public void close() throws IOException {
        System.out.println("INFO: Dummy patch generation plugin closed.");
    }

    @Override
    public void setCoverageInfo(CoverageInfo<? extends ProgramElement> coverageInfo) { }
}
