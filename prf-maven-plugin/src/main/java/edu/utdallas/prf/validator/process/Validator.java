package edu.utdallas.prf.validator.process;

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
import edu.utdallas.prf.commons.junit.runner.JUnitRunner;
import edu.utdallas.prf.commons.junit.runner.TestExecutionStatus;
import edu.utdallas.prf.commons.misc.PropertyUtils;
import org.pitest.classinfo.CachingByteArraySource;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.mutationtest.execute.MemoryWatchdog;
import org.pitest.process.ProcessArgs;
import org.pitest.util.ExitCode;
import org.pitest.util.IsolationUtils;
import org.pitest.util.SafeDataInputStream;

import javax.management.Notification;
import javax.management.NotificationListener;
import java.lang.management.MemoryNotificationInfo;
import java.net.Socket;
import java.util.Collection;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class Validator {
    private static final int CACHE_SIZE;

    static {
        CACHE_SIZE = PropertyUtils.getIntProperty("prf.def.cache.size", 200);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Validator is HERE!");
        final int port = Integer.parseInt(args[0]);
        try (Socket socket = new Socket("localhost", port)) {
            final SafeDataInputStream dis = new SafeDataInputStream(socket.getInputStream());

            final ValidatorArguments arguments = dis.read(ValidatorArguments.class);

            final ValidatorReporter reporter = new ValidatorReporter(socket.getOutputStream());

            addMemoryWatchDog(reporter);

            ClassByteArraySource byteArraySource = new ClassloaderByteArraySource(IsolationUtils.getContextClassLoader());
            byteArraySource = new CachingByteArraySource(byteArraySource, CACHE_SIZE);

            final ClassLoader loader = new SelectiveClassLoader(byteArraySource, arguments.whiteListPrefix, arguments.patch);

            final JUnitRunner runner = new JUnitRunner(loader, arguments.testClassNames, arguments.testComparator, true);
            final TestExecutionStatus status = runner.run(loader, arguments.patch.getCoveringTestsFilter(),
                    arguments.testComparator.getTestsTiming(),
                    arguments.timeoutConstant,
                    arguments.timeoutPercent);

            ValidationOutcome outcome = null;
            switch (status) {
                case OK:
                    outcome = ValidationOutcome.PLAUSIBLE;
                    break;
                case EARLY_EXIT:
                    outcome = ValidationOutcome.RUN_ERROR;
                    break;
                case TIMED_OUT:
                    outcome = ValidationOutcome.TIMED_OUT;
            }

            reporter.reportValidationOutcome(outcome);

            System.out.println("Validator is DONE!");
            reporter.done(ExitCode.OK);
        }
    }

    // credit: adopted from PIT source code
    private static void addMemoryWatchDog(final ValidatorReporter reporter) {
        final NotificationListener listener = new NotificationListener() {
            @Override
            public void handleNotification(final Notification notification,
                                           final Object handback) {
                final String type = notification.getType();
                if (type.equals(MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED)) {
                    reporter.reportValidationOutcome(ValidationOutcome.MEMORY_ERROR);
                    reporter.done(ExitCode.OUT_OF_MEMORY);
                }
            }
        };
        MemoryWatchdog.addWatchDogToAllPools(90, listener);
    }

    public static ValidationOutcome runValidator(final ProcessArgs defaultProcessArgs,
                                                 final String whiteListPrefix,
                                                 final Collection<String> testClassNames,
                                                 final PraPRTestComparator testComparator,
                                                 final long timeoutConstant,
                                                 final double timeoutPercent,
                                                 final Patch patch) {
        final ValidatorArguments arguments = new ValidatorArguments(whiteListPrefix, testClassNames, testComparator, timeoutConstant, timeoutPercent, patch);
        final ValidatorProcess process = new ValidatorProcess(defaultProcessArgs, arguments);
        try {
            process.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        process.waitToDie();
        return process.getValidatorOutcome();
    }
}
