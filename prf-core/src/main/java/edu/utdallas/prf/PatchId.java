package edu.utdallas.prf;

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

import java.io.Serializable;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class PatchId implements Serializable {
    private static final long serialVersionUID = 1L;

    private static int idCounter = 0;

    private final int id;

    private PatchId(int id) {
        this.id = id;
    }

    public static PatchId alloc() {
        return new PatchId(idCounter++);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final PatchId that = (PatchId) o;
        return this.id == that.id;
    }

    @Override
    public int hashCode() {
        return ((Integer) this.id).hashCode();
    }

    @Override
    public String toString() {
        return String.valueOf(this.id);
    }
}