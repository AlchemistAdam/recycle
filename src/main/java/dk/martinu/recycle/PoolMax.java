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

/**
 * A {@code RetentionPolicy} that limits the amount of elements that
 * can be retained (pushed) in the stack.
 *
 * @author Adam Martinu
 * @see #PoolMax(int)
 * @since 1.0
 */
public class PoolMax extends RetentionPolicy {

    /**
     * Maximum number of elements that can be retained
     */
    public final int size;
    /**
     * Number of currently retained elements in the stack.
     */
    protected int count = 0;

    /**
     * Constructs a new retention policy with the specified maximum pool size.
     *
     * @param size maximum number of elements that can be retained
     * @throws IllegalArgumentException if {@code size < 1}
     */
    public PoolMax(final int size) {
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
     * Resets the element counter.
     */
    @Override
    protected void uninstall() {
        super.uninstall();
        count = 0;
    }
}
