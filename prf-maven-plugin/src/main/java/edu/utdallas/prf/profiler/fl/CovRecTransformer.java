package edu.utdallas.prf.profiler.fl;

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

import edu.utdallas.prf.commons.relational.StringDomain;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * The base class for all fault localization transformers.
 * Subclasses of this class file transformer can transform all non-native, concrete methods
 * of all application classes.
 *
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public abstract class CovRecTransformer implements ClassFileTransformer {
    protected final String whiteListPrefix;

    public CovRecTransformer(final String whiteListPrefix) {
        this.whiteListPrefix = whiteListPrefix.replace('.', '/');
    }

    @Override
    public byte[] transform(final ClassLoader loader,
                            final String className,
                            final Class<?> classBeingRedefined,
                            final ProtectionDomain protectionDomain,
                            final byte[] classfileBuffer) throws IllegalClassFormatException {
        if (!className.startsWith(this.whiteListPrefix)) {
            return null; // no transformation
        }
        return transform(className, classfileBuffer);
    }

    protected abstract byte[] transform(String className, byte[] classBytes);

    /**
     * Returns the <code>StringDomain</code> used during FL transformation
     *
     * @return The <code>StringDomain</code> used during FL transformation
     */
    public abstract StringDomain getDomain();
}
