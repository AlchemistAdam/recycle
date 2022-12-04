package dk.martinu.recycle;

import org.jetbrains.annotations.*;

/**
 * A {@code RetentionPolicy} that does not allow any elements to be retained
 * (pushed) in the stack. This can be used to temporarily disable recycling of
 * objects without circumventing the use of a {@link Recycler}. To obtain an
 * instance of this class, use {@link #get()}.
 *
 * @author Adam Martinu
 * @since 1.0
 */
public class PoolNone extends RetentionPolicy {

    /**
     * Private singleton.
     */
    @Nullable
    private static volatile PoolNone instance = null;

    /**
     * Returns an instance of this class.
     */
    @NotNull
    public static PoolNone get() {
        if (instance == null)
            synchronized (PoolNone.class) {
                if (instance == null)
                    instance = new PoolNone();
            }
        //noinspection ConstantConditions
        return instance;
    }

    /**
     * Private constructor for singleton pattern.
     */
    private PoolNone() { }

    /**
     * Returns {@code false}; This retention policy does not allow elements to
     * be pushed.
     */
    @Contract(pure = true)
    @Override
    public boolean canPush() {
        // never allow elements to be stored
        return false;
    }

    /**
     * Removes all elements from the specified stack; This retention policy
     * does not allow elements to be retained.
     */
    @Contract()
    @Override
    protected void install(@NotNull final RecyclerStack<?> stack) {
        super.install(stack);
        // remove any pooled elements
        stack.clear();
    }
}
