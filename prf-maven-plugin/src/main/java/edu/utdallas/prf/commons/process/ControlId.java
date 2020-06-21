package edu.utdallas.prf.commons.process;

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

import org.pitest.util.Id;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class ControlId {
    public static final byte REPORT = Id.REPORT;

    public static final byte REPORT_TEST_TIME = 1;

    public static final byte REPORT_FAILING_TEST_NAMES = 2;

    public static final byte REPORT_CLASS_LEVEL_FL = 4;

    public static final byte REPORT_CLASS_LEVEL_COV = 5;

    public static final byte REPORT_METHOD_LEVEL_FL = 8;

    public static final byte REPORT_METHOD_LEVEL_COV = 9;

    public static final byte REPORT_LINE_LEVEL_FL = 16;

    public static final byte REPORT_LINE_LEVEL_COV = 17;

    public static final byte DONE = Id.DONE;

    private ControlId() { }
}
