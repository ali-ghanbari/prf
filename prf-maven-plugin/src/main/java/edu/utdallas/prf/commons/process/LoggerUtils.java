package edu.utdallas.prf.commons.process;

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

import org.pitest.functional.SideEffect1;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public final class LoggerUtils {
    private static final Object LOCK = new Object();

    private LoggerUtils() { }

    public static SideEffect1<String> out() {
        return new SideEffect1<String>() {
            @Override
            public void apply(final String msg) {
                synchronized (LOCK) {
                    System.out.print(msg);
                    System.out.flush();
                }
            }
        };
    }

    public static SideEffect1<String> err() {
        return new SideEffect1<String>() {
            @Override
            public void apply(final String msg) {
                synchronized (LOCK) {
                    System.out.print(msg);
                    System.out.flush();
                }
            }
        };
    }
}