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
 * This is essentially a map with the following signature:
 * class name -> suspiciousness value
 * This is a bit different that what is common in literature. However, we believe, most
 * of the times, class-level FL coincides with file level FL.
 * In fact this is more general than that, as one can aggregate the suspiciousness
 * values and calculate file-level suspiciousness values.
 *
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class ClassLevelFL extends FaultLocalizationInfo<Class> {

}
