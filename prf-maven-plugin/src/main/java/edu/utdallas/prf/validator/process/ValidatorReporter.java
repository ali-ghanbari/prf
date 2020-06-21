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

import edu.utdallas.prf.commons.process.AbstractReporter;
import edu.utdallas.prf.commons.process.ControlId;

import java.io.OutputStream;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
class ValidatorReporter extends AbstractReporter {
    public ValidatorReporter(final OutputStream os) {
        super(os);
    }

    public synchronized void reportValidationOutcome(final ValidationOutcome outcome) {
        this.dos.writeByte(ControlId.REPORT);
        this.dos.write(outcome);
        this.dos.flush();
    }
}