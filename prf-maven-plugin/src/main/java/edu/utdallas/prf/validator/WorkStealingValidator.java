package edu.utdallas.prf.validator;

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

import edu.utdallas.prf.Patch;
import edu.utdallas.prf.PraPRTestComparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

import edu.utdallas.prf.validator.process.ValidationOutcome;
import edu.utdallas.prf.validator.process.Validator;
import org.apache.commons.lang3.Validate;
import org.pitest.process.ProcessArgs;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class WorkStealingValidator extends RecursiveAction implements PatchValidator {
    private final ProcessArgs defaultProcessArgs;

    private final String whiteListPrefix;

    private final Collection<String> testClassNames;

    private final PraPRTestComparator testComparator;

    private final long timeoutConstant;

    private final double timeoutPercent;

    private final List<Patch> patches;

    private final int low;

    private final int high;

    private long timeElapsed;

    private int parallelism;

    private final Map<Patch, ValidationOutcome> validationStatusMap;

    private WorkStealingValidator(final ProcessArgs defaultProcessArgs,
                                  final String whiteListPrefix,
                                  final Collection<String> testClassNames,
                                  final PraPRTestComparator testComparator,
                                  final long timeoutConstant,
                                  final double timeoutPercent,
                                  final List<Patch> patches,
                                  final int low,
                                  final int high,
                                  final Map<Patch, ValidationOutcome> validationStatusMap) {
        Validate.isTrue(low <= high);
        this.defaultProcessArgs = defaultProcessArgs;
        this.whiteListPrefix = whiteListPrefix;
        this.testClassNames = testClassNames;
        this.testComparator = testComparator;
        this.timeoutConstant = timeoutConstant;
        this.timeoutPercent = timeoutPercent;
        this.patches = patches;
        this.low = low;
        this.high = high;
        this.validationStatusMap = validationStatusMap;
    }

    public WorkStealingValidator(final ProcessArgs defaultProcessArgs,
                                 final String whiteListPrefix,
                                 final Collection<String> testClassNames,
                                 final PraPRTestComparator testComparator,
                                 final long timeoutConstant,
                                 final double timeoutPercent,
                                 final Collection<Patch> patches,
                                 final Map<Patch, ValidationOutcome> validationStatusMap) {
        this(defaultProcessArgs, whiteListPrefix, testClassNames, testComparator, timeoutConstant, timeoutPercent, new ArrayList<>(patches), 0, patches.size() - 1, validationStatusMap);
    }

    private void validate(final Patch patch) {
        final ValidationOutcome outcome = Validator.runValidator(this.defaultProcessArgs,
                this.whiteListPrefix,
                this.testClassNames,
                this.testComparator,
                this.timeoutConstant,
                this.timeoutPercent,
                patch);
        this.validationStatusMap.put(patch, outcome);
    }

    @Override
    protected void compute() {
        final int low = this.low;
        final int high = this.high;
        if (low == high) {
            validate(this.patches.get(low));
        } else {
            final int mid = (low + high) / 2;
            invokeAll(duplicate(low, mid), duplicate(mid + 1, high));
        }
    }

    private WorkStealingValidator duplicate(final int low, final int high) {
        return new WorkStealingValidator(this.defaultProcessArgs, this.whiteListPrefix, this.testClassNames, this.testComparator, this.timeoutConstant, this.timeoutPercent, this.patches, low, high, this.validationStatusMap);
    }

    @Override
    public void run(int parallelism) {
        final ForkJoinPool pool;
        if (parallelism <= 0) {
            pool = new ForkJoinPool();
        } else {
            pool = new ForkJoinPool(parallelism);
        }
        this.parallelism = pool.getParallelism();
        final long start = System.currentTimeMillis();
        pool.invoke(this);
        this.timeElapsed = System.currentTimeMillis() - start;
    }

    @Override
    public long getTimeElapsed() {
        return this.timeElapsed;
    }

    @Override
    public int getDegreeOfParallelism() {
        return this.parallelism;
    }
}
