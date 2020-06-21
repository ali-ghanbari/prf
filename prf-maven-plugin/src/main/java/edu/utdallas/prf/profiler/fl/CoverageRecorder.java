package edu.utdallas.prf.profiler.fl;

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

import edu.utdallas.prf.Class;
import edu.utdallas.prf.ClassLevelCoverage;
import edu.utdallas.prf.ClassLevelFL;
import edu.utdallas.prf.LineLevelCoverage;
import edu.utdallas.prf.LineLevelFL;
import edu.utdallas.prf.Method;
import edu.utdallas.prf.MethodLevelCoverage;
import edu.utdallas.prf.MethodLevelFL;
import edu.utdallas.prf.SourceLine;
import edu.utdallas.prf.commons.misc.PropertyUtils;
import edu.utdallas.prf.commons.relational.StringDomain;
import edu.utdallas.prf.profiler.ProfilerReporter;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class CoverageRecorder {
    private static final int UNIT_SIZE;

    private static final byte[] BYTE_UNIT;

    private static final long[] LONG_UNIT;

    private static final byte[][] UNIT_SIZED_2D_BYTE_ARRAY;

    private static final Map<String, Object> CLASS_COV;

    private static final Map<String, Object> METHOD_COV;

    private static final Map<String, Object> LINE_COV;

    private static FLOptions options;

    private static byte[][] currentLineCovUnits;

    private static byte[] currentByteUnit;

    private static long[] currentLongUnit;

    static {
        UNIT_SIZE = PropertyUtils.getIntProperty("prf.def.unit.sz", 1024);
        BYTE_UNIT = new byte[UNIT_SIZE];
        LONG_UNIT = new long[UNIT_SIZE];
        UNIT_SIZED_2D_BYTE_ARRAY = new byte[UNIT_SIZE][];
        CLASS_COV = new HashMap<>();
        METHOD_COV = new HashMap<>();
        LINE_COV = new HashMap<>();
    }

    private CoverageRecorder() { }

    public static void setFLOptions(final FLOptions options) {
        CoverageRecorder.options = options;
    }

    public static void setCurrentTest(final String testName) {
        switch (CoverageRecorder.options) {
            case CLASS_LEVEL:
                currentByteUnit = BYTE_UNIT.clone();
                CLASS_COV.put(testName, currentByteUnit);
                break;
            case METHOD_LEVEL:
                currentLongUnit = LONG_UNIT.clone();
                METHOD_COV.put(testName, currentLongUnit);
                break;
            case LINE_LEVEL:
                currentLineCovUnits = UNIT_SIZED_2D_BYTE_ARRAY.clone();
                LINE_COV.put(testName, currentLineCovUnits);
        }
    }

    // this is called only when class level FL is active
    public static void markClass(final int classIndex) {
        currentByteUnit[classIndex / Byte.SIZE] |= 1 << (classIndex % Byte.SIZE);
    }

    // this is called only when method level FL is active
    public static void markMethod(final int methodIndex) {
        currentLongUnit[methodIndex / Long.SIZE] |= 1 << (methodIndex % Long.SIZE);
    }

    // this is called only when line level FL is active
    public static void markSourceLine(final int sourceFileIndex, final int lineNumber) {
        byte[] unit = currentLineCovUnits[sourceFileIndex];
        if (unit == null) {
            unit = BYTE_UNIT.clone();
            currentLineCovUnits[sourceFileIndex] = unit;
        }
        unit[lineNumber / Byte.SIZE] |= 1 << (lineNumber % Byte.SIZE);
    }

    private static ClassLevelFL makeClassLevelFLInfo(final StringDomain classesDom,
                                                     final Collection<String> failingTestNames,
                                                     final FLStrategy flStrategy) {
        final Map<String, CoverageInfo> coverageInfoMap = new HashMap<>();
        final Set<String> failingTests = new HashSet<>(failingTestNames);
        for (Map.Entry<String, Object> entry : CLASS_COV.entrySet()) {
            final String testName = entry.getKey();
            final boolean isFailing = failingTests.contains(testName);
            final byte[] unit = (byte[]) entry.getValue();
            int classIndex = 0;
            for (int chunk : unit) {
                chunk = chunk & 0b11111111;
                if (chunk == 0) {
                    classIndex += Byte.SIZE;
                } else {
                    while (chunk != 0) {
                        if ((chunk & 1) != 0) {
                            final String className = classesDom.get(classIndex);
                            CoverageInfo info = coverageInfoMap.get(className);
                            if (info == null) {
                                info = new CoverageInfo();
                                coverageInfoMap.put(className, info);
                            }
                            (isFailing ? info.ef : info.ep).add(testName);
                        }
                        chunk >>>= 1;
                        classIndex++;
                    }
                }
            }
        }
        final ClassLevelFL result = new ClassLevelFL();
        final Set<String> allPassing = new HashSet<>(CLASS_COV.keySet());
        allPassing.removeAll(failingTests);
        for (final Map.Entry<String, CoverageInfo> entry : coverageInfoMap.entrySet()) {
            final String className = entry.getKey();
            final int ep = entry.getValue().ep.size();
            final int ef = entry.getValue().ef.size();
            final int np = allPassing.size() - ep;
            final int nf = failingTests.size() - ef;
            result.put(new Class(className), flStrategy.computeSusp(ef, ep, nf, np));
        }
        return result;
    }

    private static ClassLevelCoverage makeClassLevelCoverageInfo(final StringDomain classesDom) {
        final ClassLevelCoverage res = new ClassLevelCoverage();
        for (Map.Entry<String, Object> entry : CLASS_COV.entrySet()) {
            final String testName = entry.getKey();
            final Set<Class> coveredClasses = new HashSet<>();
            res.put(testName, coveredClasses);
            final byte[] unit = (byte[]) entry.getValue();
            int classIndex = 0;
            for (int chunk : unit) {
                chunk = chunk & 0b11111111;
                if (chunk == 0) {
                    classIndex += Byte.SIZE;
                } else {
                    while (chunk != 0) {
                        if ((chunk & 1) != 0) {
                            final String className = classesDom.get(classIndex);
                            coveredClasses.add(new Class(className));
                        }
                        chunk >>>= 1;
                        classIndex++;
                    }
                }
            }
        }
        return res;
    }

    private static MethodLevelFL makeMethodLevelFLInfo(final StringDomain methodsDom,
                                                       final Collection<String> failingTestNames,
                                                       final FLStrategy flStrategy) {
        final Map<String, CoverageInfo> coverageInfoMap = new HashMap<>();
        final Set<String> failingTests = new HashSet<>(failingTestNames);
        for (Map.Entry<String, Object> entry : METHOD_COV.entrySet()) {
            final String testName = entry.getKey();
            final boolean isFailing = failingTests.contains(testName);
            final long[] unit = (long[]) entry.getValue();
            int methodIndex = 0;
            for (long chunk : unit) {
                if (chunk == 0) {
                    methodIndex += Long.SIZE;
                } else {
                    while (chunk != 0L) {
                        if ((chunk & 1L) != 0L) {
                            final String methodsName = methodsDom.get(methodIndex);
                            CoverageInfo info = coverageInfoMap.get(methodsName);
                            if (info == null) {
                                info = new CoverageInfo();
                                coverageInfoMap.put(methodsName, info);
                            }
                            (isFailing ? info.ef : info.ep).add(testName);
                        }
                        chunk >>>= 1;
                        methodIndex++;
                    }
                }
            }
        }
        final MethodLevelFL result = new MethodLevelFL();
        final Set<String> allPassing = new HashSet<>(METHOD_COV.keySet());
        allPassing.removeAll(failingTests);
        for (final Map.Entry<String, CoverageInfo> entry : coverageInfoMap.entrySet()) {
            final String methodName = entry.getKey();
            final int ep = entry.getValue().ep.size();
            final int ef = entry.getValue().ef.size();
            final int np = allPassing.size() - ep;
            final int nf = failingTests.size() - ef;
            result.put(new Method(methodName), flStrategy.computeSusp(ef, ep, nf, np));
        }
        return result;
    }

    private static MethodLevelCoverage makeMethodLevelCoverageInfo(final StringDomain methodsDom) {
        final MethodLevelCoverage res = new MethodLevelCoverage();
        for (Map.Entry<String, Object> entry : METHOD_COV.entrySet()) {
            final String testName = entry.getKey();
            final Set<Method> coveredMethods = new HashSet<>();
            res.put(testName, coveredMethods);
            final long[] unit = (long[]) entry.getValue();
            int methodIndex = 0;
            for (long chunk : unit) {
                if (chunk == 0) {
                    methodIndex += Long.SIZE;
                } else {
                    while (chunk != 0L) {
                        if ((chunk & 1L) != 0L) {
                            final String methodsName = methodsDom.get(methodIndex);
                            coveredMethods.add(new Method(methodsName));
                        }
                        chunk >>>= 1;
                        methodIndex++;
                    }
                }
            }
        }
       return res;
    }

    private static LineLevelFL makeLineLevelFLInfo(final StringDomain filesDom,
                                                   final Collection<String> failingTestNames,
                                                   final FLStrategy flStrategy) {
        final Map<SourceLine, CoverageInfo> coverageInfoMap = new HashMap<>();
        final Set<String> failingTests = new HashSet<>(failingTestNames);
        for (Map.Entry<String, Object> entry : LINE_COV.entrySet()) {
            final String testName = entry.getKey();
            final boolean isFailing = failingTests.contains(testName);
            final byte[][] units = (byte[][]) entry.getValue();
            for (int sourceFileIndex = 0; sourceFileIndex < units.length; sourceFileIndex++) {
                final byte[] unit = units[sourceFileIndex];
                if (unit == null) {
                    continue;
                }
                final String sourceFileName = filesDom.get(sourceFileIndex);
                int lineNumber = 0;
                for (int chunk : unit) {
                    chunk = chunk & 0b11111111;
                    if (chunk == 0) {
                        lineNumber += Byte.SIZE;
                    } else {
                        while (chunk != 0) {
                            if ((chunk & 1) != 0) {
                                final SourceLine sourceLine = new SourceLine(sourceFileName, lineNumber);
                                CoverageInfo info = coverageInfoMap.get(sourceLine);
                                if (info == null) {
                                    info = new CoverageInfo();
                                    coverageInfoMap.put(sourceLine, info);
                                }
                                (isFailing ? info.ef : info.ep).add(testName);
                            }
                            chunk >>>= 1;
                            lineNumber++;
                        }
                    }
                }
            }
        }
        final LineLevelFL result = new LineLevelFL();
        final Set<String> allPassing = new HashSet<>(LINE_COV.keySet());
        allPassing.removeAll(failingTests);
        for (final Map.Entry<SourceLine, CoverageInfo> entry : coverageInfoMap.entrySet()) {
            final SourceLine sourceLine = entry.getKey();
            final int ep = entry.getValue().ep.size();
            final int ef = entry.getValue().ef.size();
            final int np = allPassing.size() - ep;
            final int nf = failingTests.size() - ef;
            result.put(sourceLine, flStrategy.computeSusp(ef, ep, nf, np));
        }
        return result;
    }

    private static LineLevelCoverage makeLineLevelCoverageInfo(final StringDomain filesDom) {
        final LineLevelCoverage res = new LineLevelCoverage();
        for (Map.Entry<String, Object> entry : LINE_COV.entrySet()) {
            final String testName = entry.getKey();
            final Set<SourceLine> coveredLines = new HashSet<>();
            res.put(testName, coveredLines);
            final byte[][] units = (byte[][]) entry.getValue();
            for (int sourceFileIndex = 0; sourceFileIndex < units.length; sourceFileIndex++) {
                final byte[] unit = units[sourceFileIndex];
                if (unit == null) {
                    continue;
                }
                final String sourceFileName = filesDom.get(sourceFileIndex);
                int lineNumber = 0;
                for (int chunk : unit) {
                    chunk = chunk & 0b11111111;
                    if (chunk == 0) {
                        lineNumber += Byte.SIZE;
                    } else {
                        while (chunk != 0) {
                            if ((chunk & 1) != 0) {
                                final SourceLine sourceLine = new SourceLine(sourceFileName, lineNumber);
                                coveredLines.add(sourceLine);
                            }
                            chunk >>>= 1;
                            lineNumber++;
                        }
                    }
                }
            }
        }
        return res;
    }

    static class CoverageInfo {
        final Set<String> ep = new HashSet<>();
        final Set<String> ef = new HashSet<>();
    }

    public static void reportCoverageInfo(final ProfilerReporter reporter,
                                          final StringDomain domain) {
        switch (CoverageRecorder.options) {
            case CLASS_LEVEL:
                reporter.reportClassLevelCov(makeClassLevelCoverageInfo(domain));
                break;
            case METHOD_LEVEL:
                reporter.reportMethodLevelCov(makeMethodLevelCoverageInfo(domain));
                break;
            case LINE_LEVEL:
                reporter.reportLineLevelCov(makeLineLevelCoverageInfo(domain));
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    public static void reportFLInfo(final ProfilerReporter reporter,
                                    final StringDomain domain,
                                    final Collection<String> failingTestNames,
                                    final FLStrategy flStrategy) {
        switch (CoverageRecorder.options) {
            case CLASS_LEVEL:
                reporter.reportClassLevelFL(makeClassLevelFLInfo(domain, failingTestNames, flStrategy));
                break;
            case METHOD_LEVEL:
                reporter.reportMethodLevelFL(makeMethodLevelFLInfo(domain, failingTestNames, flStrategy));
                break;
            case LINE_LEVEL:
                reporter.reportLineLevelFL(makeLineLevelFLInfo(domain, failingTestNames, flStrategy));
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }
}
