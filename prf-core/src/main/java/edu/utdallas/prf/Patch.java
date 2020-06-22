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

import edu.utdallas.prf.commons.junit.runner.TestUnitFilter;
import org.apache.commons.lang3.Validate;

import java.io.Serializable;
import java.util.Collection;

/**
 * This class represents a generated, compiled patch.
 *
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class Patch implements Serializable {
    private static final long serialVersionUID = 1L;

    private final PatchId id;

    private final Collection<PatchLocation> locations;

    private final TestUnitFilter coveringTestsFilter;

    public Patch(final Collection<PatchLocation> locations) {
        Validate.isInstanceOf(Serializable.class, locations);
        this.id = PatchId.alloc();
        this.locations = locations;
        this.coveringTestsFilter = TestUnitFilter.all();
    }

    public Patch(final Collection<PatchLocation> locations,
                 final Collection<String> coveringTests) {
        Validate.isInstanceOf(Serializable.class, locations);
        Validate.isInstanceOf(Serializable.class, coveringTests);
        this.id = PatchId.alloc();
        this.locations = locations;
        this.coveringTestsFilter = TestUnitFilter.some(coveringTests);
    }

    public PatchId getId() {
        return this.id;
    }

    public Collection<PatchLocation> getLocations() {
        return this.locations;
    }

    public TestUnitFilter getCoveringTestsFilter() {
        return this.coveringTestsFilter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Patch)) {
            return false;
        }
        final Patch that = (Patch) o;
        return this.id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
}
