package edu.utdallas.prf;

/*
 * #%L
 * prf-core
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
 * A hash table mapping a test name to the set of source lines covered by the test.
 * A source line is modeled using a <code>SourceLine</code> object.
 * Being a hash table, apparently, the object's <code>get</code> method shall
 * return <code>null</code> in case a given test case covers no application classes.
 *
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class LineLevelCoverage extends CoverageInfo<SourceLine> {

}
