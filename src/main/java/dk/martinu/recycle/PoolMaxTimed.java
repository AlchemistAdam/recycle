package dk.martinu.recycle;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntUnaryOperator;

/**
 * A {@code RetentionPolicy} that limits the amount of elements that
 * can be retained (pushed) in the stack, and disposes elements at a fixed time
 * interval.
 *
 * @author Adam Martinu
 * @see #PoolMaxTimed(int, long, IntUnaryOperator)
 * @since 1.0
 */
public class PoolMaxTimed extends RetentionPolicyTimed {

    /**
     * Maximum number of elements that can be retained
     */
    public final int size;
    /**
     * Number of currently retained elements in the stack.
     */
    protected int count = 0;

    /**
     * Constructs a new timed retention policy with the specified maximum pool
     * size, time interval and operator.
     *
     * @param size     maximum number of elements that can be retained
     * @param timeMs   time interval between disposals in milliseconds
     * @param operator operator to determine how many elements to dispose
     * @throws IllegalArgumentException if {@code timeMs < 1} or
     *                                  {@code size < 1}
     * @throws NullPointerException     if {@code operator} is {@code null}
     */
    public PoolMaxTimed(final int size, final long timeMs, @NotNull final IntUnaryOperator operator) {
        super(timeMs, operator);
        if (size < 1)
            throw new IllegalArgumentException("size is less than 1 {" + size + "}");
        this.size = size;

    }

    /**
     * Returns {@code true} if the number of elements in the stack is less than
     * the maximum pool size, otherwise {@code false}.
     */
    @Contract(pure = true)
    @Override
    public boolean canPush() {
        return count < size;
    }

    /**
     * Decreases the count of retained elements.
     */
    @Override
    public void onPop() {
        count--;
    }

    /**
     * Increases the count of retained elements.
     */
    @Override
    public void onPush() {
        count++;
    }

    /**
     * Terminates this retention policy's {@link Disposer} and resets the
     * element counter.
     */
    @Override
    protected void uninstall() {
        super.uninstall();
        count = 0;
    }
}
