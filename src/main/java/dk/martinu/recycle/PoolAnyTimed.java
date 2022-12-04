package dk.martinu.recycle;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntUnaryOperator;

/**
 * A {@code RetentionPolicy} that does not limit the amount of elements that
 * can be retained (pushed) in the stack, but disposes elements at fixed time
 * intervals.
 *
 * @author Adam Martinu
 * @see #PoolAnyTimed(long, IntUnaryOperator)
 * @since 1.0
 */
public class PoolAnyTimed extends RetentionPolicyTimed {

    /**
     * Constructs a new timed retention policy with the specified time interval
     * and operator.
     *
     * @param timeMs   time interval between disposals in milliseconds
     * @param operator operator to determine how many elements to dispose
     * @throws IllegalArgumentException if {@code timeMs < 1}
     * @throws NullPointerException     if {@code operator} is {@code null}
     */
    public PoolAnyTimed(final long timeMs, @NotNull final IntUnaryOperator operator) {
        super(timeMs, operator);
    }

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
