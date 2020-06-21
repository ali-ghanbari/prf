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

import java.io.Serializable;
import java.util.Objects;

public class Class implements ProgramElement, Serializable {
    private static final long serialVersionUID = 1L;

    private final String className;

    public Class(final String className) {
        this.className = className;
    }

    public String getClassName() {
        return this.className;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Class)) {
            return false;
        }
        final Class that = (Class) o;
        return this.className.equals(that.className);
    }

    @Override
    public int hashCode() {
        return this.className.hashCode();
    }
}
