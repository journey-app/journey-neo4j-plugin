/**
 * This file is part of journey-neo4j-plugin. journey-neo4j-plugin is a neo4j server extension that provides out-of-box action path analysis features on top of the graph database.
 *
 * Copyright 2015 ThoughtWorks, Inc. and Pengchao Wang
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.thoughtworks.studios.journey.utils;


import org.apache.commons.lang.NotImplementedException;
import org.neo4j.function.Function;
import org.neo4j.helpers.Predicate;
import org.neo4j.helpers.collection.Iterables;

import java.util.*;

import static org.neo4j.helpers.collection.Iterables.filter;

public class IterableUtils {

    public static <T> Iterable<T> compact(Iterable<T> iterable) {
        return filter(new Predicate<T>() {
            @Override
            public boolean accept(T item) {
                return item != null;
            }
        }, iterable);
    }

    public static <T> Iterable<T> toIterable(final Iterator<T> iterator) {
        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return iterator;
            }
        };
    }


    public static <FROM, TO> Iterable<TO> flatMap(final Function<FROM, Iterable<TO>> function, Iterable<FROM> from) {
        Iterator<TO> iterator = Iterables.flatMap(new Function<FROM, Iterator<TO>>() {
            @Override
            public Iterator<TO> apply(FROM o) {
                return function.apply(o).iterator();
            }
        }, from.iterator());
        return toIterable(iterator);
    }

    public static <T> RewindableIterator<T> rewindable(Iterator<T> original) {
        return new RewindableIterator<T>(original);
    }

    public static <T> Iterable<T> uniqueBy(final Function<T, Object> function, final Iterable<T> iterable) {
        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                final Iterator<T> iterator = iterable.iterator();

                return new Iterator<T>() {
                    Set<Object> keys = new HashSet<>();
                    T nextItem;

                    @Override
                    public boolean hasNext() {
                        while (iterator.hasNext()) {
                            nextItem = iterator.next();
                            if (keys.add(function.apply(nextItem))) {
                                return true;
                            }
                        }

                        return false;
                    }

                    @Override
                    public T next() {
                        if (nextItem == null && !hasNext()) {
                            throw new NoSuchElementException();
                        }

                        return nextItem;
                    }

                    @Override
                    public void remove() {
                    }
                };
            }
        };
    }

    public static class RewindableIterator<T> implements Iterator<T> {
        private final ArrayList<T> visited;
        private Iterator<T> original;

        public RewindableIterator(Iterator<T> original) {
            this.original = original;
            this.visited = new ArrayList<T>();
        }

        @Override
        public boolean hasNext() {
            return original.hasNext();
        }

        @Override
        public T next() {
            T next = original.next();
            visited.add(next);
            return next;
        }

        @Override
        public void remove() {
            throw new NotImplementedException("can not remove in a rewindable iterator");
        }

        public Iterator<T> rewind() {
            return Iterables.concat(visited.iterator(), original);
        }

        public T lastVisited() {
            return visited.get(visited.size() - 1);
        }

        public Iterator<T> original() {
            return original;
        }
    }
}
