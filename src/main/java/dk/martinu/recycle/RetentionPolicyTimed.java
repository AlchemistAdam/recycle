package dk.martinu.recycle;

import org.jetbrains.annotations.*;

import java.util.Objects;
import java.util.function.IntUnaryOperator;

/**
 * Specialized extension of {@code RetentionPolicy} that disposes elements at a
 * fixed time interval. Two default implementations are provided by this API:
 * <ul>
 *     <li>{@link PoolAnyTimed}</li>
 *     <li>{@link PoolMaxTimed}</li>
 * </ul>
 * Extending this class allows for custom behavior of retaining elements.
 *
 * @author Adam Martinu
 * @since 1.0
 */
public abstract class RetentionPolicyTimed extends RetentionPolicy {

    /**
     * Time interval in milliseconds to wait between disposals.
     */
    public final long timeMs;
    /**
     * An operator that accepts the size of a {@link RecyclerStack} and returns
     * how many elements to dispose.
     */
    @NotNull
    public final IntUnaryOperator operator;
    /**
     * Thread used to dispose elements.
     */
    @Nullable
    protected Disposer disposer = null;

    /**
     * Constructs a new timed retention policy with the specified time interval
     * and operator.
     *
     * @param timeMs   time interval between disposals in milliseconds
     * @param operator operator to determine how many elements to dispose
     * @throws IllegalArgumentException if {@code timeMs < 1}
     * @throws NullPointerException     if {@code operator} is {@code null}
     */
    public RetentionPolicyTimed(final long timeMs, @NotNull final IntUnaryOperator operator) {
        if (timeMs < 1)
            throw new IllegalArgumentException("timeMs is less than 1 {" + timeMs + "}");
        this.timeMs = timeMs;
        this.operator = Objects.requireNonNull(operator, "operator is null");
    }

    /**
     * Constructs a new {@link Disposer} for the specified stack and starts it.
     *
     * @param stack the stack this retention policy is being installed for
     */
    @Contract()
    @Override
    protected void install(@NotNull final RecyclerStack<?> stack) {
        super.install(stack);
        disposer = new Disposer(stack, timeMs, operator);
        disposer.start();
    }

    /**
     * Terminates this retention policy's {@link Disposer}.
     */
    @Contract()
    @Override
    protected void uninstall() {
        super.uninstall();
        if (disposer != null) {
            disposer.terminate();
            disposer = null;
        }
    }
}
