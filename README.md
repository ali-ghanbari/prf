# PRF: A Framework for Building Automatic Program Repair Prototypes for JVM-based Language

## Table of Contents
- [Introduction](#introduction)
- [PRF Setup](#prf-setup)
- [PRF Demonstration](#prf-demonstration)
    * [Example 1: Chart-1 of Defects4J](#example-1-chart-1-of-defects4j)
    * [Example 2: Math-65 of Defects4J](#example-2-math-65-of-defects4j)
    * [Other Buggy Projects](#other-buggy-projects)
    * [YouTube Demo Video](#youtube-demo-video)
- [Developing PRF Plugins](#developing-prf-plugins)
    * [Developing Patch Generation Plugin](#developing-patch-generation-pluging)
    * [Developing Patch Prioritization/Filtering Plugin](#developing-patch-prioritization-filtering-pluging) 
- [PRF Reports](#prf-reports)
    * [Ranked Patches](#ranked-patches)
    * [Other Artifacts](#other-artifacts)
- [System Requirements](#system-requirements)
- [Empirical Analysis](#empirical-analysis)

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

Please see the examples in the section that follows to learn how you can
use PRF Maven plugin.  

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
mvn edu.utdallas:objsim:validate -Dhttps.protocols=TLSv1.2
```
After seeing (green) `BUILD SUCCESS` message, due to the last command,
you will be able to review the fix report that appears above the 
`BUILD SUCCESS` message. In my case, the fix report looks like the following.

```text

```

### Other Buggy Projects
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

<!-- ****************************** GENERIC OPTIONS ******************************* -->

        <!-- <whiteListPrefix>${project.groupId}</whiteListPrefix>                  -->
        <!-- <targetTests>                                                          -->
        <!--    <targetTest>{whiteListPrefix}.*Test</targetTest>                    -->
        <!--    <targetTest>{whiteListPrefix}.*Tests</targetTest>                   -->
        <!-- </targetTests>                                                         -->
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
Some of the tags under `<congigutation>` are optional; we have shown with their
default values them in the form of comments. We stress that the XML snippet
shown above is just a template and the users will need to specialize values and
get rid of those ellipsis marks.

For example, the only failing test for Defects4J bug Chart-12 is as follows.
```xml
<failingTests>
    <failingTest>org.jfree.chart.plot.junit.MultiplePiePlotTests::testConstructor</failingTest>
</failingTests>
``` 
Please note that you may use `:` or simply `.` instead of `::` as the separator
between the test class name and method name.

By default, the system looks for a CSV file named `input-file.csv` under the
project's base directory. The file generated by our tool `input-file-generator`
, described shortly, shall also generate the required CSV file named as such.
Nevertheless, you may use a different file as the CSV file by specifying its
relative/absolute patch and name under `<inputCSVFile>` tag.

ObjSim uses groupId of the target project to identify the application classes
of the target project during instrumentation. This default action can be customized
via the tag `<whiteListPrefix>`. The user can write their desired prefix of classes
to be instrumented. Please note that ObjSim expects that `<whiteListPrefix>`
includes all the patched method mentioned in the input CSV file. 

Last but not least, since test cases of some large and complex projects might
be demanding, we have provided the user with a mechanism through which they
can specify the maximum amount of allotted heap space (and/or permanent
generation space, if applicable) for the child _profiler_ processes. Profiler
processes, as described in the paper, are responsible for executing test cases
against the original and patched program and collect information about the
system state right at the exit point(s) of the specified patched method.

In the rest of this section, we discuss the format of input CSV file expected
by ObjSim Maven plugin and the way you can automatically generate the file
for a Maven project on which PraPR is already applied.

As we have already discussed in the paper, the input CSV file is expected
to consist of the five columns as follows.
```text
Id, Susp, Method, Class-File, Covering-Tests
```
Each row of this file corresponds to a patch, wherein `Id` is an integer
identifier for the patch, `Susp` is the suspiciousness value of the patched
location corresponding to the patch, `Method` is fully qualified name and
signature of the patched method, `Class-File` is the path to the patched
class file (if you are using a source code-level APR, you can compile the
patched class file to get the desired `.class` file), and `Covering-Tests`
denotes the space-separated list of fully qualified test case names covering
the patched location corresponding to the patch. Please note that one can
list all the test cases of the program here in case covering test cases
are not available. We stress that using covering test cases is preferable
only due to performance considerations during profiling.

As for the patches generated by PraPR, the script `input-file-generator`
that we have included in this repository, generated the information described
above in the following way. `Id` corresponds to the patch number located in
LOG fix report generated by PraPR (LOG fix reports of PraPR are stored in
`target/prapr-reports/*/fix-report.log`). The values of `Susp` and `Method`
are obtained from the XML report generated by PraPR (compressed XML report
of PraPR are stored in `target/prapr-reports/*/mutations.xml.gz`). It is
worth noting that suspiciousness values computed by PraPR are based on
well-known Ochiai formula, but any other suspiciousness formula can also
be used. `Class-File` is intended to point to the class file corresponding
to the patch which in case of PraPR is stored with the name `mutant-*.class`
under the directory `target/prapr-reports/*/pool/`. Lastly, the script
extracts the test cases covering patched location corresponding to each patch
from the XML report of PraPR.

With that said, in order for our script find the required information, PraPR
should be configure to produce both LOG and XML file as well as the flag
`verboseReport` should be set to true so that the tool dumps all the class
files in the `pool` folder.

After running PraPR with the aforementioned configurations, if the tool is
able to find more than one plausible patch, you can follow the steps
instructed below to produce the desired input CSV file.

**Step 0:** Please make sure the environment variable `JAVA_HOME` points
to the home directory of JDK 1.8. Please also make sure that there are
one or more patches listed in the LOG report in
`target/prapr-reports/*/fix-report.log`, the XML report
`target/prapr-reports/*/mutations.xml.gz`
exists, and the directory `target/prapr-reports/*/pool/` contains one
or more `.class` files.

**Step 1:** Assuming that ObjSim's base directory is in system `PATH`,
you may use the following command to generate the desired `input-file.csv`.
```shell script
input-file-generator target/prapr-reports/*/
```
After running the tool, you can find the file `input-file.csv` stored
in the base directory of the target project.

### YouTube Demo Video
You can watch our demo [YouTube video](https://bit.ly/2K8gnYV) showing how you can
setup ObjSim and apply it on one of the example projects.

## ObjSim Reports
Once ObjSim Maven plugin runs, it prints out the name of the test cases
being executed during profiling, exception traces of the failing tests,
and some other information that helped us to better understand its
runtime behavior and track and fix its bugs. In this section, we describe
the information produced by this tool.

All the information produced by ObjSim is stored inside the directory
`objsim-output` located under the base directory of the target project.

### Ranked Patches
ObjSim does not print the sorted list of patches on the screen. Instead it
dumps the list inside the file `objsim-output/ranking.txt` This files is
merely a list of positive integers each in a new line. Each number identifies
a patch, and these identifiers are consistent with the patch numbers used
in the LOG report generated by PraPR.

### Other Artifacts
Corresponding to each patch a directory shall be created under
`objsim-output/`. These directories shall be named as `patch-n` where `n`
if the identifier of the patch consistent with the ones in `rankings.txt`
and those in LOG report generated by PraPR.

Three files shall be created under `objsim-output/patch-n/`: (1) compressed
file `original.gz`; (2) compressed file `patched.gz`; (3) CSV file containing
raw distances between original and patched versions of the program. The two
compressed files contain serialized form of the system state at the exit
point(s) of the patched method (plus additional information about the test
cases and the status of the test cases that cover the method). These files
are not directly usable by the users and are intended to be used in the
future versions of the tool and/or statistical study that we are currently
conducting on these data. 

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

## Empirical Analysis
The following table shows the details of our experiments with 55 bugs from
[Defects4J v1.4.0](https://github.com/Greg4cr/defects4j/tree/additional-faults-1.4)
bug database. These bugs are the ones that [PraPR](https://github.com/prapr/prapr)
is able to produce plausible patches (patches that simply pass all the test cases)
and at least one genuine fix (plausible patches that correctly fix the bug).
The goal is to show the effectiveness of ObjSim in prioritizing genuine fixes above
the plausible, but overfitted, ones. 

| Bug Id|# Plausible Patches|# Genuine Fixes|Rank Before|Rank After |
|:---:  |:---:              | :---:           | :---:     | :---:     |
| Chart-1|2|1|1|1 |
| Chart-8|2|2|2|2 |
| Chart-11|2|1|1|1 |
| **Chart-12**|**2**|**1**|**2**|**1** |
| Chart-20|1|1|1|1 |
| Chart-24|2|1|1|1 |
| Chart-26|100|1|17|Time-out |
| Closure-10|4|1|1|4 |
| Closure-11|15|3|1|Time-out |
| Closure-14|1|1|1|1 |
| Closure-18|1|1|1|1 |
| Closure-31|9|1|6|Time-out |
| Closure-46|7|2|1|4 |
| Closure-62 <sup>:one:</sup>|1|1|1|1 |
| Closure-63 <sup>:one:</sup>|1|1|1|1 |
| Closure-70|1|1|1|1 |
| Closure-73|1|1|1|1 |
| Closure-86|3|2|1|1 |
| Closure-92 <sup>:two:</sup>|4|1|1|4 |
| Closure-93 <sup>:two:</sup>|4|1|1|4 |
| Closure-126|12|2|5|Time-out |
| CommonsCli-4|6|1|6|6 |
| CommonsCli-22|1|1|1|1 |
| CommonsCli-23|2|2|1|1 |
| CommonsCodec-13|1|1|1|1 |
| CommonsCsv-5|1|1|1|1 |
| **CommonsJXPath-1**|**4**|**1**|**4**|**1** |
| JacksonDatabind-8|1|1|1|1 |
| **JacksonDatabind-34**|**2**|**1**|**2**|**1** |
| JacksonDatabind-36|1|1|1|1 |
| **JacksonDatabind-39**|**7**|**1**|**5**|**1** |
| Jsoup-3|1|1|1|1 |
| Jsoup-42|13|1|1|Time-out |
| Lang-6|1|1|1|1 |
| Lang-10|7|2|2|Time-out |
| Lang-26|1|1|1|1 |
| Lang-33|1|1|1|1 |
| **Lang-57**|**3**|**1**|**3**|**1** |
| Lang-59|2|1|2|2 |
| Math-5|3|1|1|1 |
| Math-33|1|1|1|1 |
| Math-34|1|1|1|1 |
| Math-50|30|1|30|30 |
| Math-58|2|1|2|2 |
| Math-59|1|1|1|1 |
| Math-70|1|1|1|1 |
| Math-75|1|1|1|1 |
| **Math-82**|**9**|**1**|**9**|**1** |
| Math-85|4|1|4|4 |
| Mockito-5|31|1|31|31 |
| **Mockito-29**|**2**|**1**|**2**|**1** |
| Mockito-38|3|1|2|2 |
| **Time-4**|**5**|**1**|**5**|**3** |
| Time-11|32|1|1|1 |
| **Time-19**|**2**|**1**|**2**|**1** |

:one:, :two:: Bugs [Closure-62](https://github.com/Greg4cr/defects4j/blob/additional-faults-1.4/framework/projects/Closure/patches/62.src.patch) and [Closure-63](https://github.com/Greg4cr/defects4j/blob/additional-faults-1.4/framework/projects/Closure/patches/63.src.patch), as well as [Closure-92](https://github.com/Greg4cr/defects4j/blob/additional-faults-1.4/framework/projects/Closure/patches/92.src.patch) and [Closure-93](https://github.com/Greg4cr/defects4j/blob/additional-faults-1.4/framework/projects/Closure/patches/93.src.patch), are duplicated in Defects4J database. We counted each pair once in our analyses.

In this table the column "Bug Id" represents the bug identifier consistent with that of
Defects4J database. The columns "# Plausible Patches", and "# Genuine Fixes" list the number
of plausible patches and genuine fixes, resp., generated by PraPR. The column "Rank Before"
reports the _best rank_ of genuine fix, among other plausible patches, for each bug as
prioritized by the default ranking scheme of PraPR. By best rank, we mean the rank of the
first genuine fix that we find in the list of plausible patches. Lastly, the column "Rank After" 
reports the best rank of genuine fix for each bug as prioritized by ObjSim. Please note that
duplicated bug pairs have been treated as identical in our analyses.

We conducted our experiments on a commodity Dell Optiplex 7020 PC with Intel Core i5 3.30GHz
CPU, 16 GB of RAM, and running Ubuntu 16.04.6 LTS. The current version of ObjSim uses only
one core of CPU at a time. We put a cap limit on the time and the amount of heap space that
the tool was allowed to allocate. We ran ObjSim only for 5 minutes per bug and set the maximum
heap size of the JVM instances to 16 GB. During 5 minutes period for each bug, the heap space
for none of the bugs raised above 9 GB and only 6 of the bugs failed to get fully processed
by ObjSim within the time budget.

Despite such a tough limit, ObjSim has been able to prioritize 5 more genuine fixes in top-1
position. This is more than 16% improvement in the number of genuine fixes ranked in top-1
position. Please note that for 3 of the bugs, ObjSim degrades the rank of genuine fix, and
that is because the genuine fix in those bugs happened to be involved in radical changes in
control flow of the program, and hence in the objects that it computes, e.g., deleting a block
of code (which redirects control flow to a method with entirely different body) and calling
different methods with entirely different behavior, etc. As per the previous empirical studies
<sup>:three:,:four:,:five:</sup>, most of such changes are unlikely to be genuine fixes for the
bugs.

In summary, ObjSim increases the number of genuine fixes ranked in top-1 position from 30
to 35 (a 16.67% improvement), and it reduces the average rank of genuine fixes from 3.04 to
2.74 (an almost 10% improvement).

We are actively working on applying ObjSim on patches generated by other APR systems,
including but not limited to ASTOR implementations, CapGen, NOPOL, and Sequencer. 
We believe such an efficient and effective method for patch prioritization will contribute
significantly for bringing APR systems to everyday programming.

***

:three: Tan, et al., "Anti-patterns in search-based program repair," in FSE'16.

:four: Wen, et al., "Context-Aware Patch Generation for Better Automated Program Repair," in ICSE'18.

:five: Le, et al., "History Driven Program Repair," in SANER'16.
