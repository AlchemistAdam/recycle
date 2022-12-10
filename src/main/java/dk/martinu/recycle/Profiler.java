package dk.martinu.recycle;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

// DOC
// TEST
public class Profiler<T> extends Thread implements Recycler<T> {

    // DOC
    public static final int N_ELEMENTS = 0;
    // DOC
    public static final int N_BUCKETS = 1;
    // DOC
    public static final int N_FREE = 2;
    // DOC
    public static final int N_GET = 3;
    // DOC
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
     * the specified time interval in milliseconds between snapshots.
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
     * {@inheritDoc}
     */
    @Override
    public void free(final T element) {
        recycler.free(element);
        session.incrementFree();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T get() {
        final T rv = recycler.get();
        session.incrementGet();
        return rv;
    }

    /**
     * Returns a stream of all snapshots gathered in this session.
     */
    @Contract(value = "-> new", pure = true)
    @NotNull
    public synchronized Stream<int[]> getSnapshots() {
        return snapshots.stream();
    }

    /**
     * Returns an unmodifiable list that reads through to this session's list
     * of snapshots. Note that the backing list is updated asynchronously when
     * new snapshots are captured, and as such access to the returned list
     * should be synchronized on this {@code Profiler} instance.
     */
    @Contract(value = "-> new", pure = true)
    @NotNull
    public List<int[]> getSnapshotsList() {
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

    @Override
    public void run() {
        // timestamp of the last snapshot
        long timestamp = System.currentTimeMillis();
        // difference between current time and timestamp
        long delta;

        // TODO make it possible to stop the profiler
        //noinspection InfiniteLoopStatement
        while (true) {
            delta = System.currentTimeMillis() - timestamp;
            if (delta >= timeMs) {
                snapshots.add(session.createSnapshot());
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

    public static class Session {

        /**
         * The stack this session is created for.
         */
        @NotNull
        protected final RecyclerStack<?> stack;
        /**
         * The bucket that is currently used.
         */
        @NotNull
        protected RecyclerStack.Bucket<?> bucket;
        /**
         * Number of elements in the stack.
         */
        protected int nElements;
        /**
         * Number of buckets in the stack.
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
         * called while stack had elements) since the last snapshot was taken.
         */
        protected int nRecycled;

        /**
         * Constructs a new session for the specified recycler stack.
         * 
         * @throws NullPointerException if {@code stack} is {@code null}
         */
        public Session(@NotNull final RecyclerStack<?> stack) {
            this.stack = Objects.requireNonNull(stack, "stack is null");
            nElements = stack.size();

            // count number of buckets
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
         * numbers from the returned array.
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
         * Called whenever {@link Recycler#free(Object)} is called.
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
         * Called whenever {@link Recycler#get()} is called.
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
