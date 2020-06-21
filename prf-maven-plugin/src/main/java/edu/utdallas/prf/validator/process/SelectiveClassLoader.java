package edu.utdallas.prf.validator.process;

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

import edu.utdallas.prf.Patch;
import edu.utdallas.prf.PatchLocation;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.pitest.classinfo.ClassByteArraySource;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class SelectiveClassLoader extends ClassLoader {
    private final ClassByteArraySource byteArraySource;

    private final String whiteListPrefix;

    private final Map<String, Pair<File, byte[]>> patchedFileTable;

    public SelectiveClassLoader(final ClassByteArraySource byteArraySource,
                                final String whiteListPrefix,
                                final Patch patch) {
        this.byteArraySource = byteArraySource;
        this.whiteListPrefix = whiteListPrefix;
        this.patchedFileTable = new HashMap<>();
        for (final PatchLocation location : patch.getLocations()) {
            final Pair<File, byte[]> pair = new MutablePair<>(location.getClassFile(), null);
            this.patchedFileTable.put(location.getClassName(), pair);
        }
    }

    @Override
    public Class<?> loadClass(final String name) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            final Class<?> clazz = findLoadedClass(name);
            if (clazz != null) {
                return clazz;
            }
            final Pair<File, byte[]> pair = this.patchedFileTable.get(name);
            if (pair != null) {
                byte[] bytes = pair.getValue();
                if (bytes == null) {
                    final File file = pair.getKey();
                    try {
                        bytes = FileUtils.readFileToByteArray(file);
                    } catch (IOException e) {
                        throw new ClassNotFoundException(file.getAbsolutePath(), e.getCause());
                    }
                }
                return defineClass(name, bytes, 0, bytes.length);
            } else if (name.startsWith(this.whiteListPrefix)) {
                final byte[] bytes = this.byteArraySource.getBytes(name).getOrElse(null);
                if (bytes != null) {
                    return defineClass(name, bytes, 0, bytes.length);
                }
            }
        }
        return super.loadClass(name);
    }
}
