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

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Basic implementation of a {@link Recycler} that delegates most method calls
 * to its underlying stack, except {@code get} which returns an element from
 * the supplier when the stack is empty
 * <p>
 * This implementation is thread safe and can be used concurrently.
 *
 * @param <T> the element type
 * @author Adam Martinu
 * @see Recyclers
 * @since 1.0
 */
public class DefaultRecycler<T> implements Recycler<T> {

    /**
     * The stack to operate on.
     */
    @NotNull
    protected final RecyclerStack<T> stack;
    /**
     * The supplier of elements.
     */
    @NotNull
    protected final Supplier<T> supplier;

    /**
     * Constructs a new recycler with the specified array producer and element
     * supplier.
     *
     * @param producer producer of bucket arrays
     * @param policy   determines how elements are retained
     * @param supplier supplier for providing elements when the recycler
     * @throws NullPointerException if {@code producer}, {@code policy} or
     *                              {@code supplier} is {@code null}
     */
    public DefaultRecycler(@NotNull final ArrayProducer<T> producer, @NotNull final RetentionPolicy policy,
            @NotNull final Supplier<T> supplier) {
        this.stack = new RecyclerStack<>(producer, policy);
        this.supplier = Objects.requireNonNull(supplier, "supplier is null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        synchronized (stack) {
            stack.clear();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T get() {
        synchronized (stack) {
            if (stack.isEmpty())
                return supplier.get();
            else
                return stack.pop();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Contract(value = "_, _ -> param1", mutates = "param1")
    @Override
    public T[] get(final T[] array, int n) {
        if (n > array.length)
            n = array.length;
        final int popCount;
        synchronized (stack) {
            popCount = stack.pop(array, n);
        }
        if (popCount < n) {
            for (int i = popCount; i < n; i++)
                array[i] = supplier.get();
        }
        return array;
    }

    /**
     * {@inheritDoc}
     */
    @Contract(pure = true)
    @NotNull
    @Override
    public RecyclerStack<?> getStack() {
        return stack;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void retain(final T[] array, final int n) {
        synchronized (stack) {
            stack.push(array, n);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void retain(final T element) {
        synchronized (stack) {
            stack.push(element);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Contract(pure = true)
    @Override
    public int size() {
        synchronized (stack) {
            return stack.size();
        }
    }
}
