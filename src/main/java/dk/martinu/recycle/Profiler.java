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
 * {@code Recycler} itself and delegates method calls to the recycler being
 * profiled. To profile a recycler, simply wrap it in a profiler:
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
 *     <li>Number of elements pushed onto the recycler stack with
 *     {@link Recycler#retain(Object)} or
 *     {@link Recycler#retain(Object[], int)}</li>
 *     <li>Number of elements popped from the recycler stack with
 *     {@link Recycler#get()} or {@link Recycler#get(Object[], int)}</li>
 *     <li>Number of elements that were recycled</li>
 * </ol>
 * The constant array indices {@code N_ELEMENTS}, {@code N_BUCKETS},
 * {@code N_RETAIN}, {@code N_GET} and {@code N_RECYCLED} can be used to access
 * these numbers from the snapshot array.
 * <p>
 * This implementation is thread safe and can be used concurrently.
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
     * Array index to retrieve the number of elements pushed onto the recycler
     * stack with {@link Recycler#retain(Object)} or
     * {@link Recycler#retain(Object[], int)}.
     *
     * @see #createSnapshot()
     * @see #getSnapshots()
     */
    public static final int N_RETAIN = 2;
    /**
     * Array index to retrieve the number of elements popped from the recycler
     * stack with {@link Recycler#get()} or
     * {@link Recycler#get(Object[], int)}.
     *
     * @see #createSnapshot()
     * @see #getSnapshots()
     */
    public static final int N_GET = 3;
    /**
     * Array index to retrieve the number of recycled elements .
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
     * Boolean flag that keeps this profiler looping while {@code true}. Set to
     * {@code false} with {@link #terminate()}.
     */
    protected boolean run = true;

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
        synchronized (recycler) {
            recycler.clear();
            session.clear();
        }
    }

    /**
     * Creates a snapshot and returns it. The constant array indices
     * {@code N_ELEMENTS}, {@code N_BUCKETS}, {@code N_RETAIN}, {@code N_GET}
     * and {@code N_RECYCLED} can be used to access the numbers from the
     * snapshot array.
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
    public T get() {
        final T rv;
        synchronized (recycler) {
            rv = recycler.get();
            session.incrementGet();
        }
        startIfNew();
        return rv;
    }

    /**
     * {@inheritDoc}
     */
    @Contract(value = "_, _ -> param1", mutates = "param1")
    @Override
    public T[] get(final T[] array, final int n) {
        synchronized (recycler) {
            recycler.get(array, n);
            session.incrementGetArray(n);
        }
        startIfNew();
        return array;
    }

    /**
     * Returns an unmodifiable list that reads through to this profiler's list
     * of snapshots. The constant array indices {@code N_ELEMENTS},
     * {@code N_BUCKETS}, {@code N_RETAIN}, {@code N_GET} and
     * {@code N_RECYCLED} can be used to access the numbers from the snapshot
     * array.
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
     * {@inheritDoc}
     */
    @Override
    public void retain(final T element) {
        synchronized (recycler) {
            recycler.retain(element);
            session.incrementRetain();
        }
        startIfNew();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void retain(final T[] array, final int n) {
        synchronized (recycler) {
            recycler.retain(array, n);
            session.incrementRetainArray();
        }
        startIfNew();
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

        while (true) {
            delta = System.currentTimeMillis() - timestamp;
            if (delta >= timeMs)
                synchronized (this) {
                    if (run) {
                        snapshots.add(session.createSnapshot());

                        timestamp = System.currentTimeMillis();
                    }
                    else
                        break;
                }
                // wait remaining time
            else
                synchronized (this) {
                    if (run)
                        try {
                            wait(timeMs - delta);
                        }
                        catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    else
                        break;
                }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRetentionPolicy(final @NotNull RetentionPolicy policy) {
        synchronized (recycler) {
            recycler.setRetentionPolicy(policy);
            session.reset();
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
     * Terminates this profiler, preventing any snapshots from being captured
     * after this call. Does nothing if this {@code Profiler} is already
     * terminated.
     */
    public void terminate() {
        if (!run)
            return;

        synchronized (this) {
            if (run) {
                run = false;
                notifyAll(); // wake profiler's thread if sleeping to exit loop early
            }
        }
    }

    /**
     * Starts this thread if {@code getState()} returns {@code State.NEW}.
     */
    protected synchronized void startIfNew() {
        if (getState() == State.NEW)
            start();
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
         * Internal cursor copy used for tracking of elements.
         */
        protected int cursor;
        /**
         * Number of elements in the recycler stack.
         */
        protected int nElements;
        /**
         * Number of buckets in the recycler stack.
         */
        protected int nBuckets;
        /**
         * Number of elements pushed onto the recycler stack with
         * {@link Recycler#retain(Object)} or
         * {@link Recycler#retain(Object[], int)} since the last snapshot was
         * taken.
         */
        protected int nRetain;
        /**
         * Number of elements popped from the recycler stack with
         * {@link Recycler#get()} or {@link Recycler#get(Object[], int)} since
         * the last snapshot was taken.
         */
        protected int nGet;
        /**
         * Number of times an element was recycled ({@link Recycler#get()} or
         * {@link Recycler#get(Object[], int)} was called while the recycler
         * stack had elements) since the last snapshot was taken.
         */
        protected int nRecycled;

        /**
         * Constructs a new session for the specified recycler stack.
         *
         * @throws NullPointerException if {@code stack} is {@code null}
         */
        public Session(@NotNull final RecyclerStack<?> stack) {
            this.stack = Objects.requireNonNull(stack, "stack is null");
            this.cursor = stack.cursor;

            // initial number of elements
            nElements = stack.size();

            // count initial number of buckets
            nBuckets = stack.bucketCount;
        }

        /**
         * Called whenever {@link Profiler#clear()} is called.
         */
        public synchronized void clear() {
            nElements = 0;
            nBuckets = 1;
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
            rv[N_RETAIN] = nRetain;
            rv[N_GET] = nGet;
            rv[N_RECYCLED] = nRecycled;

            nRetain = nGet = nRecycled = 0;

            return rv;
        }

        /**
         * Called whenever {@link Profiler#get()} is called.
         */
        public synchronized void incrementGet() {
            nGet++;

            if (nElements > 0) {
                nRecycled++;
                nElements--;
                nBuckets = stack.bucketCount;
                cursor = stack.cursor;
            }
        }

        /**
         * Called whenever {@link Profiler#get(Object[], int)} is called.
         *
         * @param n {@code int} parameter passed to {@code get}
         */
        public synchronized void incrementGetArray(final int n) {
            nGet += n;

            if (nElements > 0) {
                final int popCount = Math.min(nElements, n);
                nRecycled += popCount;
                nElements -= popCount;
                nBuckets = stack.bucketCount;
                cursor = stack.cursor;
            }
        }

        /**
         * Called whenever {@link Profiler#retain(Object)} is called.
         */
        public synchronized void incrementRetain() {
            // new bucket in stack
            if (nBuckets != stack.bucketCount) {
                nRetain++;
                nElements++;
                nBuckets = stack.bucketCount;
                cursor = stack.cursor;
            }
            // new cursor position in same bucket
            else if (cursor != stack.cursor) {
                nRetain++;
                nElements++;
                cursor = stack.cursor;
            }
        }

        /**
         * Called whenever {@link Profiler#retain(Object[], int)} is called.
         */
        public synchronized void incrementRetainArray() {
            // new bucket(s) in stack
            if (nBuckets != stack.bucketCount) {
                int pushCount = 0;

                // temporary bucket variable
                RecyclerStack.Bucket<?> bucket = stack.bucket;

                // follow links until bucket var is equal to last known bucket
                for (int i = stack.bucketCount; i > nBuckets; i--) {
                    //noinspection ConstantConditions
                    pushCount += bucket.array.length;
                    bucket = bucket.next;
                }

                //noinspection ConstantConditions
                pushCount += bucket.array.length - cursor;

                nRetain += pushCount;
                nElements += pushCount;
                nBuckets = stack.bucketCount;
                cursor = stack.cursor;
            }
            // new cursor position in same bucket
            else if (cursor != stack.cursor) {
                final int pushCount = stack.cursor - cursor;

                nRetain += pushCount;
                nElements += pushCount;
                cursor = stack.cursor;
            }
        }

        /**
         * Resets the element and bucket count to the values returned by the
         * stack. Called whenever
         * {@link Profiler#setRetentionPolicy(RetentionPolicy)} is called.
         */
        public synchronized void reset() {
            nElements = stack.size();
            nBuckets = stack.bucketCount;
        }
    }
}
