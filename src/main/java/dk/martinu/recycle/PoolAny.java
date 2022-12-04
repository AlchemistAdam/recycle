package dk.martinu.recycle;

import org.jetbrains.annotations.*;

/**
 * A {@code RetentionPolicy} implementation that does not limit the amount of
 * elements that can be retained (pushed) in the stack. To obtain an instance
 * of this class, use {@link #get()}.
 *
 * @author Adam Martinu
 * @since 1.0
 */
public class PoolAny extends RetentionPolicy {

    /**
     * Private singleton.
     */
    @Nullable
    private static volatile PoolAny instance = null;

    /**
     * Returns an instance of this class.
     */
    @NotNull
    public static PoolAny get() {
        if (instance == null)
            synchronized (PoolAny.class) {
                if (instance == null)
                    instance = new PoolAny();
            }
        //noinspection ConstantConditions
        return instance;
    }

    /**
     * Private constructor for singleton pattern.
     */
    private PoolAny() { }

    /**
     * Returns {@code true}; This retention policy allows all elements to be
     * pushed.
     */
    @Contract(pure = true)
    @Override
    public boolean canPush() {
        // always allow elements to be stored
        return true;
    }
}
