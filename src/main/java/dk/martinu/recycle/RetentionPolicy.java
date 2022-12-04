package dk.martinu.recycle;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract class to determine how to retain elements in {@link RecyclerStack}
 * objects. Several default implementations are provided by this API:
 * <ul>
 *     <li>{@link PoolNone}</li>
 *     <li>{@link PoolAny}</li>
 *     <li>{@link PoolAnyTimed}</li>
 *     <li>{@link PoolMax}</li>
 *     <li>{@link PoolMaxTimed}</li>
 * </ul>
 * Extending this class or {@link RetentionPolicyTimed} allows for custom
 * behavior of retaining elements.
 *
 * @author Adam Martinu
 * @since 1.0
 */
public abstract class RetentionPolicy {

    /**
     * Returns {@code true} if an element can be pushed onto the stack,
     * otherwise {@code false}. This method will be called by
     * {@link RecyclerStack#push(Object)} to determine if the element should be
     * pushed or not.
     */
    public abstract boolean canPush();

    /**
     * Event method called by {@link RecyclerStack} whenever an element is
     * popped. Does nothing by default.
     */
    @Contract(pure = true)
    public void onPop() { }

    /**
     * Event method called by {@link RecyclerStack} whenever an element
     * is pushed. Does nothing by default.
     */
    @Contract(pure = true)
    public void onPush() { }

    /**
     * Called by {@link RecyclerStack} when constructing an instance or setting
     * a new retention policy. Does nothing by default.
     *
     * @param stack the stack this policy is being installed for
     * @see RecyclerStack#setRetentionPolicy(RetentionPolicy)
     */
    @Contract(pure = true)
    protected void install(@NotNull final RecyclerStack<?> stack) { }

    /**
     * Called by {@link RecyclerStack} when setting a new retention policy.
     * Does nothing by default.
     *
     * @see RecyclerStack#setRetentionPolicy(RetentionPolicy)
     */
    @Contract(pure = true)
    protected void uninstall() { }
}
