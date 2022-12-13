/*
 * Copyright (c) 2021, Adam Martinu. All rights reserved. Altering or
 * removing copyright notices or this file header is not allowed.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");  you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package dk.martinu.recycle;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;

// TEST

/**
 * Utility class to profile {@link Recycler} instances. This class implements
 * {@code Recycler} itself and simply delegates method calls to the recycler
 * being profiled. To profile a recycler, simply wrap it in a profiler:
 * <pre>
 *     Recycler r = new Profiler(new MyRecycler());
 * </pre>
 * You can then continue to use the {@code Recycler} instance as usual.
 * <p>
 * Profilers are also daemon threads, and capture snapshots of gathered
 * statistics asynchronously at a fixed time interval which can be passed to
 * the constructor. Asynchronous capturing of snapshots is disabled if a time
 * interval of {@code 0} is used. A snapshot is an {@code int} array that
 * contains the following:
 * <ol start=0>
 *     <li>Number of elements in the recycler stack</li>
 *     <li>Number of buckets in the recycler stack</li>
 *     <li>Number of times {@link Recycler#free(Object)} was called</li>
 *     <li>Number of times {@link Recycler#get()} was called</li>
 *     <li>Number of elements that were recycled</li>
 * </ol>
 * The constant array indices {@code N_ELEMENTS}, {@code N_BUCKETS},
 * {@code N_FREE}, {@code N_GET} and {@code N_RECYCLED} can be used to access
 * these numbers from the snapshot array.
 *
 * @author Adam Martinu
 * @since 1.0
 */
public class Profiler<T> extends Thread implements Recycler<T> {

    /**
     * Array index to retrieve the number of elements in the recycler stack.
     *
     * @see #createSnapshot()
     * @see #getSnapshots()
     */
    public static final int N_ELEMENTS = 0;
    /**
     * Array index to retrieve the number of buckets in the recycler stack.
     *
     * @see #createSnapshot()
     * @see #getSnapshots()
     */
    public static final int N_BUCKETS = 1;
    /**
     * Array index to retrieve the number of times
     * {@link Recycler#free(Object)} was called.
     *
     * @see #createSnapshot()
     * @see #getSnapshots()
     */
    public static final int N_FREE = 2;
    /**
     * Array index to retrieve the number of times {@link Recycler#get()} was
     * called.
     *
     * @see #createSnapshot()
     * @see #getSnapshots()
     */
    public static final int N_GET = 3;
    /**
     * Array index to retrieve the number of times an element was recycled.
     *
     * @see #createSnapshot()
     * @see #getSnapshots()
     */
    public static final int N_RECYCLED = 4;

    /**
     * Time interval in milliseconds between snapshots.
     */
    public final long timeMs;
    /**
     * This profilers recycler.
     */
    @NotNull
    public final Recycler<T> recycler;
    /**
     * Current profiling session.
     */
    @NotNull
    protected final Session session;
    /**
     * List of gathered snapshots.
     */
    @NotNull
    protected final ArrayList<int[]> snapshots = new ArrayList<>(32);

    /**
     * Constructs a new profiler wrapped around the specified recycler with a
     * time interval of 10 seconds between snapshots.
     *
     * @param recycler the recycler to profile
     * @throws NullPointerException if {@code recycler} is {@code null}
     */
    public Profiler(@NotNull final Recycler<T> recycler) {
        this(10000L, recycler);
    }

    /**
     * Constructs a new profiler wrapped around the specified recycler with
     * the specified time interval in milliseconds between snapshots. This
     * profiler will not capture any snapshots if {@code timeMs} is equal to
     * {@code 0} (see {@link #createSnapshot()}).
     *
     * @param timeMs   time in milliseconds between snapshots
     * @param recycler the recycler to profile
     * @throws IllegalArgumentException if {@code timeMs} is negative
     * @throws NullPointerException     if {@code recycler} is {@code null}
     */
    public Profiler(final long timeMs, @NotNull final Recycler<T> recycler) {
        if (timeMs < 0)
            throw new IllegalArgumentException("timeMs is negative");
        this.timeMs = timeMs;
        this.recycler = Objects.requireNonNull(recycler, "recycler is null");
        session = new Session(recycler.getStack());

        setDaemon(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        recycler.clear();
    }

    /**
     * Creates a snapshot and returns it. The constant array indices
     * {@code N_ELEMENTS}, {@code N_BUCKETS}, {@code N_FREE}, {@code N_GET} and
     * {@code N_RECYCLED} can be used to access the numbers from the snapshot
     * array.
     * <p>
     * <b>NOTE:</b> this method should only be used when snapshot captures by
     * this profiler is disabled ({@code timeMs} is set to {@code 0}) and
     * statistics must be gathered manually.
     *
     * @return a new snapshot
     * @see #Profiler(long, Recycler)
     */
    @Contract(value = "-> new")
    public synchronized int[] createSnapshot() {
        return session.createSnapshot();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void free(final T element) {
        recycler.free(element);
        session.incrementFree();
        synchronized (this) {
            if (getState() == State.NEW)
                start();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T get() {
        final T rv = recycler.get();
        session.incrementGet();
        synchronized (this) {
            if (getState() == State.NEW)
                start();
        }
        return rv;
    }

    /**
     * Returns an unmodifiable list that reads through to this profiler's list
     * of snapshots. The constant array indices {@code N_ELEMENTS},
     * {@code N_BUCKETS}, {@code N_FREE}, {@code N_GET} and {@code N_RECYCLED}
     * can be used to access the numbers from the snapshot array.
     * <p>
     * <b>NOTE:</b> the backing list is updated asynchronously when new
     * snapshots are captured, and as such access to the returned list should
     * be synchronized on this {@code Profiler} instance.
     */
    @Contract(value = "-> new", pure = true)
    @NotNull
    public List<int[]> getSnapshots() {
        return Collections.unmodifiableList(snapshots);
    }

    /**
     * {@inheritDoc}
     */
    @Contract(pure = true)
    @NotNull
    @Override
    public RecyclerStack<?> getStack() {
        return recycler.getStack();
    }

    /**
     * Captures snapshots at a fixed time interval. Does nothing if
     * {@link #timeMs} is equal to {@code 0}.
     */
    @Override
    public void run() {
        if (timeMs == 0)
            return;

        // timestamp of the last snapshot
        long timestamp = System.currentTimeMillis();
        // difference between current time and timestamp
        long delta;

        // TODO make it possible to stop the profiler
        //noinspection InfiniteLoopStatement
        while (true) {
            delta = System.currentTimeMillis() - timestamp;
            if (delta >= timeMs) {
                synchronized (this) {
                    snapshots.add(session.createSnapshot());
                }
                timestamp = System.currentTimeMillis();
            }
            // wait remaining time
            else
                try {
                    synchronized (this) {
                        wait(timeMs - delta);
                    }
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Contract(pure = true)
    @Override
    public int size() {
        return recycler.size();
    }

    /**
     * Container class for storing statistics during a profiling session.
     */
    public static class Session {

        /**
         * The recycler stack this session is created for.
         */
        @NotNull
        protected final RecyclerStack<?> stack;
        /**
         * The bucket that is currently used.
         */
        @NotNull
        protected RecyclerStack.Bucket<?> bucket;
        /**
         * Number of elements in the recycler stack.
         */
        protected int nElements;
        /**
         * Number of buckets in the recycler stack.
         */
        protected int nBuckets;
        /**
         * Number of times {@link Recycler#free} was called since the last
         * snapshot was taken.
         */
        protected int nFree;
        /**
         * Number of times {@link Recycler#get()} was called since the last
         * snapshot was taken.
         */
        protected int nGet;
        /**
         * Number of times an element was recycled ({@link Recycler#get()} was
         * called while the recycler stack had elements) since the last snapshot
         * was taken.
         */
        protected int nRecycled;

        /**
         * Constructs a new session for the specified recycler stack.
         *
         * @throws NullPointerException if {@code stack} is {@code null}
         */
        public Session(@NotNull final RecyclerStack<?> stack) {
            this.stack = Objects.requireNonNull(stack, "stack is null");

            // initial number of elements
            nElements = stack.size();

            // count initial number of buckets
            nBuckets = 1;
            bucket = stack.bucket;
            while (bucket.next != null) {
                bucket = bucket.next;
                nBuckets++;
            }

            // reset bucket variable to last bucket
            bucket = stack.bucket;
        }

        /**
         * Creates a snapshot of this session and returns it. Use the constant
         * array indices in {@link Profiler} to retrieve the respective
         * numbers from the returned snapshot array.
         */
        @Contract(value = "-> new")
        public synchronized int[] createSnapshot() {
            final int[] rv = new int[5];
            rv[N_ELEMENTS] = nElements;
            rv[N_BUCKETS] = nBuckets;
            rv[N_FREE] = nFree;
            rv[N_GET] = nGet;
            rv[N_RECYCLED] = nRecycled;

            nFree = nGet = nRecycled = 0;

            return rv;
        }

        /**
         * Called whenever {@link Profiler#free(Object)} is called.
         */
        public synchronized void incrementFree() {
            nFree++;
            nElements++;
            if (bucket != stack.bucket) {
                bucket = stack.bucket;
                nBuckets++;
            }
        }

        /**
         * Called whenever {@link Profiler#get()} is called.
         */
        public synchronized void incrementGet() {
            nGet++;
            if (nElements > 0) {
                nRecycled++;
                nElements--;
            }
            if (bucket != stack.bucket) {
                bucket = stack.bucket;
                nBuckets--;
            }
        }
    }
}
