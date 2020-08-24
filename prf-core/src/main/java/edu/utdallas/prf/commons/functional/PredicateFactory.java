package edu.utdallas.prf.commons.functional;

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

import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.predicate.Predicate;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public final class PredicateFactory {
    private PredicateFactory() { }

    public static <T extends Serializable> Predicate<T> fromCollection(final Collection<T> collection) {
        return new SerializableSetPredicate<>(collection);
    }

    public static <T extends Serializable> Predicate<T> alwaysTrue() {
        return new SerializablePredicate<T>() {
            @Override
            public Boolean apply(T t) {
                return true;
            }
        };
    }

    public static <T extends Serializable> Predicate<T> alwaysFalse() {
        return new SerializablePredicate<T>() {
            @Override
            public Boolean apply(T t) {
                return false;
            }
        };
    }

    public static F<String, Predicate<String>> toGlobPredicate() {
        return new F<String, Predicate<String>>() {
            @Override
            public SerializableGlob apply(final String glob) {
                return new SerializableGlob(glob);
            }
        };
    }

    public static Collection<Predicate<String>> toGlobPredicates(final Collection<String> globs) {
        return FCollection.map(globs, toGlobPredicate());
    }

    public static Predicate<String> orGlobs(final Collection<String> globs) {
        return new SerializableOr<>(FCollection.map(globs, toGlobPredicate()));
    }

    public static Predicate<String> notGlob(final String glob) {
        return new SerializableNot<>(toGlobPredicate().apply(glob));
    }

    public static <T extends Serializable> Predicate<T> not(final Predicate<T> operand) {
        return new SerializableNot<>(operand);
    }

    public static <T extends Serializable> Predicate<T> and(final Predicate<T> operand1, final Predicate<T> operand2) {
        return new SerializableAnd<>(Arrays.asList(operand1, operand2));
    }

    public static <T extends Serializable> Predicate<T> or(final Predicate<T> operand1, final Predicate<T> operand2) {
        return new SerializableOr<>(Arrays.asList(operand1, operand2));
    }

    static class SerializableSetPredicate<T extends Serializable> implements SerializablePredicate<T> {
        private static final long serialVersionUID = 1L;

        private final Set<T> set;

        public SerializableSetPredicate(final Set<T> set) {
            this.set = set;
        }

        public SerializableSetPredicate(final Collection<T> collection) {
            this.set = new HashSet<>(collection);
        }

        @Override
        public Boolean apply(final T value) {
            return this.set.contains(value);
        }
    }

    // credit: copied from PITest source code
    static class SerializableAnd<T extends Serializable> implements SerializablePredicate<T> {
        private static final long serialVersionUID = 1L;

        private final Set<F<T, Boolean>> ps = new LinkedHashSet<>();

        public SerializableAnd(final Iterable<? extends F<T, Boolean>> ps) {
            for (final F<T, Boolean> each : ps) {
                this.ps.add(each);
            }
        }

        @Override
        public Boolean apply(final T a) {
            for (final F<T, Boolean> each : this.ps) {
                if (!each.apply(a)) {
                    return false;
                }
            }
            return !this.ps.isEmpty();
        }
    }

    // credit: copied from PITest source code
    static class SerializableNot<T extends Serializable> implements SerializablePredicate<T> {
        private static final long serialVersionUID = 1L;

        private final F<T, Boolean> p;

        public SerializableNot(final F<T, Boolean> p) {
            this.p = p;
        }

        @Override
        public Boolean apply(final T t) {
            return !this.p.apply(t);
        }
    }

    // credit: copied from PITest source code
    static class SerializableOr<T extends Serializable> implements SerializablePredicate<T> {
        private static final long serialVersionUID = 1L;

        private final Set<Predicate<T>> ps = new LinkedHashSet<>();

        public SerializableOr(final Iterable<Predicate<T>> ps) {
            for (final Predicate<T> each : ps) {
                this.ps.add(each);
            }
        }

        @Override
        public Boolean apply(final T t) {
            for (final Predicate<T> each : this.ps) {
                if (each.apply(t)) {
                    return true;
                }
            }
            return false;
        }
    }

    // credit: copied from PITest source code
    static class SerializableGlob implements SerializablePredicate<String> {
        private static final long serialVersionUID = 1L;

        private final Pattern regex;

        public SerializableGlob(final String glob) {
            if (glob.startsWith("~")) {
                this.regex = Pattern.compile(glob.substring(1));
            } else {
                this.regex = Pattern.compile(convertGlobToRegex(glob));
            }
        }

        private static String convertGlobToRegex(final String glob) {
            final StringBuilder out = new StringBuilder("^");
            for (int i = 0; i < glob.length(); ++i) {
                final char c = glob.charAt(i);
                switch (c) {
                    case '$':
                        out.append("\\$");
                        break;
                    case '*':
                        out.append(".*");
                        break;
                    case '?':
                        out.append('.');
                        break;
                    case '.':
                        out.append("\\.");
                        break;
                    case '\\':
                        out.append("\\\\");
                        break;
                    default:
                        out.append(c);
                }
            }
            out.append('$');
            return out.toString();
        }

        public boolean matches(final CharSequence seq) {
            return this.regex.matcher(seq).matches();
        }

        @Override
        public Boolean apply(final String value) {
            return matches(value);
        }

        @Override
        public String toString() {
            return this.regex.pattern();
        }
    }
}
