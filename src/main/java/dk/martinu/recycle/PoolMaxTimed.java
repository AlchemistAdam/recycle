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

import java.util.function.IntUnaryOperator;

/**
 * A {@code RetentionPolicy} that limits the amount of elements that
 * can be retained (pushed) in the stack, and disposes elements at a fixed time
 * interval.
 * <p>
 * <b>NOTE:</b> this class modifies the recycler stack asynchronously.
 * Instances of this class should only be used with recyclers that synchronize
 * access to their stack, such as {@link DefaultRecycler}.
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
    public void uninstall() {
        super.uninstall();
        count = 0;
    }
}
