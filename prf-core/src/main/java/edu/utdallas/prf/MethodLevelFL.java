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
 * This is a hash table mapping a method to the suspiciousness value computed for
 * that method. Being a hash table, apparently, the object's <code>get</code> method
 * shall return <code>null</code> in case the given method is covered by no test case.
 *
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class MethodLevelFL extends FaultLocalizationInfo<Method> {

}
