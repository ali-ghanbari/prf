package edu.utdallas.prf.commons.misc;

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

/**
 * Utility functions for "cleanly" accessing properties.
 *
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class PropertyUtils {
    private PropertyUtils() { }

    public static int getIntProperty(final String property, final int defaultVal) {
        return Integer.parseInt(System.getProperty(property, String.valueOf(defaultVal)));
    }
}
