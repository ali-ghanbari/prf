package edu.utdallas.prf.commons.relational;

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

import edu.utdallas.relational.Domain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;

/**
 * A class mapping string keys to unique integers, vice versa.
 *
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class StringDomain extends Domain<String> {

    public StringDomain(final String name) {
        setName(name);
    }

    public void load(final String dirName) {
        final String[] fields;
        final File domFile = new File(dirName, getName() + ".dom");
        try (final Reader reader = new FileReader(domFile);
             final BufferedReader br = new BufferedReader(reader)) {
            fields = br.readLine().split("\\s");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (!fields[0].equals(getName())) {
            throw new IllegalArgumentException();
        }
        final int size = Integer.parseInt(fields[1]);
        final File mapFile = new File(dirName, fields[2]);
        try (final Reader reader = new FileReader(mapFile);
             final BufferedReader br = new BufferedReader(reader)) {
            for (int i = 0; i < size; i++) {
                String line = br.readLine();
                if (line == null) {
                    throw new IllegalArgumentException("Unexpected end of file");
                }
                line = line.trim();
                add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save(String dirName, boolean saveDomMap) {
        try {
            super.save(dirName, saveDomMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}