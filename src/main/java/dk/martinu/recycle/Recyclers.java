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

import java.lang.reflect.Array;
import java.util.Objects;
import java.util.function.Supplier;

// TODO implement exponential recycler

/**
 * Static factory class for creating various {@link Recycler} instances.
 * <p>
 * This factory can create the following types of recyclers:
 * <ul>
 *     <li>Linear</li>
 * </ul>
 * Additionally, each factory method is overloaded to accept a
 * {@link RetentionPolicy} instance to further control how the recycler
 * operates.
 *
 * @author Adam Martinu
 * @since 1.0
 */
public class Recyclers {

    /**
     * Returns a new recycler that grows linearly, has a bucket size of
     * {@code 128} and uses a {@link PoolAny} retention policy.
     *
     * @param componentType the class of the elements stored in the recycler
     * @param supplier      supplier for providing elements when the recycler
     *                      is empty.
     * @param <T>           the element type
     * @return a new recycler that grows linearly
     * @throws NullPointerException if {@code componentType} or
     *                              {@code supplier} is {@code null}
     */
    @Contract(value = "_, _ -> new", pure = true)
    @NotNull
    public static <T> Recycler<T> createLinear(@NotNull final Class<T> componentType,
            @NotNull final Supplier<T> supplier) {
        return createLinear(componentType, 128, PoolAny.get(), supplier);
    }

    /**
     * Returns a new recycler that grows linearly, has a bucket size of
     * {@code 128} and uses the specified retention policy.
     *
     * @param componentType the class of the elements stored in the recycler
     * @param policy        determines how elements are retained
     * @param supplier      supplier for providing elements when the recycler
     *                      is empty.
     * @param <T>           the element type
     * @return a new recycler that grows linearly
     * @throws NullPointerException if {@code componentType}, {@code policy} or
     *                              {@code supplier} is {@code null}
     */
    @Contract(value = "_, _, _ -> new", pure = true)
    @NotNull
    public static <T> Recycler<T> createLinear(@NotNull final Class<T> componentType,
            @NotNull final RetentionPolicy policy, @NotNull final Supplier<T> supplier) {
        return createLinear(componentType, 128, policy, supplier);
    }

    /**
     * Returns a new recycler that grows linearly and has the specified bucket
     * size.
     *
     * @param componentType the class of the elements stored in the recycler
     * @param bucketSize    the size of each bucket in the recycler
     * @param supplier      supplier for providing elements when the recycler
     *                      is empty.
     * @param <T>           the element type
     * @return a new recycler that grows linearly
     * @throws IllegalArgumentException if {@code bucketSize} is less than
     *                                  {@code 1}
     * @throws NullPointerException     if {@code componentType} or
     *                                  {@code supplier} is {@code null}
     */
    @Contract(value = "_, _, _ -> new", pure = true)
    @NotNull
    public static <T> Recycler<T> createLinear(@NotNull final Class<T> componentType, final int bucketSize,
            @NotNull final Supplier<T> supplier) {
        return new DefaultRecycler<>(new LinearProducer<>(componentType, bucketSize), PoolAny.get(), supplier);
    }

    /**
     * Returns a new recycler that grows linearly, has the specified bucket
     * size and uses the specified retention policy.
     *
     * @param componentType the class of the elements stored in the recycler
     * @param bucketSize    the size of each bucket in the recycler
     * @param policy        determines how elements are retained
     * @param supplier      supplier for providing elements when the recycler
     *                      is empty.
     * @param <T>           the element type
     * @return a new recycler that grows linearly
     * @throws IllegalArgumentException if {@code bucketSize} is less than
     *                                  {@code 1}
     * @throws NullPointerException     if {@code componentType},
     *                                  {@code policy} or {@code supplier} is
     *                                  {@code null}
     */
    @Contract(value = "_, _, _, _ -> new", pure = true)
    @NotNull
    public static <T> Recycler<T> createLinear(@NotNull final Class<T> componentType, final int bucketSize,
            @NotNull final RetentionPolicy policy, @NotNull final Supplier<T> supplier) {
        return new DefaultRecycler<>(new LinearProducer<>(componentType, bucketSize), policy, supplier);
    }

    /**
     * Linear producer of bucket arrays for a {@link RecyclerStack}. All arrays
     * produced have the exact same length, and as a result the capacity of the
     * stack will grow linearly.
     *
     * @param <T> the element type
     */
    @SuppressWarnings("ClassCanBeRecord") // class is internal
    protected static class LinearProducer<T> implements ArrayProducer<T> {

        /**
         * The array component type.
         */
        @NotNull
        public final Class<T> componentType;
        /**
         * The size (array length) of each bucket.
         */
        public final int bucketSize;

        /**
         * Constructs a new producer that returns equally sized arrays.
         *
         * @param componentType the array component type
         * @param bucketSize    the size (array length) of each bucket
         * @throws NullPointerException     if {@code componentType} is
         *                                  {@code null}
         * @throws IllegalArgumentException if {@code bucketSize < 1}
         */
        public LinearProducer(@NotNull final Class<T> componentType, final int bucketSize) {
            this.componentType = Objects.requireNonNull(componentType, "componentType is null");
            if (bucketSize < 1)
                throw new IllegalArgumentException("bucketSize is less than 1");
            this.bucketSize = bucketSize;
        }

        /**
         * Returns a new array with a length equal to {@link #bucketSize}.
         */
        @Contract(value = "-> new", pure = true)
        @NotNull
        @Override
        public T[] get() {
            //noinspection unchecked
            return (T[]) Array.newInstance(componentType, bucketSize);
        }
    }
}
