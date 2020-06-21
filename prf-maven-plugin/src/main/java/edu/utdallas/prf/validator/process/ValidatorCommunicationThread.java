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

import edu.utdallas.prf.commons.process.ControlId;
import org.pitest.functional.SideEffect1;
import org.pitest.util.CommunicationThread;
import org.pitest.util.ReceiveStrategy;
import org.pitest.util.SafeDataInputStream;
import org.pitest.util.SafeDataOutputStream;

import java.net.ServerSocket;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
class ValidatorCommunicationThread extends CommunicationThread {
    private final DataReceiver receiver;

    public ValidatorCommunicationThread(final ServerSocket socket,
                                        final ValidatorArguments arguments) {
        this(socket, new DataSender(arguments), new DataReceiver());
    }

    public ValidatorCommunicationThread(final ServerSocket socket,
                                        final DataSender sender,
                                        final DataReceiver receiver) {
        super(socket,sender, receiver);
        this.receiver = receiver;
    }

    public ValidationOutcome getValidationOutcome() {
        return this.receiver.outcome;
    }

    private static class DataSender implements SideEffect1<SafeDataOutputStream> {
        final ValidatorArguments arguments;

        public DataSender(final ValidatorArguments arguments) {
            this.arguments = arguments;
        }

        @Override
        public void apply(final SafeDataOutputStream dos) {
            dos.write(this.arguments);
        }
    }

    private static class DataReceiver implements ReceiveStrategy {
        ValidationOutcome outcome;

        @Override
        public void apply(final byte control, final SafeDataInputStream dis) {
            if (control == ControlId.REPORT) {
                this.outcome = dis.read(ValidationOutcome.class);
            }
        }
    }
}