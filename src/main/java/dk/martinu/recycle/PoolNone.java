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

/**
 * A {@code RetentionPolicy} that does not allow any elements to be retained
 * (pushed) in the stack. This can be used to temporarily disable recycling of
 * objects without circumventing the use of a {@link Recycler}. To obtain an
 * instance of this class, use {@link #get()}.
 *
 * @author Adam Martinu
 * @since 1.0
 */
public class PoolNone implements RetentionPolicy {

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
    @Contract(value = "-> false", pure = true)
    @Override
    public boolean canPush() {
        return false;
    }

    /**
     * Returns {@code 0}; This retention policy does not allow elements to be
     * pushed.
     */
    @Contract(pure = true)
    @Override
    public int canPush(final int n) {
        return 0;
    }

    /**
     * Removes all elements from the specified stack; This retention policy
     * does not allow elements to be retained.
     */
    @Contract()
    @Override
    public void install(@NotNull final RecyclerStack<?> stack) {
        stack.clear();
    }
}
