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

/**
 * Abstract class to determine how to retain elements in
 * {@link Recycler}/{@link RecyclerStack} objects. Several default
 * implementations are provided by this API:
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
public interface  RetentionPolicy {

    /**
     * Returns {@code true} if an element can be pushed onto the stack,
     * otherwise {@code false}. This method will be called by
     * {@link RecyclerStack#push(Object)} to determine if the element should be
     * pushed or not.
     */
    boolean canPush();

    /**
     * Event method called by {@link RecyclerStack} whenever an element is
     * popped. Does nothing by default.
     */
    @Contract(pure = true)
    default void onPop() { }

    /**
     * Event method called by {@link RecyclerStack} whenever an element
     * is pushed. Does nothing by default.
     */
    @Contract(pure = true)
    default void onPush() { }

    /**
     * Called by {@link RecyclerStack} when constructing an instance or setting
     * a new retention policy. Does nothing by default.
     *
     * @param stack the stack this policy is being installed for
     * @see RecyclerStack#setRetentionPolicy(RetentionPolicy)
     */
    @Contract(pure = true)
    default void install(@NotNull final RecyclerStack<?> stack) { }

    /**
     * Called by {@link RecyclerStack} when setting a new retention policy.
     * Does nothing by default.
     *
     * @see RecyclerStack#setRetentionPolicy(RetentionPolicy)
     */
    @Contract(pure = true)
    default void uninstall() { }
}
