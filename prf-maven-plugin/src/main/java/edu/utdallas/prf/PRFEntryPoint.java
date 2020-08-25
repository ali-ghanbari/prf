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

import edu.utdallas.prf.commons.misc.Ansi;
import edu.utdallas.prf.commons.process.LoggerUtils;
import edu.utdallas.prf.maven.NamedPluginInfo;
import edu.utdallas.prf.profiler.Profiler;
import edu.utdallas.prf.profiler.ProfilerOptions;
import edu.utdallas.prf.profiler.ProfilerResults;
import edu.utdallas.prf.profiler.cg.CGOptions;
import edu.utdallas.prf.profiler.fl.FLOptions;
import edu.utdallas.prf.profiler.fl.FLStrategy;
import edu.utdallas.prf.validator.PatchValidator;
import edu.utdallas.prf.validator.WorkStealingValidator;
import edu.utdallas.prf.validator.process.ValidationOutcome;
import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ClassInfo;
import org.pitest.classpath.ClassFilter;
import org.pitest.classpath.ClassPath;
import org.pitest.classpath.CodeSource;
import org.pitest.classpath.PathFilter;
import org.pitest.classpath.ProjectClassPaths;
import org.pitest.functional.predicate.Predicate;
import org.pitest.functional.prelude.Prelude;
import org.pitest.mutationtest.config.DefaultCodePathPredicate;
import org.pitest.mutationtest.config.DefaultDependencyPathPredicate;
import org.pitest.mutationtest.tooling.JarCreatingJarFinder;
import org.pitest.mutationtest.tooling.KnownLocationJavaAgentFinder;
import org.pitest.process.JavaAgent;
import org.pitest.process.JavaExecutableLocator;
import org.pitest.process.KnownLocationJavaExecutableLocator;
import org.pitest.process.LaunchOptions;
import org.pitest.process.ProcessArgs;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class PRFEntryPoint {
    private final ClassPath classPath;

    private final ClassByteArraySource byteArraySource;

    private final Predicate<String> appClassFilter;

    private final Predicate<String> testClassFilter;

    private final Collection<String> failingTests;

    private final File compatibleJREHome;

    private final List<String> childProcessArguments;

    private final int parallelism;

    private final long timeoutConstant;

    private final double timeoutPercent;

    private final boolean collectCoverage;

    private final FLOptions flOptions;

    private final FLStrategy flStrategy;

    private final CGOptions cgOptions;

    private final MavenProject mavenProject;

    private final PatchGenerationPlugin patchGenerationPlugin;

    private final NamedPluginInfo patchGenerationPluginInfo;

    private final PatchPrioritizationPlugin patchPrioritizationPlugin;

    private final NamedPluginInfo patchPrioritizationPluginInfo;

    private ProcessArgs defaultProcessArgs;

    private Collection<String> testClassNames;

    private Collection<Patch> patches;

    private PraPRTestComparator testComparator;

    private PRFEntryPoint(final ClassPath classPath,
                          final ClassByteArraySource byteArraySource,
                          final Predicate<String> appClassFilter,
                          final Predicate<String> testClassFilter,
                          final Collection<String> failingTests,
                          final File compatibleJREHome,
                          final List<String> childProcessArguments,
                          final int parallelism,
                          final long timeoutConstant,
                          final double timeoutPercent,
                          final boolean collectCoverage,
                          final FLOptions flOptions,
                          final FLStrategy flStrategy,
                          final CGOptions cgOptions,
                          final MavenProject mavenProject,
                          final PatchGenerationPlugin patchGenerationPlugin,
                          final NamedPluginInfo patchGenerationPluginInfo,
                          final PatchPrioritizationPlugin patchPrioritizationPlugin,
                          final NamedPluginInfo patchPrioritizationPluginInfo) {
        this.classPath = classPath;
        this.byteArraySource = byteArraySource;
        this.appClassFilter = appClassFilter;
        this.testClassFilter = testClassFilter;
        this.failingTests = failingTests;
        this.compatibleJREHome = compatibleJREHome;
        this.childProcessArguments = childProcessArguments;
        this.parallelism = parallelism;
        this.timeoutConstant = timeoutConstant;
        this.timeoutPercent = timeoutPercent;
        this.collectCoverage = collectCoverage;
        this.flOptions = flOptions;
        this.flStrategy = flStrategy;
        this.cgOptions = cgOptions;
        this.mavenProject = mavenProject;
        this.patchGenerationPlugin = patchGenerationPlugin;
        this.patchGenerationPluginInfo = patchGenerationPluginInfo;
        this.patchPrioritizationPlugin = patchPrioritizationPlugin;
        this.patchPrioritizationPluginInfo = patchPrioritizationPluginInfo;
    }

    public static PRFEntryPoint createEntryPoint() {
        return new PRFEntryPoint(null, null, null, null, null, null, null, 0, 0L, 0D, false, null, null, null, null, null, null, null, null);
    }

    public PRFEntryPoint withClassPath(final ClassPath classPath) {
        return new PRFEntryPoint(classPath, this.byteArraySource, this.appClassFilter, this.testClassFilter, this.failingTests, this.compatibleJREHome, this.childProcessArguments, this.parallelism, this.timeoutConstant, this.timeoutPercent, this.collectCoverage, this.flOptions, this.flStrategy, this.cgOptions, this.mavenProject, this.patchGenerationPlugin, this.patchGenerationPluginInfo, this.patchPrioritizationPlugin, this.patchPrioritizationPluginInfo);
    }

    public PRFEntryPoint withAppClassFilter(final Predicate<String> appClassFilter) {
        return new PRFEntryPoint(this.classPath, this.byteArraySource, appClassFilter, this.testClassFilter, this.failingTests, this.compatibleJREHome, this.childProcessArguments, this.parallelism, this.timeoutConstant, this.timeoutPercent, this.collectCoverage, this.flOptions, this.flStrategy, this.cgOptions, this.mavenProject, this.patchGenerationPlugin, this.patchGenerationPluginInfo, this.patchPrioritizationPlugin, this.patchPrioritizationPluginInfo);
    }

    public PRFEntryPoint withTestClassFilter(final Predicate<String> testClassFilter) {
        return new PRFEntryPoint(this.classPath, this.byteArraySource, this.appClassFilter, testClassFilter, this.failingTests, this.compatibleJREHome, this.childProcessArguments, this.parallelism, this.timeoutConstant, this.timeoutPercent, this.collectCoverage, this.flOptions, this.flStrategy, this.cgOptions, this.mavenProject, this.patchGenerationPlugin, this.patchGenerationPluginInfo, this.patchPrioritizationPlugin, this.patchPrioritizationPluginInfo);
    }

    public PRFEntryPoint withFailingTests(final Collection<String> failingTests) {
        return new PRFEntryPoint(this.classPath, this.byteArraySource, this.appClassFilter, this.testClassFilter, failingTests, this.compatibleJREHome, this.childProcessArguments, this.parallelism, this.timeoutConstant, this.timeoutPercent, this.collectCoverage, this.flOptions, this.flStrategy, this.cgOptions, this.mavenProject, this.patchGenerationPlugin, this.patchGenerationPluginInfo, this.patchPrioritizationPlugin, this.patchPrioritizationPluginInfo);
    }

    public PRFEntryPoint withCompatibleJREHome(final File compatibleJREHome) {
        return new PRFEntryPoint(this.classPath, this.byteArraySource, this.appClassFilter, this.testClassFilter, this.failingTests, compatibleJREHome, this.childProcessArguments, this.parallelism, this.timeoutConstant, this.timeoutPercent, this.collectCoverage, this.flOptions, this.flStrategy, this.cgOptions, this.mavenProject, this.patchGenerationPlugin, this.patchGenerationPluginInfo, this.patchPrioritizationPlugin, this.patchPrioritizationPluginInfo);
    }

    public PRFEntryPoint withChildProcessArguments(final List<String> childProcessArguments) {
        return new PRFEntryPoint(this.classPath, this.byteArraySource, this.appClassFilter, this.testClassFilter, this.failingTests, this.compatibleJREHome, childProcessArguments, this.parallelism, this.timeoutConstant, this.timeoutPercent, this.collectCoverage, this.flOptions, this.flStrategy, this.cgOptions, this.mavenProject, this.patchGenerationPlugin, this.patchGenerationPluginInfo, this.patchPrioritizationPlugin, this.patchPrioritizationPluginInfo);
    }

    public PRFEntryPoint withByteArraySource(final ClassByteArraySource byteArraySource) {
        return new PRFEntryPoint(this.classPath, byteArraySource, this.appClassFilter, this.testClassFilter, this.failingTests, this.compatibleJREHome, this.childProcessArguments, this.parallelism, this.timeoutConstant, this.timeoutPercent, this.collectCoverage, this.flOptions, this.flStrategy, this.cgOptions, this.mavenProject, this.patchGenerationPlugin, this.patchGenerationPluginInfo, this.patchPrioritizationPlugin, this.patchPrioritizationPluginInfo);
    }

    public PRFEntryPoint withParallelismDegree(final int parallelism) {
        return new PRFEntryPoint(this.classPath, this.byteArraySource, this.appClassFilter, this.testClassFilter, this.failingTests, this.compatibleJREHome, this.childProcessArguments, parallelism, this.timeoutConstant, this.timeoutPercent, this.collectCoverage, this.flOptions, this.flStrategy, this.cgOptions, this.mavenProject, this.patchGenerationPlugin, this.patchGenerationPluginInfo, this.patchPrioritizationPlugin, this.patchPrioritizationPluginInfo);
    }

    public PRFEntryPoint withTimeoutConstant(final long timeoutConstant) {
        return new PRFEntryPoint(this.classPath, this.byteArraySource, this.appClassFilter, this.testClassFilter, this.failingTests, this.compatibleJREHome, this.childProcessArguments, this.parallelism, timeoutConstant, this.timeoutPercent, this.collectCoverage, this.flOptions, this.flStrategy, this.cgOptions, this.mavenProject, this.patchGenerationPlugin, this.patchGenerationPluginInfo, this.patchPrioritizationPlugin, this.patchPrioritizationPluginInfo);
    }

    public PRFEntryPoint withTimeoutPercent(final double timeoutPercent) {
        return new PRFEntryPoint(this.classPath, this.byteArraySource, this.appClassFilter, this.testClassFilter, this.failingTests, this.compatibleJREHome, this.childProcessArguments, this.parallelism, this.timeoutConstant, timeoutPercent, this.collectCoverage, this.flOptions, this.flStrategy, this.cgOptions, this.mavenProject, this.patchGenerationPlugin, this.patchGenerationPluginInfo, this.patchPrioritizationPlugin, this.patchPrioritizationPluginInfo);
    }

    public PRFEntryPoint withSystemUnderRepair(final MavenProject mavenProject) {
        return new PRFEntryPoint(this.classPath, this.byteArraySource, this.appClassFilter, this.testClassFilter, this.failingTests, this.compatibleJREHome, this.childProcessArguments, this.parallelism, this.timeoutConstant, this.timeoutPercent, this.collectCoverage, this.flOptions, this.flStrategy, this.cgOptions, mavenProject, this.patchGenerationPlugin, this.patchGenerationPluginInfo, this.patchPrioritizationPlugin, this.patchPrioritizationPluginInfo);
    }

    public PRFEntryPoint withFLOptions(final FLOptions flOptions) {
        return new PRFEntryPoint(this.classPath, this.byteArraySource, this.appClassFilter, this.testClassFilter, this.failingTests, this.compatibleJREHome, this.childProcessArguments, this.parallelism, this.timeoutConstant, this.timeoutPercent, this.collectCoverage, flOptions, this.flStrategy, this.cgOptions, this.mavenProject, this.patchGenerationPlugin, this.patchGenerationPluginInfo, this.patchPrioritizationPlugin, this.patchPrioritizationPluginInfo);
    }

    public PRFEntryPoint withFLStrategy(final FLStrategy flStrategy) {
        return new PRFEntryPoint(this.classPath, this.byteArraySource, this.appClassFilter, this.testClassFilter, this.failingTests, this.compatibleJREHome, this.childProcessArguments, this.parallelism, this.timeoutConstant, this.timeoutPercent, this.collectCoverage, this.flOptions, flStrategy, this.cgOptions, this.mavenProject, this.patchGenerationPlugin, this.patchGenerationPluginInfo, this.patchPrioritizationPlugin, this.patchPrioritizationPluginInfo);
    }

    public PRFEntryPoint withCGOptions(final CGOptions cgOptions) {
        return new PRFEntryPoint(this.classPath, this.byteArraySource, this.appClassFilter, this.testClassFilter, this.failingTests, this.compatibleJREHome, this.childProcessArguments, this.parallelism, this.timeoutConstant, this.timeoutPercent, this.collectCoverage, this.flOptions, this.flStrategy, cgOptions, this.mavenProject, this.patchGenerationPlugin, this.patchGenerationPluginInfo, this.patchPrioritizationPlugin, this.patchPrioritizationPluginInfo);
    }

    public PRFEntryPoint withPatchGenerationPlugin(final PatchGenerationPlugin patchGenerationPlugin) {
        return new PRFEntryPoint(this.classPath, this.byteArraySource, this.appClassFilter, this.testClassFilter, this.failingTests, this.compatibleJREHome, this.childProcessArguments, this.parallelism, this.timeoutConstant, this.timeoutPercent, this.collectCoverage, this.flOptions, this.flStrategy, this.cgOptions, this.mavenProject, patchGenerationPlugin, this.patchGenerationPluginInfo, this.patchPrioritizationPlugin, this.patchPrioritizationPluginInfo);
    }

    public PRFEntryPoint withPatchGenerationPluginInfo(final NamedPluginInfo patchGenerationPluginInfo) {
        return new PRFEntryPoint(this.classPath, this.byteArraySource, this.appClassFilter, this.testClassFilter, this.failingTests, this.compatibleJREHome, this.childProcessArguments, this.parallelism, this.timeoutConstant, this.timeoutPercent, this.collectCoverage, this.flOptions, this.flStrategy, this.cgOptions, this.mavenProject, this.patchGenerationPlugin, patchGenerationPluginInfo, this.patchPrioritizationPlugin, this.patchPrioritizationPluginInfo);
    }

    public PRFEntryPoint withPatchPrioritizationPlugin(final PatchPrioritizationPlugin patchPrioritizationPlugin) {
        return new PRFEntryPoint(this.classPath, this.byteArraySource, this.appClassFilter, this.testClassFilter, this.failingTests, this.compatibleJREHome, this.childProcessArguments, this.parallelism, this.timeoutConstant, this.timeoutPercent, this.collectCoverage, this.flOptions, this.flStrategy, this.cgOptions, this.mavenProject, patchGenerationPlugin, this.patchGenerationPluginInfo, patchPrioritizationPlugin, this.patchPrioritizationPluginInfo);
    }

    public PRFEntryPoint withPatchPrioritizationPluginInfo(final NamedPluginInfo patchPrioritizationPluginInfo) {
        return new PRFEntryPoint(this.classPath, this.byteArraySource, this.appClassFilter, this.testClassFilter, this.failingTests, this.compatibleJREHome, this.childProcessArguments, this.parallelism, this.timeoutConstant, this.timeoutPercent, this.collectCoverage, this.flOptions, this.flStrategy, this.cgOptions, this.mavenProject, this.patchGenerationPlugin, this.patchGenerationPluginInfo, this.patchPrioritizationPlugin, patchPrioritizationPluginInfo);
    }

    public PRFEntryPoint withCollectCoverage(final boolean collectCoverage) {
        return new PRFEntryPoint(this.classPath, this.byteArraySource, this.appClassFilter, this.testClassFilter, this.failingTests, this.compatibleJREHome, this.childProcessArguments, this.parallelism, this.timeoutConstant, this.timeoutPercent, collectCoverage, this.flOptions, this.flStrategy, this.cgOptions, this.mavenProject, this.patchGenerationPlugin, this.patchGenerationPluginInfo, this.patchPrioritizationPlugin, this.patchPrioritizationPluginInfo);
    }

    public void run() throws NotFoundException, MojoFailureException {
        this.defaultProcessArgs = getDefaultProcessArgs();
        this.testClassNames = retrieveTestClassNames();
        if (this.testClassNames.isEmpty()) {
            throw new NotFoundException("no test classes found; perhaps whiteListPrefix is not set properly");
        }

        // run profiler
        final ProfilerResults profilerResults = runProfiler();

        // run patch generator
        this.patches = runPatchGenerator(profilerResults.getFLInfo(), profilerResults.getCoverageInfo());

        if (this.failingTests.isEmpty()) {
            Collections.addAll(this.failingTests, profilerResults.getFailingTestNames());
        } else {
            System.out.println("INFO: Inferred failing test cases are ignored; PRF shall use:");
            for (final String testName : this.failingTests) {
                System.out.println("\t" + testName);
            }
        }
        this.testComparator = new PraPRTestComparator(profilerResults.getTestsTiming(), this.failingTests);

        // run patch validator
        final Map<Patch, ValidationOutcome> validationStatusMap = new ConcurrentHashMap<>();
        final PatchValidator patchValidator = runPatchValidator(validationStatusMap);
        // classify patches
        List<Patch> plausiblePatches = new LinkedList<>();
        for (final Map.Entry<Patch, ValidationOutcome> entry : validationStatusMap.entrySet()) {
            if (entry.getValue() == ValidationOutcome.PLAUSIBLE) {
                plausiblePatches.add(entry.getKey());
            }
        }
//        if (plausiblePatches.isEmpty()) {
//            throw new NotFoundException("0 plausible patches found");
//        }
        // run patch fix report generator
        fixReportGenerator(plausiblePatches, profilerResults.getFLInfo(), profilerResults.getCoverageInfo());
    }

    private void fixReportGenerator(List<Patch> plausiblePatches,
                                    final FaultLocalizationInfo<? extends ProgramElement> flInfo,
                                    final CoverageInfo<? extends ProgramElement> covInfo) throws MojoFailureException {
        plausiblePatches = prioritizePatches(plausiblePatches, flInfo, covInfo);
        System.out.println("\n=====================================");
        System.out.println("    PRF Fix Report");
        System.out.println("=====================================");
        int patchNo = 0;
        for (final Patch patch : plausiblePatches) {
            System.out.println(Ansi.construct((++patchNo) + ".", Ansi.ColorCode.BOLD_FACE, Ansi.ColorCode.MAGENTA));
            final Map<String, String> affected = new HashMap<>();
            for (final PatchLocation location : patch.getLocations()) {
                String packageName = location.getClassName();
                final int lastDot = packageName.lastIndexOf('.');
                packageName = packageName.substring(0, lastDot);
                affected.put(location.getSourceFile().getAbsolutePath(), packageName);
            }
            final File src = new File(this.mavenProject.getBuild().getSourceDirectory());
            for (final String patchedFileName : affected.keySet()) {
                final File patchedFile = new File(patchedFileName);
                final File originalFile = new File(FileUtils.getFile(src, affected.get(patchedFileName).replace('.', File.separatorChar).split(File.pathSeparator)), patchedFile.getName());
                System.out.print(Ansi.construct("File:", Ansi.ColorCode.BOLD_FACE, Ansi.ColorCode.WHITE));
                System.out.println(" " + originalFile.getAbsolutePath());
                System.out.print(Ansi.construct("Patch:", Ansi.ColorCode.BOLD_FACE, Ansi.ColorCode.YELLOW));
                System.out.println();
                final ProcessBuilder pb = new ProcessBuilder("diff", patchedFile.getAbsolutePath(), originalFile.getAbsolutePath());
                try {
                    pb.inheritIO().start().waitFor();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            System.out.println("---------------------------------------------");
        }
    }

    private List<Patch> runPatchGenerator(final FaultLocalizationInfo<? extends ProgramElement> flInfo,
                                          final CoverageInfo<? extends ProgramElement> covInfo) throws MojoFailureException {
        final PatchGenerationPlugin plugin = this.patchGenerationPlugin;
        plugin.init();
        plugin.setFaultLocalizationInfo(flInfo);
        plugin.setCoverageInfo(covInfo);
        plugin.setProjectBaseDirectory(this.mavenProject.getBasedir());
        final Build build = this.mavenProject.getBuild();
        plugin.setProjectSourceDirectory(new File(build.getSourceDirectory()));
        plugin.setProjectTestSourceDirectory(new File(build.getTestSourceDirectory()));
        plugin.setProjectBuildDirectory(new File(build.getOutputDirectory()));
        plugin.setCompatibleJREHomeDirectory(this.compatibleJREHome);
//        plugin.setLauncherJDKHomeDirectory(this.patchGenerationPluginInfo.getLauncherJDKHomeDirectory());
        for (final Map.Entry<String, String> entry : this.patchGenerationPluginInfo.getParameters().entrySet()) {
            plugin.visitPluginParameter(entry.getKey(), entry.getValue());
        }
        final List<Patch> patches;
        try {
            patches = plugin.generate();
            plugin.close();
        } catch (Exception e) {
            throw new MojoFailureException(e.getMessage(), e.getCause());
        }
        return patches;
    }

    private List<Patch> prioritizePatches(List<Patch> plausiblePatches,
                                          final FaultLocalizationInfo<? extends ProgramElement> flInfo,
                                          final CoverageInfo<? extends ProgramElement> covInfo) throws MojoFailureException {
        final PatchPrioritizationPlugin plugin = this.patchPrioritizationPlugin;
        plugin.init();
        plugin.setFaultLocalizationInfo(flInfo);
        plugin.setCoverageInfo(covInfo);
        plugin.setProjectBaseDirectory(this.mavenProject.getBasedir());
        final Build build = this.mavenProject.getBuild();
        plugin.setProjectSourceDirectory(new File(build.getSourceDirectory()));
        plugin.setProjectTestSourceDirectory(new File(build.getTestSourceDirectory()));
        plugin.setProjectBuildDirectory(new File(build.getOutputDirectory()));
        plugin.setCompatibleJREHomeDirectory(this.compatibleJREHome);
        for (final Map.Entry<String, String> entry : this.patchPrioritizationPluginInfo.getParameters().entrySet()) {
            plugin.visitPluginParameter(entry.getKey(), entry.getValue());
        }
        final List<Patch> patches;
        try {
            patches = plugin.sort(plausiblePatches);
            plugin.close();
        } catch (Exception e) {
            throw new MojoFailureException(e.getMessage(), e.getCause());
        }
        return patches;
    }

    private PatchValidator runPatchValidator(final Map<Patch, ValidationOutcome> validationStatusMap) {
        final PatchValidator validator = new WorkStealingValidator(this.defaultProcessArgs,
                this.appClassFilter,
                this.testClassNames,
                this.testComparator,
                this.timeoutConstant,
                this.timeoutPercent,
                this.patches,
                validationStatusMap);
        validator.run(this.parallelism);
        return validator;
    }

    private ProfilerResults runProfiler() {
        return Profiler.runProfiler(this.defaultProcessArgs, this.appClassFilter, this.testClassNames, new ProfilerOptions(this.collectCoverage, this.flOptions, this.flStrategy, this.cgOptions));
    }

    private List<String> retrieveTestClassNames() {
        final ProjectClassPaths pcp = new ProjectClassPaths(this.classPath,
                defaultClassFilter(),
                defaultPathFilter());
        final CodeSource codeSource = new CodeSource(pcp);
        final ArrayList<String> testClassNames = new ArrayList<>();
        for (final ClassInfo classInfo : codeSource.getTests()) {
            testClassNames.add(classInfo.getName().asJavaName());
        }
        Collections.sort(testClassNames);
        testClassNames.trimToSize();
        return testClassNames;
    }

    private static PathFilter defaultPathFilter() {
        return new PathFilter(new DefaultCodePathPredicate(),
                Prelude.not(new DefaultDependencyPathPredicate()));
    }

    private ClassFilter defaultClassFilter() {
        return new ClassFilter(this.testClassFilter, this.appClassFilter);
    }

    private ProcessArgs getDefaultProcessArgs() {
        final LaunchOptions defaultLaunchOptions = new LaunchOptions(getJavaAgent(),
                getDefaultJavaExecutableLocator(),
                this.childProcessArguments,
                Collections.<String, String>emptyMap());
        return ProcessArgs.withClassPath(this.classPath)
                .andLaunchOptions(defaultLaunchOptions)
                .andStderr(LoggerUtils.err())
                .andStdout(LoggerUtils.out());
    }

    private JavaExecutableLocator getDefaultJavaExecutableLocator() {
        final File javaFile = FileUtils.getFile(this.compatibleJREHome, "bin", "java");
        return new KnownLocationJavaExecutableLocator(javaFile.getAbsolutePath());
    }

    private JavaAgent getJavaAgent() {
        final String jarLocation = (new JarCreatingJarFinder(this.byteArraySource))
                .getJarLocation()
                .value();
        return new KnownLocationJavaAgentFinder(jarLocation);
    }
}
