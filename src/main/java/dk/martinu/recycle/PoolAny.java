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
 * A {@code RetentionPolicy} implementation that does not limit the amount of
 * elements that can be retained (pushed) in the stack. To obtain an instance
 * of this class, use {@link #get()}.
 *
 * @author Adam Martinu
 * @since 1.0
 */
public class PoolAny implements RetentionPolicy {

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
