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

/**
 * Static factory class for creating various {@link Recycler} instances.
 * <p>
 * This factory can create the following types of recyclers:
 * <ul>
 *     <li>{@link #createConstant(Class, int, RetentionPolicy, Supplier) Constant}</li>
 *     <li>{@link #createLinear(Class, int, int, RetentionPolicy, Supplier) Linear}</li>
 *     <li>{@link #createExponential(Class, int, double, RetentionPolicy, Supplier) Exponential}</li>
 * </ul>
 * Additionally, each factory method is overloaded to accept a
 * {@link RetentionPolicy} instance to further control how the recycler
 * operates.
 * <p>
 * All recyclers created by this factory are thread safe and can be used
 * concurrently and with {@link RetentionPolicyTimed timed} retention policies.
 *
 * @author Adam Martinu
 * @since 1.0
 */
public class Recyclers {

    /**
     * Returns a new recycler with a constant bucket size of {@code 128} and a
     * {@link PoolAny} retention policy.
     *
     * @param componentType the class of the elements stored in the recycler
     * @param supplier      supplier for providing elements when the recycler
     *                      is empty.
     * @param <T>           the element type
     * @return a new recycler with a constant bucket size
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
     * Returns a new recycler with a constant bucket size of {@code 128} and
     * the specified retention policy.
     *
     * @param componentType the class of the elements stored in the recycler
     * @param policy        determines how elements are retained
     * @param supplier      supplier for providing elements when the recycler
     *                      is empty.
     * @param <T>           the element type
     * @return a new recycler with a constant bucket size
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
     * Returns a new recycler with the specified constant bucket size and a
     * {@link PoolAny} retention policy.
     *
     * @param componentType the class of the elements stored in the recycler
     * @param bucketSize    the size of each bucket in the recycler
     * @param supplier      supplier for providing elements when the recycler
     *                      is empty.
     * @param <T>           the element type
     * @return a new recycler with a constant bucket size
     * @throws IllegalArgumentException if {@code bucketSize} is less than
     *                                  {@code 1}
     * @throws NullPointerException     if {@code componentType} or
     *                                  {@code supplier} is {@code null}
     */
    @Contract(value = "_, _, _ -> new", pure = true)
    @NotNull
    public static <T> Recycler<T> createConstant(@NotNull final Class<T> componentType, final int bucketSize,
            @NotNull final Supplier<T> supplier) {
        return createConstant(componentType, bucketSize, PoolAny.get(), supplier);
    }

    /**
     * Returns a new recycler with the specified constant bucket size and
     * retention policy.
     *
     * @param componentType the class of the elements stored in the recycler
     * @param bucketSize    the size of each bucket in the recycler
     * @param policy        determines how elements are retained
     * @param supplier      supplier for providing elements when the recycler
     *                      is empty.
     * @param <T>           the element type
     * @return a new recycler with a constant bucket size
     * @throws NullPointerException if {@code componentType}, {@code policy} or
     *                              {@code supplier} is {@code null}
     */
    @Contract(value = "_, _, _, _ -> new", pure = true)
    @NotNull
    public static <T> Recycler<T> createConstant(@NotNull final Class<T> componentType, final int bucketSize,
            @NotNull final RetentionPolicy policy, @NotNull final Supplier<T> supplier) {
        return new DefaultRecycler<>(new ConstantProducer<>(componentType, bucketSize), policy, supplier);
    }

    /**
     * Returns a new recycler with a bucket size that increases exponentially,
     * using a default coefficient and base of {@code 64} and {@code 1.25d},
     * and a {@link PoolAny} retention policy. The size of each bucket is
     * calculated as
     * <pre>
     *     f(x) = abˣ
     * </pre>
     * where {@code a} and {@code b} are the coefficient and the base,
     * respectively.
     *
     * @param componentType the class of the elements stored in the recycler
     * @param supplier      supplier for providing elements when the recycler
     *                      is empty.
     * @param <T>           the element type
     * @return a new recycler with a bucket size that increases exponentially
     * @throws NullPointerException if {@code componentType} or
     *                              {@code supplier} is {@code null}
     */
    @Contract(value = "_, _ -> new", pure = true)
    @NotNull
    public static <T> Recycler<T> createExponential(@NotNull final Class<T> componentType,
            @NotNull final Supplier<T> supplier) {
        return createExponential(componentType, 64, 1.25d, PoolAny.get(), supplier);
    }

    /**
     * Returns a new recycler with a bucket size that increases exponentially,
     * using a default coefficient and base of {@code 64} and {@code 1.25d},
     * and the specified retention policy. The size of each bucket is
     * calculated as
     * <pre>
     *     f(x) = abˣ
     * </pre>
     * where {@code a} and {@code b} are the coefficient and the base,
     * respectively.
     *
     * @param componentType the class of the elements stored in the recycler
     * @param policy        determines how elements are retained
     * @param supplier      supplier for providing elements when the recycler
     *                      is empty.
     * @param <T>           the element type
     * @return a new recycler with a bucket size that increases exponentially
     * @throws NullPointerException if {@code componentType}, {@code policy} or
     *                              {@code supplier} is {@code null}
     */
    @Contract(value = "_, _, _ -> new", pure = true)
    @NotNull
    public static <T> Recycler<T> createExponential(@NotNull final Class<T> componentType,
            @NotNull final RetentionPolicy policy, @NotNull final Supplier<T> supplier) {
        return createExponential(componentType, 64, 1.25d, policy, supplier);
    }

    /**
     * Returns a new recycler with a bucket size that increases exponentially
     * and a {@link PoolAny} retention policy. The size of each bucket is
     * calculated as
     * <pre>
     *     f(x) = abˣ
     * </pre>
     * where {@code a} and {@code b} are the coefficient and the base,
     * respectively.
     *
     * @param componentType the class of the elements stored in the recycler
     * @param coefficient   the coefficient to the exponentiation
     * @param base          the base of the exponentiation
     * @param supplier      supplier for providing elements when the recycler
     *                      is empty.
     * @param <T>           the element type
     * @return a new recycler with a bucket size that increases exponentially
     * @throws NullPointerException     if {@code componentType} or
     *                                  {@code supplier} is {@code null}
     * @throws IllegalArgumentException if {@code coefficient < 1} or
     *                                  {@code base < 1}
     */
    @Contract(value = "_, _, _, _ -> new", pure = true)
    @NotNull
    public static <T> Recycler<T> createExponential(@NotNull final Class<T> componentType, final int coefficient,
            final double base, @NotNull final Supplier<T> supplier) {
        return createExponential(componentType, coefficient, base, PoolAny.get(), supplier);
    }

    /**
     * Returns a new recycler with a bucket size that increases exponentially
     * and the specified retention policy. The size of each bucket is
     * calculated as
     * <pre>
     *     f(x) = abˣ
     * </pre>
     * where {@code a} and {@code b} are the coefficient and the base,
     * respectively.
     *
     * @param componentType the class of the elements stored in the recycler
     * @param coefficient   the coefficient to the exponentiation
     * @param base          the base of the exponentiation
     * @param policy        determines how elements are retained
     * @param supplier      supplier for providing elements when the recycler
     *                      is empty.
     * @param <T>           the element type
     * @return a new recycler with a bucket size that increases exponentially
     * @throws NullPointerException     if {@code componentType},
     *                                  {@code policy} or {@code supplier} is
     *                                  {@code null}
     * @throws IllegalArgumentException if {@code coefficient < 1} or
     *                                  {@code base < 1}
     */
    @Contract(value = "_, _, _, _, _ -> new", pure = true)
    @NotNull
    public static <T> Recycler<T> createExponential(@NotNull final Class<T> componentType, final int coefficient,
            final double base, @NotNull final RetentionPolicy policy, @NotNull final Supplier<T> supplier) {
        return new DefaultRecycler<>(new ExponentialProducer<>(componentType, coefficient, base), policy, supplier);
    }

    /**
     * Returns a new recycler with a bucket size that increases linearly, using
     * a default slope and intercept of {@code 64}, and a {@link PoolAny}
     * retention policy. The size of each bucket is calculated as
     * <pre>
     *     f(x) = ax + b
     * </pre>
     * where {@code a} and {@code b} are the slope and the intercept,
     * respectively.
     *
     * @param componentType the class of the elements stored in the recycler
     * @param supplier      supplier for providing elements when the recycler
     *                      is empty.
     * @param <T>           the element type
     * @return a new recycler with a bucket size that increases linearly
     * @throws NullPointerException if {@code componentType} or
     *                              {@code supplier} is {@code null}
     */
    @Contract(value = "_, _ -> new", pure = true)
    @NotNull
    public static <T> Recycler<T> createLinear(@NotNull final Class<T> componentType,
            @NotNull final Supplier<T> supplier) {
        return createLinear(componentType, 64, 64, PoolAny.get(), supplier);
    }

    /**
     * Returns a new recycler with a bucket size that increases linearly, using
     * a default slope and intercept of {@code 64}, and the specified retention
     * policy. The size of each bucket is calculated as
     * <pre>
     *     f(x) = ax + b
     * </pre>
     * where {@code a} and {@code b} are the slope and the intercept,
     * respectively.
     *
     * @param componentType the class of the elements stored in the recycler
     * @param policy        determines how elements are retained
     * @param supplier      supplier for providing elements when the recycler
     *                      is empty.
     * @param <T>           the element type
     * @return a new recycler with a bucket size that increases linearly
     * @throws NullPointerException if {@code componentType}, {@code policy} or
     *                              {@code supplier} is {@code null}
     */
    @Contract(value = "_, _, _ -> new", pure = true)
    @NotNull
    public static <T> Recycler<T> createLinear(@NotNull final Class<T> componentType,
            @NotNull final RetentionPolicy policy, @NotNull final Supplier<T> supplier) {
        return createLinear(componentType, 64, 64, policy, supplier);
    }

    /**
     * Returns a new recycler with a bucket size that increases linearly and a
     * {@link PoolAny} retention policy. The size of each bucket is calculated as
     * <pre>
     *     f(x) = ax + b
     * </pre>
     * where {@code a} and {@code b} are the slope and the intercept,
     * respectively.
     *
     * @param componentType the class of the elements stored in the recycler
     * @param slope         the slope of a linear line
     * @param intercept     the intercept of a linear line
     * @param supplier      supplier for providing elements when the recycler
     *                      is empty.
     * @param <T>           the element type
     * @return a new recycler with a bucket size that increases linearly
     * @throws NullPointerException     if {@code componentType} or
     *                                  {@code supplier} is {@code null}
     * @throws IllegalArgumentException if {@code slope < 1} or
     *                                  {@code intercept < 1}
     */
    @Contract(value = "_, _, _, _ -> new", pure = true)
    @NotNull
    public static <T> Recycler<T> createLinear(@NotNull final Class<T> componentType, final int slope,
            final int intercept, @NotNull final Supplier<T> supplier) {
        return createLinear(componentType, slope, intercept, PoolAny.get(), supplier);
    }

    /**
     * Returns a new recycler with a bucket size that increases linearly and
     * the specified retention policy. The size of each bucket is calculated as
     * <pre>
     *     f(x) = ax + b
     * </pre>
     * where {@code a} and {@code b} are the slope and the intercept,
     * respectively.
     *
     * @param componentType the class of the elements stored in the recycler
     * @param slope         the slope of a linear line
     * @param intercept     the intercept of a linear line
     * @param policy        determines how elements are retained
     * @param supplier      supplier for providing elements when the recycler
     *                      is empty.
     * @param <T>           the element type
     * @return a new recycler with a bucket size that increases linearly
     * @throws NullPointerException     if {@code componentType},
     *                                  {@code policy} or {@code supplier} is
     *                                  {@code null}
     * @throws IllegalArgumentException if {@code slope < 1} or
     *                                  {@code intercept < 1}
     */
    @Contract(value = "_, _, _, _, _ -> new", pure = true)
    @NotNull
    public static <T> Recycler<T> createLinear(@NotNull final Class<T> componentType, final int slope,
            final int intercept, @NotNull final RetentionPolicy policy, @NotNull final Supplier<T> supplier) {
        return new DefaultRecycler<>(new LinearProducer<>(componentType, slope, intercept), policy, supplier);
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
        @Override
        public T[] get(final int x) {
            //noinspection unchecked
            return (T[]) Array.newInstance(componentType, bucketSize);
        }
    }

    /**
     * Exponential producer of bucket arrays for a {@link RecyclerStack}. The
     * length of produced arrays is calculated as
     * <pre>
     *     f(x) = abˣ
     * </pre>
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
         * {@code a} in the expression
         * <pre>
         *     f(x) = abˣ
         * </pre>
         * where the result of {@code f(x)} is the array length.
         */
        public final int coefficient;
        /**
         * The base when calculating the array length. Equal to {@code a} in
         * <pre>
         *     f(x) = abˣ
         * </pre>
         * where the result of {@code f(x)} is the array length.
         */
        public final double base;

        /**
         * Constructs a new producer that returns arrays increasing
         * exponentially in size. Given a parameter {@code x}, the array length
         * is calculated as
         * <pre>
         *     f(x) = abˣ
         * </pre>
         * where {@code a} and {@code b} are the coefficient and the base,
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
        @Contract(value = "_ -> new", pure = true)
        @Override
        public T[] get(final int x) {
            final int bucketSize = coefficient * (int) Math.pow(base, x);
            //noinspection unchecked
            return (T[]) Array.newInstance(componentType, bucketSize);
        }
    }

    /**
     * Linear producer of bucket arrays for a {@link RecyclerStack}. The
     * length of produced arrays is calculated as
     * <pre>
     *     f(x) = ax + b
     * </pre>
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
         * The slope (coefficient) when calculating the array length. Equal to
         * {@code a} in
         * <pre>
         *     f(x) = ax + b
         * </pre>
         * where the result of {@code f(x)} is the array length.
         */
        public final int slope;
        /**
         * The intercept when calculating the array length. Equal to {@code b}
         * in
         * <pre>
         *     f(x) = ax + b
         * </pre>
         * where the result of {@code f(x)} is the array length. The length of
         * the first array will be equal to this value.
         */
        public final int intercept;

        /**
         * Constructs a new producer that returns arrays increasing linearly in
         * size. Given a parameter {@code x}, the array length is calculated as
         * <pre>
         *     f(x) = ax + b
         * </pre>
         * where {@code a} and {@code b} are the slope and the intercept
         * respectively.
         *
         * @param componentType the array component type
         * @param slope         The slope when calculating the array length
         * @param intercept     The intercept when calculating the array
         *                      length
         * @throws NullPointerException     if {@code componentType} is
         *                                  {@code null}
         * @throws IllegalArgumentException if {@code slope < 1} or
         *                                  {@code intercept < 1}
         */
        public LinearProducer(@NotNull final Class<T> componentType, final int slope, final int intercept) {
            this.componentType = Objects.requireNonNull(componentType, "componentType is null");
            if (slope < 1)
                throw new IllegalArgumentException("slope is less than 1");
            if (intercept < 1)
                throw new IllegalArgumentException("intercept is less than 1");
            this.slope = slope;
            this.intercept = intercept;
        }

        /**
         * Returns a new array with a length equal to
         * <pre>
         *     slope * x + intercept
         * </pre>
         */
        @Contract(value = "_ -> new", pure = true)
        @Override
        public T[] get(final int x) {
            final int bucketSize = slope * x + intercept;
            //noinspection unchecked
            return (T[]) Array.newInstance(componentType, bucketSize);
        }
    }
}
