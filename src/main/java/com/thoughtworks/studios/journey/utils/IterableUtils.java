package com.thoughtworks.studios.journey.utils;


import org.apache.commons.lang.NotImplementedException;
import org.neo4j.function.Function;
import org.neo4j.helpers.Predicate;
import org.neo4j.helpers.collection.Iterables;

import java.util.ArrayList;
import java.util.Iterator;

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
