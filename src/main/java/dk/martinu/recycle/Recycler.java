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

import java.util.Objects;

/**
 * The base interface of a recycler. Recyclers are similar to stacks; both are
 * collections of elements, and allow the collection to be modified with
 * push and pop operations. Recyclers differ from stacks when attempting to
 * pop an element when the collection is empty. Instead of throwing an
 * exception or returning a default value, recyclers delegate the task of
 * returning a value elsewhere. The push and pop operations of a recycler are
 * named {@code retain} and {@code get} respectively.
 * <p>
 * Recyclers are, as the name implies, meant to be used as a collection of
 * reusable and short-lived objects to reduce memory upkeep by reusing objects
 * multiple times instead of creating new ones.
 * <p>
 * The following example demonstrates how to use a {@code Recycler}:
 * <pre>
 *     // create Recycler
 *     Recycler&lt;Point&gt; recycler = ...
 *
 *     // get a potentially recycled Point object
 *     Point point = recycler.get();
 *
 *     // do something with point here
 *     ...
 *
 *     // retain point object so it can be reused
 *     recycler.retain(point);
 * </pre>
 * Note that it is perfectly legal to retain objects that where not retrieved
 * from a recycler with {@code get()}. Objects retrieved from a recycler should
 * always be retained after use. Otherwise, they would get garbage collected,
 * and thus defeat the purpose of a recycler.
 *
 * @param <T> the element type
 * @author Adam Martinu
 * @see Recyclers
 * @since 1.0
 */
public interface Recycler<T> {

    /**
     * Removes all elements in the recycler.
     */
    void clear();

    /**
     * Returns a potentially recycled element.
     */
    T get();

    /**
     * Fills the specified array with potentially recycled elements and returns
     * it.
     *
     * @param array the array to fill
     * @return the array passed to this method
     * @throws NullPointerException if {@code array} is {@code null}
     */
    @Contract(value = "null -> fail; _ -> param1", mutates = "param1")
    default T[] get(final T[] array) {
        Objects.requireNonNull(array, "array is null");
        return get(array, array.length);
    }

    /**
     * Fills the specified array with {@code n} potentially recycled elements
     * and returns it.
     *
     * @param array the array to fill
     * @param n     the number of elements to get
     * @return the array passed to this method
     * @throws NullPointerException if {@code array} is {@code null}
     */
    @Contract(value = "null, _ -> fail; _, _ -> param1", mutates = "param1")
    T[] get(final T[] array, final int n);

    /**
     * Returns the underlying stack this recycler operates on.
     * <p>
     * <b>NOTE:</b> the {@code RecyclerStack} implementation is not
     * synchronized and should not be used concurrently.
     */
    @Contract(pure = true)
    @NotNull
    RecyclerStack<?> getStack();

    /**
     * Retains all elements in the specified array.
     *
     * @param array array of elements to retain
     * @throws NullPointerException if {@code array} is {@code null}
     */
    @Contract(value = "null -> fail")
    default void retain(final T[] array) {
        Objects.requireNonNull(array, "array is null");
        retain(array, array.length);
    }

    /**
     * Retains {@code n} elements from the specified array.
     *
     * @param array array of elements to retain
     * @param n     number of elements to retain
     * @throws NullPointerException if {@code array} is {@code null}
     */
    @Contract(value = "null, _ -> fail")
    void retain(final T[] array, final int n);

    /**
     * Retains the specified element so it can be reused.
     *
     * @param element the object to retain
     */
    void retain(T element);

    /**
     * Installs the specified policy on this recycler's underlying stack.
     *
     * @throws NullPointerException if {@code policy} is {@code null}
     */
    void setRetentionPolicy(@NotNull final RetentionPolicy policy);

    /**
     * Returns the number of retained elements in the recycler.
     */
    @Contract(pure = true)
    @Range(from = 0, to = Integer.MAX_VALUE)
    int size();
}
