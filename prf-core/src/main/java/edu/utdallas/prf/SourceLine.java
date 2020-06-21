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

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class SourceLine implements ProgramElement, Serializable {
    private static final long serialVersionUID = 1L;

    private final String fileName;

    private final int lineNo;

    public SourceLine(final String fileName,
                      final int lineNo) {
        this.fileName = fileName;
        this.lineNo = lineNo;
    }

    public String getFileName() {
        return this.fileName;
    }

    public int getLineNo() {
        return this.lineNo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SourceLine)) {
            return false;
        }
        final SourceLine that = (SourceLine) o;
        return this.lineNo == that.lineNo && this.fileName.equals(that.fileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.fileName, this.lineNo);
    }

    @Override
    public String toString() {
        return String.format("(%s, %d)", this.fileName, this.lineNo);
    }
}
