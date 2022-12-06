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
 * A {@code RetentionPolicy} that does not limit the amount of elements that
 * can be retained (pushed) in the stack, but disposes elements a a fixed time
 * interval.
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
