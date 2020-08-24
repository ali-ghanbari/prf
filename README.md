# PRF: A Framework for Building Automatic Program Repair Prototypes for JVM-based Language

## Table of Contents
- [Introduction](#introduction)
- [PRF Setup](#prf-setup)
    * [Maven Project Setup](#maven-project-setup)
- [PRF Demonstration](#prf-demonstration)
    * [Example 1: Chart-1 of Defects4J](#example-1-chart-1-of-defects4j)
    * [Example 2: Math-65 of Defects4J](#example-2-math-65-of-defects4j)
    * [YouTube Demo Video](#youtube-demo-video)
- [Developing PRF Plugins](#developing-prf-plugins)
- [System Requirements](#system-requirements)

## Introduction
PRF (**P**rogram **R**epair **F**ramework) provides a framework consisting
of various tools from fault localization at different level of granularity
to fast, safe patch validation to a highly configurable fix report generation 
for developing automated program repair (APR) tools. The goal of PRF is to
help APR researchers re-use components common to all test-based generate-and-validate
APR techniques and make it easier for them to build their prototypes. With the help
of extant APR libraries such as [ASTOR](https://github.com/SpoonLabs/astor),
developers of APR tools will only need to focus on design and implementation
of more effective patch generation algorithms. Furthermore, since PRF is highly
configurable, they will be able customize other aspects of their systems, such
as fault localization, patch validation, and patch prioritization/filtering.

This document presents an overview of the system. We have also provided the users
with a pre-configured patch generation plugin based on the recent APR tool
[CapGen](https://github.com/justinwm/CapGen) for demonstration purposes.

## PRF Setup
This repository contains source code of PRF, so you can directly clone
the repository, compile it, and install the Maven plugin on your local
machine. You can follow the instructions to clone, compile and install
PRF Maven plugin. Along with the Maven plugin, other library files also
get installed on your local machine, so the APR system developers can
make their own patch generation as well as patch prioritization or
filtering plugins. In the section that follows, you will find a couple
of example buggy projects that you can try to fix using our patch
generation plugin for CapGen.

**Step 0:** Please read [system requirements](#system-requirements)
section and make sure you have at least Maven, Git, JDK 1.7
(in case you wish to fix Defects4J projects), and JDK 1.8 (to run
the core part of our CapGen plugin) installed on your computer
and the environment variable `JAVA_HOME` is pointing to the
installation patch of JDK 1.7. For example, on Unix machine that
I am using, JDK 1.7 is installed under `/Library/Java/JavaVirtualMachines/jdk1.7.0_80.jdk/Contents/Home`.
So, I use the following command to set `JAVA_HOME` variable. 
```shell script
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.7.0_80.jdk/Contents/Home
```
After this, when I run the command `mvn -ver`, I see something like
the following
```text
Apache Maven 3.5.4 (1edded0938998edf8bf061f1ceb3cfdeccf443fe; 2018-06-17T13:33:14-05:00)
Maven home: /opt/apache-maven-3.5.4
Java version: 1.7.0_80, vendor: Oracle Corporation, runtime: /Library/Java/JavaVirtualMachines/jdk1.7.0_80.jdk/Contents/Home/jre
Default locale: en_US, platform encoding: UTF-8
OS name: "mac os x", version: "10.15.5", arch: "x86_64", family: "mac"
```

**Step 1:** Use the following command to clone the repository on your
computer.
```shell script
git clone https://github.com/ali-ghanbari/prf
```

**Step 2:** Navigate to the project directory, compile, and install the
project on your local repository by using the following commands.
```shell script
cd prf
mvn clean install -Dhttps.protocols=TLSv1.2
```
Downloading all the dependencies and making the JAR files might take up to
a minute depending on the load of your computer and the speed of your
Internet connection. Please note that the command line switch
`-Dhttps.protocols=TLSv1.2` is necessary when you are compiling projects
using JDK 1.7 and older. This is a security measure that is in place
since 2018. After seeing (green) `BUILD SUCCESS` message on your screen,
your PRF Maven plugin, and the related library files, are ready to use.

During the first reading, you may skip the following subsection and see
the examples in the next section so that you can experience PRF in action
while a patch generation plugin is installed on it.

### Maven Project Setup
Assuming that you have already followed the instructions given in
[setup](#prf-setup) section to install PRF Maven plugin, and its companion
libraries, in this section we will give detailed instructions for setting
up PRF for fixing bugs in arbitrary Maven-based projects.

In order to be able to use PRF Maven plugin, you need to introduce
it as a plugin in the POM file of the target project. This can be done
by adding the following template XML snippet under the `<plugins>` tag
in the `pom.xml` of the target project.
```xml
<plugin>
    <groupId>edu.utdallas</groupId>
    <artifactId>prf-maven-plugin</artifactId>
    <version>1.0-SNAPSHOT</version>
<!--    <configuration>                                                             -->
<!-- ***************************** PROFILER OPTIONS ******************************* -->

        <!-- <flOptions>OFF</flOptions>                                             -->
        <!-- <flStrategy>OCHIAI</flStrategy>                                        -->
        <!-- <cgOptions>OFF</cgOptions>                                             -->
        <!-- <testCoverage>false</testCoverage>                                     -->
        <!-- <failingTests>                                                         -->
        <!--    <failingTest>fully.qualified.test.Class1::testMethod1</failingTest> -->
        <!--    ...                                                                 -->
        <!--    <failingTest>fully.qualified.test.ClassN::testMethodN</failingTest> -->
        <!-- </failingTests>                                                        -->

<!-- ************************** PATCH GENERATOR OPTIONS *************************** -->

        <!-- <patchGenerationPlugin>                                                -->
        <!--    <name>dummy-patch-generation-plugin</name>                          -->
        <!--    <parameters>                                                        -->
        <!--        <parameter1>Value for parameter 1</parameter1>                  -->
        <!--        ...                                                             -->
        <!--        <parameterN>Value for parameter N</parameterN>                  -->
        <!--    </parameters>                                                       -->
        <!-- </patchGenerationPlugin>                                               -->

<!-- ************************** PATCH VALIDATOR OPTIONS *************************** -->

        <!-- <parallelism>0</parallelism>                                           -->
        <!-- <timeoutConstant>5000</timeoutConstant>                                -->
        <!-- <timeoutPercent>0.5</timeoutPercent>                                   -->

<!-- *********************** FIX REPORT GENERATION OPTIONS ************************ -->

        <!-- <patchPrioritizationPlugin>                                            -->
        <!--    <name>dummy-patch-prioritization-plugin</name>                      -->
        <!--    <parameters>                                                        -->
        <!--        <parameter1>Value for parameter 1</parameter1>                  -->
        <!--        ...                                                             -->
        <!--        <parameterK>Value for parameter K</parameterK>                  -->
        <!--    </parameters>                                                       -->
        <!-- </patchPrioritizationPlugin>                                           -->

<!-- ****************************** GENERAL OPTIONS ******************************* -->
        <!-- <targetClasses>                                                        -->
        <!--    <param>${project.groupId}.*</param>                                 -->
        <!-- </targetClasses>                                                       -->
        <!-- <excludedClasses> </excludedClasses>                                   -->
        <!-- <targetTests>                                                          -->
        <!--    <param>{whiteListPrefix}.*Test</param>                              -->
        <!--    <param>{whiteListPrefix}.*Tests</param>                             -->
        <!-- </targetTests>                                                         -->
        <!-- <excludedTests> </excludedTests>                                       -->
        <!-- <childJVMArgs>                                                         -->
        <!--     <childJVMArg>-Xmx16g</childJVMArg>                                 -->
        <!--     ...                                                                -->
        <!--     <childJVMArg>Mth argument to the child JVM</childJVMArg>           -->
        <!-- </childJVMArgs>                                                        -->
<!--    </configuration>                                                            -->

<!-- ************ DEPENDENCIES FOR EXTERNAL LIBRARIES, E.G., PLUGINS ************** -->

<!--    <dependencies>                                                              -->
<!--        <dependency> 1, e.g., your awesome patch generation plugin</dependency> -->
<!--        <dependency> 2, e.g., your cool patch prioritization plugin</dependency>-->
<!--        ...                                                                     -->
<!--    </dependencies>                                                             -->
</plugin>
```
The parts under the tag `<congigutation>` are all optional; we have shown their
default values (or a description of the general form of their values) in comments.
The part for dependencies is also optional. Obviously, if the repair process does
not take advantage of any external library, e.g. a patch generation/prioritization
plugin, there will be no need for a dependency. We stress that the XML snippet
shown above is just a template, and the users will need to specialize values and
get rid of those descriptions and/or ellipsis marks.

In what follows, we explain each group of options starting from top.

#### Profiler Options
The current version of PRF allows the user to fine-tune the profiler via 4 set of
parameters. Using `<flOptions>` the user specifies the level of granularity of fault
localization or turn it off altogether. This parameter takes values `OFF` for no
fault localization (the default option), `CLASS_LEVEL` for class-level fault
localization, `METHOD_LEVEL` for method-level fault localization, and `LINE_LEVEL`
for source line level fault localization. Fault localization is off by default.
It is worth mentioning that the more fine-grained class-level fault localization,
as opposed to file-level fault localization, is not popular in fault localization
literature. However, most of the times these two coincide and we one can always
aggregate class-level suspiciousness values to calculate file-level fault localization
information<sup>:one:</sup>.

Using the parameter `<flStrategy>` the user can specify the fault localization
formula to be used for calculating suspiciousness values for each program elements.
In the current version of PRF, this parameter, takes values `OCHIAI` and `TARANTULA`.
By default, the value `OCHIAI` will be used, i.e. PRF will calculate Ochiai suspiciousness
values for each program element. APR prototype developers can extend PRF with more
suspiciousness calculation formulae. Please refer to our JavaDoc shipped with the
source code for more details. It is worth noting that this parameter will make sense
only when fault localization is not off.

Using the parameter `<cgOptions>` the user can choose the call-graph analysis they
desire. By default, this parameter is set to `OFF` meaning that no call-graph analysis
shall take place. The parameter can take other values, e.g., `DYNAMIC`
(to do dynamic analysis), `CHA` (to do static class hierarchy analysis), `RTA`
(tom do static rapid type analysis), or `ZERO_CFA` (to do static 0-CFA analysis).
Please note that the values `CHA`, `RTA`, and `ZERO_CFA` will make PRF to fork a
thread alongside the profiler to do the static analysis. In case `DYNAMIC` is used,
dynamic call-graph analysis shall be done during profiling.

Using `<testCoverage>` the user can determine whether any coverage information should
be passed to other components. If true, and fault localization is off, line-level
coverage shall be computed. If this parameter is true or fault localization is activated,    
the level of granularity for the coverage information shall be dictated by the parameter
`<flOptions>`.

By default, profiler shall infer failing test cases. However, sometimes, due to configuration
issues (that happen a lot in case of Defects4J programs), the inferred results might not
be accurate. Therefore, the users manually list the set of test cases that they expect
to fail. Please note that PRF expects the fully qualified name of test class followed by
a separator, which could be `::`, `:`, or simply `.`, and the name of the test case.

#### Patch Generator Options
By default, `dummy-patch-generator-plugin` shall be used during patch generation phase.
This plugin does nothing but looking for the patches under the base directory of the
target project. The plugin expects the patches to be located under a directory named
`patches-pool`. This directory is expected to contain a subdirectory for ech patch.
class files for each patch should reside inside those subdirectories. In case the
subdirectories contain source code for the patches as well, upon reporting the fix
report, the `diff` of the patched file and their corresponding original file shall
be displayed.

The user can pick a patch generation plugin using the parameter `<patchGenerationPlugin>`.
Under the tag for this parameter, it is necessary to specify the name of the desired
patch generation plugin (if left unspecified, `dummy-patch-generator-plugin` shall be
used). After naming a patch generation plugin, depending on the plugin, the user might
want to pass one or more arguments to the plugin. Arguments are specified using a
key-value format, written in `<key>value</key>` form under the tag `<parameters>` under
`<patchGenerationPlugin>`. The user might pass arbitrary number of parameters to the
plugin. For example, in order to run CapGen patch generation plugin on the bug Chart-1
from Defects4J, I had to add the plugin as follows.

```xml
<patchGenerationPlugin>
    <name>capgen</name>
    <parameters>
        <launcherJDKHomeDirectory>/Library/Java/JavaVirtualMachines/jdk1.8.0_231.jdk/Contents/Home</launcherJDKHomeDirectory>
        <subject>Chart</subject>
        <bugId>1</bugId>
    </parameters>
</patchGenerationPlugin>
``` 

Please note that if the user wishes to use a custom patch generation plugin, they want
to put the plugin on the classpath by specifying a dependency after closing
`<configuration>` tag. For example, for CapGen patch generation plugin we have to
use the following dependency specification.

```xml
<dependencies>
    <dependency>
        <groupId>edu.utdallas</groupId>
        <artifactId>capgen-prf-plugin</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```

#### Patch Validator Options
Patch validator is a subsystem of PRF that given a pool of generated patches runs
the existing test suite against each patch to find the patches that pass all the
test cases, a.k.a. plausible patches. In order to do it efficiently, and to use the
full power of modern microprocessors, PRF validates patches in parallel using a
work-stealing algorithm. The degree of parallelism can be specified using the
parameter `<parallelism>`. By default, this value is 0, meaning that all the available
CPU cores shall be used to validates patches parallely. Any value less than 0 or
greater than or equal the number of available CPU cores have the same effect.
The user can request for sequential patch validation by setting `<parallelism>`
to 1 or opt for lower degree of parallelism. 

As we have mentioned in our paper, patching sometimes might create infinite loops.
In theory there is no way to know that upfront, but by setting a timeout for test
execution, one is more likely to identify and filter out such patches. To that end,
PRF receives two arguments from the user: `<timeoutConstant>` and `<timeoutPercent>`.
The timeout for a test case `t` is calculated by `<timeoutConstant> + (1 + <timeoutPercent>) * t(T)`,
where `T(t)` denotes the original execution time for the test case (measured in the
profiling phase). Default values for `<timeoutConstant>` and `<timeoutPercent>` are
5000 and 0.5, respectively. Therefore, a test case taking more than 1.5 times of its
original execution time plus 5 seconds, is deemed timed out. A patched with at least
one timed out test is deemed timed out and shall not be reported to the user. 

#### Fix Report Generator Options
Fix report generator, as the name suggests, is responsible for generating fix reports.
This is nothing but showing the list of plausible patches (with enough information
to enable the users to apply the patches) so that the user can find adopt the genuine
fix for the bug among the plausible patches.

By default, PRF uses `dummy-patch-prioritization-plugin` which simply puts the
patches in an arbitrary order. However, the users can have their own patch
prioritization plugin. Through the parameter `<patchPrioritizationPlugin>`, the
users can specify the name of a patch generation plugin and pass parameters to
the plugin. This is quite similar to what we have in the case of picking a custom
patch generation plugin, and we don't feel it is necessary to repeat explaining the
procedure here.

#### General/System-wide Options
PRF uses regular expressions to identify the application classes of the target
project during profiling, patch validation, and fix report generation.
The user can add arbitrarily many regular expressions to target as many application
classes as they desire. By default, `${project.groupId}.*` shall be used
as the only regular expression in `<targetClasses>`. The user can exclude some
application classes with the help of `<excludeClasses>`, thereby achieving desired
precision in specifying application classes.

Last but not least, since test cases of some large and complex projects might
be demanding, we have provided the user with a mechanism through which they
can specify the maximum amount of allotted heap space (and/or permanent
generation space, if applicable) for the child _profiler_ or _validator_
processes.

## PRF Demonstration
In order to get a feeling of how PRF operates with a patch generation
plugin installed on it, we have shipped this version of PRF with a
patch generation plugin for the recent APR tool CapGen. CapGen is a
source code level APR system that is able to fix 22 bugs from the
well-known bug database [Defects4J](https://github.com/rjust/defects4j).
We have included two sample bugs in this repository but instructions
for other Maven-based projects are also given. Last but not least,
we have uploaded a demo video on YouTube demonstrating all these steps.  

### Example 1: Chart-1 of Defects4J
JFree Chart is a Maven-based real-world Java project with more than
96K LoC and 2,200 unit tests. Chart-1 is one of the 26 bugs of this
project in Defects4J bug database, and it is one of the 22 Defects4J
bugs that CapGen fixes. CapGen generates 138 candidate patches for
this bug among which one of them passes all the test cases and that
particular patch happens to be a genuine fix for the bug.  

After installing PRF Maven plugin, and CapGen patch generation plugin
on your local repository, as described in the previous section, please
follow the instructions given below to have PRF fix the bug using its
CapGen patch generation plugin.

It is worth noting that if you want to apply PRF with CapGen plugin
on it to a Maven-based projects other than those stated in their original
[paper](https://dl.acm.org/doi/10.1145/3180155.3180233), you want to
have the tool [Understand](https://scitools.com/features/) installed on
your computer and configured properly. CapGen uses the tool to generate
fix ingredients used during repair. Furthermore, due to the specific
design on CapGen, the name of the folder containing the buggy project
should be of the form `[ProjectName]_[BugId]_buggy` where `[ProjectName]`
denotes a project name in Defects4J (namely, Chart, Lang, Math, etc.) and
`[BugId]` is a number which depending on the project can be any number greater
than or equal 1 (e.g., for Chart this `[BugId]` should be between 1 and 26,
while for Math, this could be between 1 and 106). For example, the folder
containing the bug Chart-1 from Defects4J should be named `Chart_1_buggy`.
Since we did not have access to the source code of CapGen, we could not
obviate this peculiarity.

**Step 1:** Navigate to the bug project folder, adjust the POM file, build the
project, and invoke PRF.

Please note that in order to be able to invoke CapGen, you will need to make sure
that you have adjusted the POM file for the project correctly so that our CapGen
plugin will know the location of JDK 1.8 home directory. To do so, after navigating
to the example bug project, you can open the file `pom.xml` via your favorite editor and
search for the term `launcherJDKHomeDirectory`. You will need to update the value
of the corresponding tag so that it denotes the location of JDK 1.8 home directory
on your computer.

```shell script
cd examples
cd Chart_1_buggy
# Please make sure you have adjusted the POM file as described above
mvn clean test -DskipTests  -Dhttps.protocols=TLSv1.2
mvn edu.utdallas:prf-maven-plugin:run -Dhttps.protocols=TLSv1.2
```

After invoking the commands andseeing (green) `BUILD SUCCESS` message due to the
last command, you will be able to review the fix results.

**Step 2:**  Fix report for the current version of PRF appears directly on your
screen right above the `BUILD SUCCESS` message. However, you can always redirect
the output of the tool to a file, if you desire so. The fix report for Chart-1 looks
something like the following on my computer.

```text
=====================================
    PRF Fix Report
=====================================
1.
File: .../prf/examples/Chart_1_buggy/source/org/jfree/chart/renderer/category/AbstractCategoryItemRenderer.java
Patch:
1797c1797
<         if (dataset == null) {
---
>         if (dataset != null) {
---------------------------------------------
``` 

### Example 2: Math-65 of Defects4J
Apache Commons Math is another Maven-based real-world Java code base
with 106 bugs in Defects4J bug database; CapGen is able to fix 13 of them.
Math-65 is one bugs fixable by CapGen, and it is due to its size that we
included it among our examples. The steps needed to invoke PRF on this bug
is more or less the same as in Example 1.

You will need to navigate to the bug project folder, adjust the POM file, and
invoke PRF.
```shell script
cd examples
cd Math_65_buggy
# Please make sure you have configured the POM file as described in the previous example
mvn clean test -DskipTests  -Dhttps.protocols=TLSv1.2
mvn edu.utdallas:prf-maven-plugin:run -Dhttps.protocols=TLSv1.2
```
After seeing (green) `BUILD SUCCESS` message, due to the last command,
you will be able to review the fix report that appears above the 
`BUILD SUCCESS` message. In my case, the fix report looks like the following.

```text
=====================================
    PRF Fix Report
=====================================
1.
File: .../prf/examples/Math_65_buggy/src/main/java/org/apache/commons/math/optimization/general/AbstractLeastSquaresOptimizer.java
Patch:
258c258
<             chiSquare += ((residualsWeights[i]) * residual) * residual;
---
>             chiSquare += residual * residual / residualsWeights[i];
---------------------------------------------
```

### YouTube Demo Video
You can watch our demo [YouTube video](https://bit.ly/3ehduSS) showing how you can
setup PRF and apply it on one of the example projects.

## Developing PRF Plugins
Currently, PRF is extendible via two kinds of plugins, namely patch generation plugin
and patch prioritization plugin. Developers of APR prototypes, can construct their own
patch generation/prioritization plugin by implementing the two interfaces provided by
the PRF framework. In this section, we only explain how the developers can make their
own patch generation plugin; making a custom patch prioritization plugin is similar,
and we omit the details.

The first step is to import the companion library of PRF into the plugin's project.
After [installing](#prf-setup) the library will be available at the Maven local repo.
Thus, if the developer uses Maven to make their plugin, they can use the following 
XML snippet to import the library into their project.

```xml
<dependencies>
    <!-- ... -->
    <dependency>
        <groupId>edu.utdallas</groupId>
        <artifactId>prf-core</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
</dependencies>
``` 

In case the developer uses Gradle as their build system, they can use the following
command in their build script (provided that Gradle is configured to look into the
correct repository).

```groovy
implementation 'edu.utdallas:prf-core:1.0-SNAPSHOT'
``` 

After importing the library, implementing the interface `PatchGenerationPlugin` is
all the developer needs to do. Implementing the interface is more or less
application-specific and we cannot say much about it here. One thing that we want to
mention at this point is to defining the name of the plugin. Any class implementing
`PatchGenerationPlugin` has to implement a method named `name`. This method receives
void and returns a `String`. The string value is intended to be the name of the plugin.
This value is compared (in a case-insensitive) manner with the name provided in
`<patchGenerationPlugin>` in the target POM file. Another method with the same signature
is `description`. This method is intended to return a short description of the plugin.
This description is to be displayed on the screen by PRF. We have documented the intent
of the classes and method using JavaDoc, and we suggest reviewing the documentation
in the code.

## System Requirements
PRF is a pure Java program, so it is platform independent. Be that as it
may, the example programs that we use, i.e. Defects4J, and the CapGen patch
generation plugin have certain peculiarities that call for a specific versions
of JDK or Maven. Here is the system configuration you might want to have in
order to be able to apply PRF with CapGen plugin on example Defects4J bugs.
 
* JDK: Oracle Java SE Development Kit 7u80 (recommended for Defects4J) and
JDK 8u171 or higher needed for running the core part of CapGen patch generation plugin.  
* Build System: Maven 3+
* Version Control System: Git 2.17.1+
* OS: Ubuntu Linux or Mac OS X
* RAM: 16+ GB
* Disk Space: 200+ MB free space


***

:one: J. Sohn and S. Yoo, "FLUCCS: using code and change metrics to improve fault localization," in ISSTA'17.

