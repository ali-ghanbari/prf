package edu.utdallas.prf.commons.misc;

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
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public final class Ansi {
    public enum ColorCode {
        BOLD_FACE   ("\033[1m"),
        UNDERLINED  ("\033[4m"),
        NORMAL      ("\033[0m"),
        BLACK       ("\u001B[30m"),
        RED         ("\u001B[31m"),
        GREEN       ("\u001B[32m"),
        YELLOW      ("\u001B[33m"),
        BLUE        ("\u001B[34m"),
        MAGENTA     ("\u001B[35m"),
        CYAN        ("\u001B[36m"),
        WHITE       ("\u001B[37m");

        ColorCode(String code) {
            this.code = code;
        }

        private final String code;

        @Override
        public String toString() {
            return this.code;
        }
    }

    private Ansi() {

    }

    public static String construct(final String message, final ColorCode... color) {
        final StringBuilder c = new StringBuilder();
        for (final ColorCode colorCode : color) {
            c.append(colorCode.code);
        }
        return c.toString() + message + ColorCode.NORMAL;
    }
}