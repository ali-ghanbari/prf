package edu.utdallas.capgen.plugin;

/*
 * #%L
 * capgen-prf-plugin
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
import edu.utdallas.prf.Patch;
import edu.utdallas.prf.PatchGenerationPlugin;
import edu.utdallas.prf.PatchGenerationPluginFailure;
import edu.utdallas.prf.PatchLocation;
import edu.utdallas.prf.ProgramElement;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class CapGenPatchGenerationPlugin implements PatchGenerationPlugin {
    private File projectBaseDirectory;

    private File launcherJDKHomeDirectory;

    private File compatibleJDKHomeDirectory;

    private String subject;

    private int bugId;

    @Override
    public void visitPluginParameter(final String key, final String value) {
        if ("subject".equals(key)) {
            this.subject = value;
        }
        if ("bugId".equals(key)) {
            this.bugId = Integer.parseInt(value);
        }
        if ("launcherJDKHomeDirectory".equals(key)) {
            this.launcherJDKHomeDirectory = new File(value);
        }
    }

    private static InputStream getAuxFileInputStream(final String subject,
                                                     final int bugId) {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        return Objects.requireNonNull(loader.getResourceAsStream(String.format("aux/%s-%d.tar.gz", subject, bugId)));
    }

    public static void decompress(final InputStream is, final File out) throws IOException {
        try (final InputStream gin  = new GzipCompressorInputStream(is);
             final TarArchiveInputStream fin = new TarArchiveInputStream(gin)){
            TarArchiveEntry entry;
            while ((entry = fin.getNextTarEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                File currentFile = new File(out, entry.getName());
                File parent = currentFile.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }
                IOUtils.copy(fin, new FileOutputStream(currentFile));
            }
        }
    }

    private static void obtainCapGenJAR() throws IOException {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        IOUtils.copy(Objects.requireNonNull(loader.getResourceAsStream("capgen-wrapper-1.0.jar")),
                new FileOutputStream("capgen-wrapper-1.0.jar"));
    }

    private static InputStream getCapGenRuntimeArchiveInputStream() {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        return Objects.requireNonNull(loader.getResourceAsStream("lib.tar.gz"));
    }

    private void runCapGen() throws IOException, InterruptedException {
        final String javaExec = FileUtils.getFile(this.launcherJDKHomeDirectory, "bin", "java").getAbsolutePath();
        final ProcessBuilder pb = new ProcessBuilder(javaExec,
                "-cp",
                "capgen-runtime-lib/*:capgen-wrapper-1.0.jar",
                "edu.utdallas.capgen.wrapper.CapGenWrapper",
                this.subject,
                String.valueOf(this.bugId),
                this.projectBaseDirectory.getAbsolutePath(),
                this.compatibleJDKHomeDirectory.getAbsolutePath());
        pb.inheritIO().start().waitFor();
    }

    @Override
    public List<Patch> generate() throws PatchGenerationPluginFailure {
        final InputStream auxFileInputStream = getAuxFileInputStream(this.subject, this.bugId);
        final InputStream libInputStream = getCapGenRuntimeArchiveInputStream();
        try {
            decompress(auxFileInputStream, this.projectBaseDirectory);
            decompress(libInputStream, this.projectBaseDirectory);
            obtainCapGenJAR();
            runCapGen();
            final List<Patch> patches = loadPatches(new File(this.projectBaseDirectory, "mutated"));
            System.out.println("INFO: Found " + patches.size() + " patches.");
            return patches;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
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
    public void init() { }

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
    public void setCompatibleJREHomeDirectory(File compatibleJREHomeDirectory) {
        this.compatibleJDKHomeDirectory = compatibleJREHomeDirectory.getParentFile();
    }

    @Override
    public String name() {
        return "capgen";
    }

    @Override
    public String description() {
        return "Self-Contained CapGen Patch Generation Plugin";
    }

    @Override
    public void close() throws IOException {
        FileUtils.deleteDirectory(new File(this.projectBaseDirectory, "faultSpace"));
        FileUtils.deleteDirectory(new File(this.projectBaseDirectory, "ingredients"));
        FileUtils.deleteDirectory(new File(this.projectBaseDirectory, "capgen-runtime-lib"));
        (new File(this.projectBaseDirectory, String.format("%s_%d_info.txt", this.subject, this.bugId))).delete();
        (new File(this.projectBaseDirectory, "relatedList.txt")).delete();
        (new File(this.projectBaseDirectory, "testCaseList.txt")).delete();
        (new File(this.projectBaseDirectory, "capgen-wrapper-1.0.jar")).delete();
        final File spooned = new File(this.projectBaseDirectory, "spooned");
        if (spooned.isDirectory()) {
            FileUtils.deleteDirectory(spooned);
        }
    }

    @Override
    public void setCoverageInfo(CoverageInfo<? extends ProgramElement> coverageInfo) { }
}
