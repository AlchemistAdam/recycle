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

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A simple stack implementation that stores elements in buckets (a collection
 * of arrays). Buckets are created and disposed as needed. The size of each
 * bucket is specified externally in the form of an {@link ArrayProducer} that
 * returns bucket arrays. In other words, the proportionality between bucket
 * sizes (e.g., linear, exponential) is <b>not</b> defined by this
 * implementation, but the creator of a {@code RecyclerStack} object supplying
 * the {@code ArrayProducer} instance.
 * <p>
 * <b>NOTE:</b> this implementation is not thread safe. Access to an instance
 * of this class must be synchronized externally if it is used concurrently.
 *
 * @param <T> the element type
 * @author Adam Martinu
 * @since 1.0
 */
public class RecyclerStack<T> {

    /**
     * Producer for new bucket arrays.
     */
    @NotNull
    protected final ArrayProducer<T> producer;
    /**
     * Controls retention behavior of elements.
     */
    @NotNull
    protected RetentionPolicy policy;
    /**
     * The current bucket to push and pop elements from.
     */
    @NotNull
    protected Bucket<T> bucket;
    /**
     * Index of the next array element in the current bucket.
     */
    protected int cursor = 0;
    /**
     * Number of buckets in the stack.
     */
    protected int bucketCount = 0;

    /**
     * Creates a new {@code RecyclerStack} object that uses the specified
     * producer to get bucket arrays. The specified policy determines how
     * elements are retained in the stack.
     *
     * @param producer producer that returns bucket arrays
     * @param policy   determines how elements are retained
     * @throws NullPointerException if {@code producer} or {@code policy} is
     *                              {@code null}
     */
    public RecyclerStack(@NotNull final ArrayProducer<T> producer, @NotNull final RetentionPolicy policy) {
        this.producer = Objects.requireNonNull(producer, "producer is null");
        this.policy = Objects.requireNonNull(policy, "policy is null");
        bucket = new Bucket<>(null, producer.get(bucketCount++));

        policy.install(this);
    }

    /**
     * Removes all elements from the stack.
     */
    public void clear() {

        final boolean didClear = !isEmpty();

        // iterate all buckets
        while (cursor != 0 || bucket.next != null) {

            // dispose bucket if not 1st
            if (bucket.next != null) {
                bucket = bucket.next;
                cursor = bucket.array.length;
                bucketCount--;
            }
            // empty bucket if 1st
            else {
                for (int i = 0; i < cursor; i++)
                    bucket.array[i] = null;
                cursor = 0;
            }
        }

        if (didClear)
            policy.onClear();
    }

    /**
     * Returns {@code true} if there are no elements in the stack, otherwise
     * {@code false} is returned.
     */
    @Contract(pure = true)
    public boolean isEmpty() {
        return cursor == 0 && bucket.next == null;
    }

    /**
     * Pops the next element from the stack and returns it.
     *
     * @return the next element
     * @throws NoSuchElementException if the stack is empty
     */
    public T pop() {

        // dispose current bucket if empty
        if (cursor == 0) {
            if (bucket.next != null) {
                bucket = bucket.next;
                cursor = bucket.array.length;
                bucketCount--;
            }
            else
                throw new NoSuchElementException("stack is empty");
        }

        // decrease cursor to position of element
        --cursor;

        // pop element
        T rv = bucket.array[cursor];
        bucket.array[cursor] = null;

        // notify retention policy
        policy.onPop();

        return rv;
    }

    /**
     * Pops up to {@code n} elements from the stack and returns the number of
     * popped elements. Elements that are popped are stored in the specified
     * array.
     *
     * @param array array to store popped elements in
     * @param n     number of elements to pop
     * @return the number of popped elements
     */
    @Contract(mutates = "param1")
    public int pop(final T[] array, int n) {
        if (n > array.length)
            n = array.length;
        int index = 0;

        // iterate all buckets for n > 0
        while (n > 0 && (cursor != 0 || bucket.next != null)) {

            // dispose current bucket if empty
            if (cursor == 0) {
                bucket = bucket.next;
                cursor = bucket.array.length;
                bucketCount--;
            }

            /*
            small optimization that uses arraycopy and disposes the bucket
            immediately, instead of stepping through the array and
            copying/nullifying every element
             */
            // pop all elements from this bucket
            if (n > cursor && bucket.next != null) {

                // copy (pop) all bucket elements
                System.arraycopy(bucket.array, 0, array, index, cursor);
                index += cursor;

                // decrease remaining pop count
                n -= cursor;

                // dispose this bucket
                bucket = bucket.next;
                cursor = bucket.array.length;
                bucketCount--;
            }

            // pop up to n elements from this bucket
            else {
                // number of elements to pop from bucket
                final int m = Math.min(cursor, n);

                // pop elements
                for (int i = cursor - m; i < cursor; i++) {
                    array[index++] = bucket.array[i];
                    bucket.array[i] = null;
                }

                // decrease cursor position
                cursor -= m;
                // decrease remaining pop count n
                n -= m;
            }
        }

        // notify retention policy
        if (index > 0)
            policy.onPop(index);

        return index;
    }

    /**
     * Pushes the specified element onto the stack.
     */
    public void push(final T element) {

        // check retention policy
        if (policy.canPush()) {

            // allocate new bucket if current is full
            if (cursor == bucket.array.length) {
                bucket = new Bucket<>(bucket, producer.get(bucketCount++));
                cursor = 0;
            }

            // push element
            bucket.array[cursor++] = element;

            // notify retention policy
            policy.onPush();
        }
    }

    /**
     * Pushes up to {@code n} elements onto the stack from the specified array.
     *
     * @param array the array containing elements to push
     * @param n     number of elements to push
     */
    public void push(final T[] array, int n) {
        if (n > array.length)
            n = array.length;
        int pushCount = 0;
        int index = 0;

        // check retention policy
        n = policy.canPush(n);

        while (pushCount < n) {

            // allocate new bucket if current is full
            if (cursor == bucket.array.length) {
                bucket = new Bucket<>(bucket, producer.get(bucketCount++));
                cursor = 0;
            }

            // push elements
            while (cursor < bucket.array.length && pushCount < n) {
                bucket.array[cursor++] = array[index++];
                pushCount++;
            }
        }

        // notify retention policy
        if (pushCount > 0)
            policy.onPush(pushCount);
    }

    /**
     * Removes up to {@code n} elements from the stack.
     */
    public void remove(int n) {

        // iterate all buckets for n > 0
        while (n > 0 && (cursor != 0 || bucket.next != null)) {

            // dispose current bucket if empty
            if (cursor == 0) {
                bucket = bucket.next;
                cursor = bucket.array.length;
                bucketCount--;
            }

            /*
            small optimization that disposes the bucket immediately, instead of
            stepping through the array and nullifying every element
             */
            // pop all elements from this bucket
            if (n > cursor && bucket.next != null) {

                // decrease remaining remove count
                n -= cursor;

                // dispose this bucket
                bucket = bucket.next;
                cursor = bucket.array.length;
                bucketCount--;
            }

            // pop up to n elements from this bucket
            else {
                // number of elements to remove from bucket
                final int m = Math.min(cursor, n);

                // remove elements
                for (int i = cursor - m; i < cursor; i++)
                    bucket.array[i] = null;

                // decrease cursor position
                cursor -= m;

                // decrease remove count
                n -= m;
            }
        }
    }

    /**
     * Installs the specified policy on this stack.
     *
     * @throws NullPointerException if {@code policy} is {@code null}
     */
    public void setRetentionPolicy(@NotNull final RetentionPolicy policy) {
        Objects.requireNonNull(policy, "policy is null");
        this.policy.uninstall();
        this.policy = policy;
        policy.install(this);
    }

    /**
     * Returns the number of elements in the stack.
     */
    public int size() {

        // initialize to elements in current bucket
        int size = cursor;

        // increment by size of full buckets, if any
        Bucket<T> bucket = this.bucket;
        while ((bucket = bucket.next) != null)
            size += bucket.array.length;

        return size;
    }

    /**
     * A node structure that has an array of elements and a reference to the
     * next link (bucket).
     *
     * @param <T> the array element type
     */
    @SuppressWarnings("ClassCanBeRecord") // class is internal
    protected static class Bucket<T> {

        /**
         * The next bucket, or {@code null}.
         */
        @Nullable
        public final Bucket<T> next;
        /**
         * The array of elements.
         */
        public final T[] array;

        /**
         * Creates a new bucket.
         *
         * @param next  the next bucket, or {@code null}
         * @param array the array to store elements in
         * @throws NullPointerException if {@code array} is {@code null}
         */
        public Bucket(@Nullable final Bucket<T> next, final T[] array) {
            this.next = next;
            this.array = Objects.requireNonNull(array, "array is null");
        }
    }
}
