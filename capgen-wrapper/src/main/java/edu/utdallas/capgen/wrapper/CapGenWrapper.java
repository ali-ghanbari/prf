package edu.utdallas.capgen.wrapper;

/*
 * #%L
 * capgen-wrapper
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

import org.capgen.main.AnalyzeRepairResults;
import org.capgen.main.CompileAndRunPatch;
import org.capgen.main.ExtractSemanticFeature;
import org.capgen.main.GetFaultLocationResults;
import org.capgen.main.Main;
import org.capgen.main.ObtainFixIngredients;
import org.capgen.main.PrioritizePatches;

import java.io.File;
import java.io.PrintWriter;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class CapGenWrapper {
    public static void main(String[] args) throws Exception {
        // args[0]: project
        // args[1]: bugId
        // args[2]: project base directory (absolute path)
        // args[3]: compatible JDK home directory (absolute path)
        final String project = args[0];
        final int bugId = Integer.parseInt(args[1]);
        final File baseDirectory = new File(args[2]);
        final File compatibleJDK = new File(args[3]);
        if (!baseDirectory.isDirectory()) {
            throw new IllegalArgumentException("illegal base directory");
        }
        if (!compatibleJDK.isDirectory()) {
            throw new IllegalArgumentException("illegal jdk home directory");
        }
        configureCapGen(baseDirectory.getAbsolutePath(),
                compatibleJDK.getAbsolutePath(),
                project,
                bugId);
        runCapGen();
    }

    private static void configureCapGen(final String projectBaseDirectory,
                                        final String compatibleJDKHome,
                                        final String project,
                                        final int bugId) throws Exception {
        final File configFile = new File("config_local.txt");
        configFile.deleteOnExit();
        try(final PrintWriter pw = new PrintWriter(configFile)) {
            pw.println("workLoc=" + projectBaseDirectory);
            pw.println("bugLoc = " + (new File(projectBaseDirectory)).getParentFile().getAbsolutePath());
            pw.println("JDK7 = " + compatibleJDKHome + "/bin/");
            pw.println("task = RepairABug");
            pw.println("project = " + project);
            pw.println("bid = " + bugId);
            pw.println("faultLocation = false");
            pw.println("ingredientsExtraction = false");
            pw.println("patchPrioritization = true");
            pw.println("patchValidation = true");
            pw.println("resultsAnalysis = true");
        }
        Main.loadConfigure(configFile.getAbsolutePath());
    }

    private static void runCapGen() throws Exception {
        if (Main.checkNecessaryFiles()) {
            if (Main.faultLocation) {
                System.out.println("== Start locating the faults");
                final GetFaultLocationResults fl = new GetFaultLocationResults();
                fl.getFaultLocationResults(Main.project, Main.bid);
                System.out.println("== Finish locating the faults");
            }
            if (Main.ingredientsExtraction) {
                System.out.println("== Start extracting the ingredients");
                final ObtainFixIngredients fixIngredients = new ObtainFixIngredients(Main.project, Main.bid);
                fixIngredients.obtainIngredients();
                final ExtractSemanticFeature semanticFeature = new ExtractSemanticFeature(Main.project, Main.bid);
                semanticFeature.generateSemanticFiles();
                System.out.println("== Finish extracting the ingredients");
            }
            if (Main.patchPrioritization) {
                System.out.println("== Start prioritizing the patches");
                final PrioritizePatches prioritizer = new PrioritizePatches(Main.project, Main.bid);
                prioritizer.setConfigurations(true, true, true, false, true);
                prioritizer.prioritizingIngredients();
                System.out.println("== Finish prioritizing the patches");
            }
            if (Main.patchValidation) {
                System.out.println("== Start validating the patches");
                final CompileAndRunPatch compiler = new PatchCompiler(Main.project, Main.bid);
                compiler.testRankedPatches();
                System.out.println("== Finish validating the patches");
            }
            if (Main.resultsAnalysis) {
                System.out.println("== Start analyzing the results");
                final AnalyzeRepairResults analyzer = new AnalyzeRepairResults(Main.project, Main.bid);
                analyzer.analyzeNewResults();
                System.out.println();
                System.out.println("== Finish analyzing the results");
            }
        }
    }
}
