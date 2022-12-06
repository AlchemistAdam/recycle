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

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Basic implementation of a {@link Recycler} that provides functionality for
 * the {@code free} and {@code get} methods.
 * <p>
 * The {@link Recyclers} factory class can create simple recyclers that fit
 * most use cases. Instantiate recyclers from this class directly when finer
 * control and more advanced behaviour is required.
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
        stack.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void free(final T element) {
        stack.push(element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T get() {
        if (stack.isEmpty())
            return supplier.get();
        else
            return stack.pop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return stack.size();
    }
}
