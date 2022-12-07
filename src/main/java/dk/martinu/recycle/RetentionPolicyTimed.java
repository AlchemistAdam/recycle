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
 * <p>
 * <b>NOTE:</b> this class modifies the recycler stack asynchronously.
 * Instances of this class should only be used with recyclers that synchronize
 * access to their stack, such as {@link DefaultRecycler}.
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
