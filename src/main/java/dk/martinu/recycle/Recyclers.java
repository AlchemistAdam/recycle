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
    public static <T> Recycler<T> createConstant(@NotNull final Class<T> componentType,
            @NotNull final Supplier<T> supplier) {
        return createConstant(componentType, 128, PoolAny.get(), supplier);
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
    public static <T> Recycler<T> createConstant(@NotNull final Class<T> componentType,
            @NotNull final RetentionPolicy policy, @NotNull final Supplier<T> supplier) {
        return createConstant(componentType, 128, policy, supplier);
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
    public static <T> Recycler<T> createConstant(@NotNull final Class<T> componentType, final int bucketSize,
            @NotNull final Supplier<T> supplier) {
        return new DefaultRecycler<>(new ConstantProducer<>(componentType, bucketSize), PoolAny.get(), supplier);
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
    public static <T> Recycler<T> createConstant(@NotNull final Class<T> componentType, final int bucketSize,
            @NotNull final RetentionPolicy policy, @NotNull final Supplier<T> supplier) {
        return new DefaultRecycler<>(new ConstantProducer<>(componentType, bucketSize), policy, supplier);
    }

    /**
     * Exponential producer of bucket arrays for a {@link RecyclerStack}. The
     * length of produced arrays will increase exponentially.
     *
     * @param <T> the element type
     */
    @SuppressWarnings("ClassCanBeRecord") // class is internal
    protected static class ExponentialProducer<T> implements ArrayProducer<T> {

        /**
         * The array component type.
         */
        @NotNull
        public final Class<T> componentType;
        /**
         * The coefficient when calculating the array length. Equal to
         * {@code b} in the expression
         * <pre>
         *     y = b * a^x
         * </pre>
         * where {@code y} is the array length.
         */
        public final int coefficient;
        /**
         * The base when calculating the array length. Equal to {@code a} in
         * the expression
         * <pre>
         *     y = b * a^x
         * </pre>
         * where {@code y} is the array length.
         */
        public final double base;

        /**
         * Constructs a new producer that returns arrays which grow
         * exponentially. Given a parameter {@code x}, the array length
         * {@code y} is defined as
         * <pre>
         *     y = b * a ^ x
         * </pre>
         * where {@code b} and {@code a} are the coefficient and the base,
         * respectively.
         *
         * @param componentType the array component type
         * @param coefficient   The coefficient when calculating the array
         *                      length
         * @param base          The base when calculating the array length
         * @throws NullPointerException     if {@code componentType} is
         *                                  {@code null}
         * @throws IllegalArgumentException if {@code coefficient < 1} or
         *                                  {@code base < 1}
         */
        public ExponentialProducer(@NotNull final Class<T> componentType, final int coefficient, final double base) {
            this.componentType = Objects.requireNonNull(componentType, "componentType is null");
            if (coefficient < 1)
                throw new IllegalArgumentException("coefficient is less than 1");
            if (base < 1)
                throw new IllegalArgumentException("base is less than 1");
            this.coefficient = coefficient;
            this.base = base;
        }

        /**
         * Returns a new array with a length equal to
         * <pre>
         *     coefficient * (int) Math.pow(base, x)
         * </pre>
         */
        @NotNull
        @Override
        public T[] get(final int x) {
            // y = b * a ^ x
            final int bucketSize = coefficient * (int) Math.pow(base, x);
            //noinspection unchecked
            return (T[]) Array.newInstance(componentType, bucketSize);
        }
    }


    /**
     * Constant producer of bucket arrays for a {@link RecyclerStack}. All
     * arrays produced have the exact same length.
     *
     * @param <T> the element type
     */
    @SuppressWarnings("ClassCanBeRecord") // class is internal
    protected static class ConstantProducer<T> implements ArrayProducer<T> {

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
        public ConstantProducer(@NotNull final Class<T> componentType, final int bucketSize) {
            this.componentType = Objects.requireNonNull(componentType, "componentType is null");
            if (bucketSize < 1)
                throw new IllegalArgumentException("bucketSize is less than 1");
            this.bucketSize = bucketSize;
        }

        /**
         * Returns a new array with a length equal to {@link #bucketSize}.
         */
        @Contract(value = "_ -> new", pure = true)
        @NotNull
        @Override
        public T[] get(final int x) {
            //noinspection unchecked
            return (T[]) Array.newInstance(componentType, bucketSize);
        }
    }
}
