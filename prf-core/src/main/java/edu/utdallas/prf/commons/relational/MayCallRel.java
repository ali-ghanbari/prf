package edu.utdallas.prf.commons.relational;

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
 * A binary relation representing may-call relation.
 *
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class MayCallRel extends BinaryRelation {
    protected MayCallRel(final BinaryRelation binaryRel) {
        super(binaryRel);
    }

    public MayCallRel(MethodsDom methodsDom) {
        super("may_call", "_", methodsDom, methodsDom);
    }

    public static MayCallRel load(MethodsDom methodsDom) {
        final BinaryRelation binRel = BinaryRelation.load("may_call", methodsDom, methodsDom);
        return new MayCallRel(binRel);
    }

    public MethodsDom getMethodsDom() {
        return (MethodsDom) getLeftDomain();
    }
}
