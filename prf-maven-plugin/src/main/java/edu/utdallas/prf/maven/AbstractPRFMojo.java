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

import edu.utdallas.memo.commons.functional.PredicateFactory;
import edu.utdallas.prf.DummyPatchGenerationPlugin;
import edu.utdallas.prf.DummyPatchPrioritizationPlugin;
import edu.utdallas.prf.PRFEntryPoint;
import edu.utdallas.prf.PRFPluginBase;
import edu.utdallas.prf.PatchGenerationPlugin;
import edu.utdallas.prf.PatchPrioritizationPlugin;
import edu.utdallas.prf.commons.misc.MemberNameUtils;
import edu.utdallas.prf.commons.misc.PropertyUtils;
import edu.utdallas.prf.profiler.cg.CGOptions;
import edu.utdallas.prf.profiler.fl.FLOptions;
import edu.utdallas.prf.profiler.fl.FLStrategy;
import edu.utdallas.prf.profiler.fl.FLStrategyImpl;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.pitest.classinfo.CachingByteArraySource;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classpath.ClassPath;
import org.pitest.classpath.ClassPathByteArraySource;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.functional.Option;
import org.pitest.functional.predicate.Predicate;
import org.reflections.Reflections;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class AbstractPRFMojo extends AbstractMojo {
    private static final int CACHE_SIZE;

    static {
        CACHE_SIZE = PropertyUtils.getIntProperty("prf.def.cache.size", 200);
    }

    protected File compatibleJREHome;

    private Predicate<String> appClassFilter;

    protected Predicate<String> testClassFilter;

    protected PatchGenerationPlugin patchGenerationPluginImpl;

    protected PatchPrioritizationPlugin patchPrioritizationPluginImpl;

    protected FLStrategy flStrategyImpl;

    @Parameter(property = "project", readonly = true, required = true)
    protected MavenProject project;

    @Parameter(property = "plugin.artifactMap", readonly = true, required = true)
    protected Map<String, Artifact> pluginArtifactMap;

    // -----------------------
    // ---- plugin params ----
    // -----------------------

    /**
     * Whitelists application classes for instrumentation and repair.
     * It is in the form of a set of regular expressions.
     * By default, "${project.groupId}.*" shall be used to determine application classes.
     */
    @Parameter(property = "targetClasses")
    protected Set<String> targetClasses;

    /**
     * Regular expression used to exclude classes.
     */
    @Parameter(property = "excludedClasses")
    protected Set<String> excludedClasses;

    /**
     * Using this parameter one can narrow down the space of selected test cases
     * during profiling and patch validation.
     * It is in the form of a set of regular expressions.
     */
    @Parameter(property = "targetTests")
    protected Set<String> targetTests;

    /**
     * A set of regular expressions used to exclude tests.
     */
    @Parameter(property = "excludedTests")
    protected Set<String> excludedTests;

    /**
     * A set of failing test cases: a patch is considered plausible if it
     * does not introduce regression and passes all these failing tests
     *
     * If you leave this parameter unspecified, PRF shall infer failing test
     * names automatically, but using this feature is not recommended for
     * Defects4J programs as reproducing failing tests in those programs
     * is difficult.
     */
    @Parameter(property = "failingTests")
    protected Set<String> failingTests;

    /**
     * A timeout constant of 5000 means that we have to wait at least 5 second to
     * decide whether or not we a test case is going to time out
     */
    @Parameter(property = "timeoutConstant", defaultValue = "5000")
    protected long timeoutConstant;

    /**
     * A timeout percent of 0.5 means that a patch running more than
     * 1.5 times of its original time will be deemed timed out
     */
    @Parameter(property = "timeoutPercent", defaultValue = "0.5")
    protected double timeoutPercent;

    /**
     * A list of JVM arguments used when creating a child JVM process, e.g. during
     * profiling or patch validation.
     *
     * If left unspecified, PRF will use the following arguments:
     * "-Xmx32g" and "-XX:MaxPermSize=16g"
     */
    @Parameter(property = "childJVMArgs")
    protected List<String> childJVMArgs;

    /**
     * Determines the degree of parallelism during patch validation. A value n, where
     * n > 0, means n CPU cores shall be used for validating patches using work-stealing
     * algorithm. Obviously, for n = 1, the patches will be validated one after the other.
     * For n <= 0, all available CPU cores shall be used. By default, this value is 0.
     * Requesting more parallelism than the available CPU cores is equivalent to setting
     * n to zero.
     */
    @Parameter(property = "parallelism", defaultValue = "0")
    protected int parallelism;

    /**
     * The name of patch generation plugin.
     * Example:
     * <patchGenerationPlugin>
     *     <name>capgen</name>
     *     <parameters>
     *         <launcherJDKHomeDirectory>/path/to/launcher/JDK/home/directory</launcherJDKHomeDirectory>
     *         <project>Chart</project>
     *         <bugId>1</bugId>
     *     </parameters>
     * </patchGenerationPlugin>
     * As for the name of the plugin, case does not matter.
     * The order of parameters does not matter. Please note that all the plugins used
     * should be present in the class-path.
     *
     * By default, a built-in patch generation plugin named "dummy-patch-generation-plugin"
     * shall be selected. This plugin shall merely point to the location of patches-pool under
     * the base directory of the project, and it shall fail in case the directory is non-existent.
     */
    @Parameter(property = "patchGenerationPlugin")
    protected NamedPluginInfo patchGenerationPlugin;

    @Parameter(property = "patchPrioritizationPlugin")
    protected NamedPluginInfo patchPrioritizationPlugin;

    @Parameter(property = "testCoverage", defaultValue = "false")
    protected boolean testCoverage;

    @Parameter(property = "flOptions", defaultValue = "OFF")
    protected FLOptions flOptions;

    @Parameter(property = "flStrategy", defaultValue = "OCHIAI")
    protected String flStrategy;

    @Parameter(property = "cgOptions", defaultValue = "OFF")
    protected CGOptions cgOptions;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        validateAndSanitizeParameters();

        final ClassPath classPath = createClassPath();
        final ClassByteArraySource byteArraySource = createClassByteArraySource(classPath);

        try {
            PRFEntryPoint.createEntryPoint()
                    .withAppClassFilter(this.appClassFilter)
                    .withByteArraySource(byteArraySource)
                    .withChildProcessArguments(this.childJVMArgs)
                    .withClassPath(classPath)
                    .withCompatibleJREHome(this.compatibleJREHome)
                    .withFailingTests(this.failingTests)
                    .withTimeoutConstant(this.timeoutConstant)
                    .withTimeoutPercent(this.timeoutPercent)
                    .withCollectCoverage(this.testCoverage)
                    .withFLOptions(this.flOptions)
                    .withFLStrategy(this.flStrategyImpl)
                    .withCGOptions(this.cgOptions)
                    .withTestClassFilter(this.testClassFilter)
                    .withParallelismDegree(this.parallelism)
                    .withPatchGenerationPlugin(this.patchGenerationPluginImpl)
                    .withPatchGenerationPluginInfo(this.patchGenerationPlugin)
                    .withSystemUnderRepair(this.project)
                    .withPatchPrioritizationPlugin(this.patchPrioritizationPluginImpl)
                    .withPatchPrioritizationPluginInfo(this.patchPrioritizationPlugin)
                    .run();
        } catch (Exception e) {
            e.printStackTrace();
            throw new MojoExecutionException(e.getMessage(), e.getCause());
        }
    }

    private void validateAndSanitizeParameters() throws MojoFailureException {
        final String jreHome = System.getProperty("java.home");
        if (jreHome == null) {
            throw new MojoFailureException("JAVA_HOME is not set");
        }
        this.compatibleJREHome = new File(jreHome);
        if (!this.compatibleJREHome.isDirectory()) {
            throw new MojoFailureException("Invalid JAVA_HOME");
        }

        final String groupId = this.project.getGroupId();

        if (this.excludedClasses == null) {
            this.excludedClasses = Collections.emptySet();
        }
        final Predicate<String> excludedClassFilter = PredicateFactory.orGlobs(this.excludedClasses);

        if (this.targetClasses == null) {
            this.targetClasses = new HashSet<>();
        }
        if (this.targetClasses.isEmpty()) {
            this.targetClasses.add(groupId + ".*");
        }
        this.appClassFilter = PredicateFactory.orGlobs(this.targetClasses);
        this.appClassFilter = PredicateFactory.and(this.appClassFilter, PredicateFactory.not(excludedClassFilter));

        if (this.excludedTests == null) {
            this.excludedTests = Collections.emptySet();
        }
        final Predicate<String> excludedTestFilter = PredicateFactory.orGlobs(this.excludedTests);

        if (this.targetTests == null) {
            this.targetTests = new HashSet<>();
        }
        if (this.targetTests.isEmpty()) {
            this.targetTests.add(String.format("%s*Test", groupId));
            this.targetTests.add(String.format("%s*Tests", groupId));
        }
        this.testClassFilter = PredicateFactory.orGlobs(this.targetTests);
        this.testClassFilter = PredicateFactory.and(this.testClassFilter, PredicateFactory.not(excludedTestFilter));

        if (this.timeoutConstant < 0L) {
            throw new MojoFailureException("Invalid timeout bias");
        }

        if (this.timeoutConstant < 1000L) {
            getLog().warn("Too small timeout bias");
        }

        if (this.timeoutPercent < 0.D) {
            throw new MojoFailureException("Invalid timeout coefficient");
        }

        if (this.failingTests == null || this.failingTests.isEmpty()) {
            this.failingTests = new HashSet<>();
        } else {
            final Set<String> failingTests = new HashSet<>();
            for (final String testName : this.failingTests) {
                failingTests.add(MemberNameUtils.sanitizeTestName(testName));
            }
            this.failingTests = failingTests;
        }

        if (this.childJVMArgs == null || this.childJVMArgs.isEmpty()) {
            this.childJVMArgs = Arrays.asList("-Xmx32g", "-XX:MaxPermSize=16g");
        }

        final int totalCores = Runtime.getRuntime().availableProcessors();
        this.parallelism = Math.min(this.parallelism, totalCores);

        this.patchGenerationPluginImpl = new DummyPatchGenerationPlugin();
        if (this.patchGenerationPlugin.getName() != null) {
            this.patchGenerationPluginImpl = findPRFPlugin(PatchGenerationPlugin.class);
            if (this.patchGenerationPluginImpl == null) {
                throw new MojoFailureException("No plugin with the name "
                        + this.patchGenerationPlugin.getName() + " found in classpath." +
                        " This is perhaps a classpath issue.");
            }
        }
        final NamedPluginInfo pgInfo = this.patchGenerationPlugin;
        if (pgInfo.getParameters() == null) {
            pgInfo.setParameters(Collections.<String, String>emptyMap());
        }
        getLog().info("Found Patch Generation Plugin: " + this.patchGenerationPluginImpl.name()
                + " (" + this.patchGenerationPluginImpl.description() + ")");

        this.patchPrioritizationPluginImpl = new DummyPatchPrioritizationPlugin();
        if (this.patchPrioritizationPlugin.getName() != null) {
            this.patchPrioritizationPluginImpl = findPRFPlugin(PatchPrioritizationPlugin.class);
            if (this.patchPrioritizationPluginImpl == null) {
                throw new MojoFailureException("No plugin with the name "
                        + this.patchPrioritizationPlugin.getName() + " found in classpath." +
                        " This is perhaps a classpath issue.");
            }
        }
        final NamedPluginInfo ppInfo = this.patchPrioritizationPlugin;
        if (ppInfo.getParameters() == null) {
            ppInfo.setParameters(Collections.<String, String>emptyMap());
        }
        getLog().info("Found Patch Prioritization Plugin: " + this.patchPrioritizationPluginImpl.name()
                + " (" + this.patchPrioritizationPluginImpl.description() + ")");

        if (this.flOptions != FLOptions.OFF) {
            this.flStrategyImpl = FLStrategyImpl.valueOf(this.flStrategy);
        }
    }

    private <T extends PRFPluginBase> T findPRFPlugin(Class<T> type) {
        Reflections reflections = new Reflections(Thread.currentThread().getContextClassLoader());
        for (final Class<? extends T> pluginClass : reflections.getSubTypesOf(type)) {
            final T plugin;
            try {
                plugin = pluginClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            if (this.patchGenerationPlugin.matches(plugin)) {
                return plugin;
            }
        }
        return null;
    }

    private ClassPath createClassPath() {
        final List<File> classPathElements = new ArrayList<>();
        classPathElements.addAll(getProjectClassPath());
        classPathElements.addAll(getPluginClassPath());
        return new ClassPath(classPathElements);
    }

    private List<File> getProjectClassPath() {
        final List<File> classPath = new ArrayList<>();
        try {
            for (final Object cpElement : this.project.getTestClasspathElements()) {
                classPath.add(new File((String) cpElement));
            }
        } catch (DependencyResolutionRequiredException e) {
            getLog().warn(e);
        }
        return classPath;
    }

    private List<File> getPluginClassPath() {
        final List<File> classPath = new ArrayList<>();
        for (Object artifact : this.pluginArtifactMap.values()) {
            final Artifact dependency = (Artifact) artifact;
            if (isRelevantDep(dependency)) {
                classPath.add(dependency.getFile());
            }
        }
        return classPath;
    }

    private boolean isRelevantDep(final Artifact dependency) {
        return dependency.getGroupId().equals("edu.utdallas")
                && dependency.getArtifactId().equals("prf-maven-plugin");
    }

    private ClassByteArraySource createClassByteArraySource(final ClassPath classPath) {
        final ClassPathByteArraySource cpbas = new ClassPathByteArraySource(classPath);
        final ClassByteArraySource cbas = fallbackToClassLoader(cpbas);
        return new CachingByteArraySource(cbas, CACHE_SIZE);
    }

    // this method is adopted from PIT's source code
    private ClassByteArraySource fallbackToClassLoader(final ClassByteArraySource bas) {
        final ClassByteArraySource clSource = ClassloaderByteArraySource.fromContext();
        return new ClassByteArraySource() {
            @Override
            public Option<byte[]> getBytes(String clazz) {
                final Option<byte[]> maybeBytes = bas.getBytes(clazz);
                if (maybeBytes.hasSome()) {
                    return maybeBytes;
                }
                return clSource.getBytes(clazz);
            }
        };
    }
}
