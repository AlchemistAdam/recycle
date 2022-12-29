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

package dk.martinu.recycle.test;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

import dk.martinu.recycle.Recycler;
import dk.martinu.recycle.Recyclers;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for recyclers.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Recycler Test")
public class RecyclerTest {

    @ParameterizedTest
    @ArgumentsSource(RecyclerProvider.class)
    @DisplayName("is empty after clear")
    void emptyAfterClear(@NotNull final Recycler<Integer> recycler) {
        recycler.retain(0);
        recycler.retain(0);
        recycler.retain(0);
        assertNotEquals(0, recycler.size());

        recycler.clear();
        assertEquals(0, recycler.size());
    }

    @ParameterizedTest
    @ArgumentsSource(RecyclerProvider.class)
    @DisplayName("is empty after clear (limited bucket size)")
    void emptyAfterClear_lim(@NotNull final Recycler<Integer> recycler) {
        recycler.retain(0);
        recycler.retain(0);
        recycler.retain(0);
        assertNotEquals(0, recycler.size());

        recycler.clear();
        assertEquals(0, recycler.size());
    }

    @ParameterizedTest
    @ArgumentsSource(RecyclerProvider.class)
    @DisplayName("can get array of values from stack")
    void getArrayFromStack(@NotNull final Recycler<Integer> recycler) {
        final Integer[] numbers = {0, 1, 2, 3};
        final Integer[] rv = new Integer[numbers.length];

        recycler.retain(numbers);

        recycler.get(rv);
        assertEquals(0, recycler.size());
        for (int i = 0; i < rv.length; i++) {
            final int index = i;
            assertNotNull(rv[i], () -> String.format("null element at index [%1$d]", index));
        }
    }

    @ParameterizedTest
    @ArgumentsSource(RecyclerProvider.class)
    @DisplayName("can get array of values from stack (limited bucket size)")
    void getArrayFromStack_lim(@NotNull final Recycler<Integer> recycler) {
        final Integer[] numbers = {0, 1, 2, 3};
        final Integer[] rv = new Integer[numbers.length];

        recycler.retain(numbers);

        recycler.get(rv);
        assertEquals(0, recycler.size());
        for (int i = 0; i < rv.length; i++) {
            final int index = i;
            assertNotNull(rv[i], () -> String.format("null element at index [%1$d]", index));
        }
    }

    @ParameterizedTest
    @ArgumentsSource(RecyclerProvider.class)
    @DisplayName("can get values from stack")
    void getFromStack(@NotNull final Recycler<Integer> recycler) {
        recycler.retain(0);
        recycler.retain(1);
        recycler.retain(2);

        assertEquals(2, recycler.get());
        assertEquals(1, recycler.get());
        assertEquals(0, recycler.get());
    }

    @ParameterizedTest
    @ArgumentsSource(RecyclerProvider.class)
    @DisplayName("can get values from stack (limited bucket size)")
    void getFromStack_lim(@NotNull final Recycler<Integer> recycler) {
        recycler.retain(0);
        recycler.retain(1);
        recycler.retain(2);

        assertEquals(2, recycler.get());
        assertEquals(1, recycler.get());
        assertEquals(0, recycler.get());
    }

    @ParameterizedTest
    @ArgumentsSource(RecyclerProvider.class)
    @DisplayName("can get values from supplier")
    void getFromSupplier(@NotNull final Recycler<Integer> recycler) {
        assertEquals(0, recycler.get());
        assertEquals(1, recycler.get());
        assertEquals(2, recycler.get());
    }

    @ParameterizedTest
    @ArgumentsSource(RecyclerProvider.class)
    @DisplayName("is empty when new")
    void isEmpty(@NotNull final Recycler<Integer> recycler) {
        assertEquals(0, recycler.size());
    }


    @ParameterizedTest
    @ArgumentsSource(RecyclerProvider.class)
    @DisplayName("is not empty after retaining elements")
    void notEmpty(@NotNull final Recycler<Integer> recycler) {
        recycler.retain(0);
        assertEquals(1, recycler.size());

        recycler.retain(0);
        assertEquals(2, recycler.size());

        recycler.retain(0);
        assertEquals(3, recycler.size());

        recycler.retain(new Integer[] {0, 0, 0, 0});
        assertEquals(7, recycler.size());
    }

    @ParameterizedTest
    @ArgumentsSource(RecyclerProvider.class)
    @DisplayName("is not empty after retaining elements (limited bucket size)")
    void notEmpty_lim(@NotNull final Recycler<Integer> recycler) {
        recycler.retain(0);
        assertEquals(1, recycler.size());

        recycler.retain(0);
        assertEquals(2, recycler.size());

        recycler.retain(0);
        assertEquals(3, recycler.size());
    }

    /**
     * {@code Recycler<Integer>} argument provider for test methods.
     */
    static class RecyclerProvider implements ArgumentsProvider {

        /**
         * Provides a {@code Recycler} argument with a limited bucket size (1)
         * if the test method's name ends in {@code "_lim"}, otherwise the
         * default bucket size is used.
         *
         * @see Recyclers#createConstant(Class, Supplier)
         */
        @Override
        public Stream<? extends Arguments> provideArguments(@NotNull final ExtensionContext context) {
            final String methodName = context.getTestMethod().orElseThrow().getName();
            final Recycler<?> recycler;
            final AtomicInteger ai = new AtomicInteger();
            if (methodName.endsWith("_lim"))
                recycler = Recyclers.createConstant(Integer.class, 1, ai::getAndIncrement);
            else
                recycler = Recyclers.createConstant(Integer.class, ai::getAndIncrement);

            return Stream.of(recycler).map(Arguments::of);
        }
    }
}
