package edu.utdallas.prf.commons.junit.runner;

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

/**
 * Different values for test execution result during patch validation.
 * <code>EARLY_EXIT</code>: A test has failed, due to any reason, e.g., assertion failure
 * <code>TIMED_OUT</code>: A test has timed out (the time calculated via user-provided parameters)
 * <code>OK</code>: The test passed
 *
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public enum TestExecutionStatus {
    EARLY_EXIT,
    TIMED_OUT,
    OK
}
