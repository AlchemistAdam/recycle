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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

import dk.martinu.recycle.Recycler;
import dk.martinu.recycle.Recyclers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Test class for recyclers in general. Due to the close relation between
 * {@code Recycler} objects and {@code RecyclerStack} objects, this is
 * essentially a test for both classes.
 */
//@TestInstance(Lifecycle.PER_CLASS) TODO remove or uncomment
public class RecyclerTest {

    @ParameterizedTest
    @ArgumentsSource(RecyclerProvider.class)
    @DisplayName("empty after clear")
    void emptyAfterClear(@NotNull final Recycler<Object> recycler) {
        recycler.free(new Object());
        recycler.free(new Object());
        recycler.free(new Object());
        assertNotEquals(0, recycler.size());

        recycler.clear();
        assertEquals(0, recycler.size());
    }

    @ParameterizedTest
    @ArgumentsSource(RecyclerProvider.class)
    @DisplayName("empty after clear (limited bucket size)")
    void emptyAfterClear_lim(@NotNull final Recycler<Object> recycler) {
        recycler.free(new Object());
        recycler.free(new Object());
        recycler.free(new Object());
        assertNotEquals(0, recycler.size());

        recycler.clear();
        assertEquals(0, recycler.size());
    }

    @Test
    @DisplayName("get value from stack")
    void getFromStack() {
        final AtomicInteger ai = new AtomicInteger(10);
        final Recycler<Integer> recycler = Recyclers.createLinear(Integer.class, ai::getAndIncrement);

        final int i0 = ai.getAndIncrement();
        final int i1 = ai.getAndIncrement();
        final int i2 = ai.getAndIncrement();
        assertNotEquals(i0, i1);
        assertNotEquals(i0, i2);
        assertNotEquals(i1, i2);

        recycler.free(i0);
        recycler.free(i1);
        recycler.free(i2);
        assertEquals(i2, recycler.get());
        assertEquals(i1, recycler.get());
        assertEquals(i0, recycler.get());
    }

    @Test
    @DisplayName("get value from stack (limited bucket size)")
    void getFromStack_lim() {
        final AtomicInteger ai = new AtomicInteger(0);
        final Recycler<Integer> recycler = Recyclers.createLinear(Integer.class, 1, ai::getAndIncrement);

        final int i0 = ai.getAndIncrement();
        final int i1 = ai.getAndIncrement();
        final int i2 = ai.getAndIncrement();
        assertNotEquals(i0, i1);
        assertNotEquals(i0, i2);
        assertNotEquals(i1, i2);

        recycler.free(i0);
        recycler.free(i1);
        recycler.free(i2);
        assertEquals(i2, recycler.get());
        assertEquals(i1, recycler.get());
        assertEquals(i0, recycler.get());
    }

    @Test
    @DisplayName("get value from supplier")
    void getFromSupplier() {
        final AtomicInteger ai = new AtomicInteger(0);
        final Recycler<Integer> recycler = Recyclers.createLinear(Integer.class, ai::getAndIncrement);

        assertEquals(0, recycler.get());
        assertEquals(1, recycler.get());
        assertEquals(2, recycler.get());
    }

    @ParameterizedTest
    @ArgumentsSource(RecyclerProvider.class)
    @DisplayName("get value from supplier and stack")
    void getFromSupplierAndStack(@NotNull final Recycler<Object> recycler) {
        final Object obj0 = recycler.get();
        final Object obj1 = recycler.get();
        final Object obj2 = recycler.get();

        assertNotEquals(obj0, obj1);
        assertNotEquals(obj0, obj2);
        assertNotEquals(obj1, obj2);

        recycler.free(obj0);
        recycler.free(obj1);
        recycler.free(obj2);

        assertEquals(obj2, recycler.get());
        assertEquals(obj1, recycler.get());
        assertEquals(obj0, recycler.get());
    }

    @ParameterizedTest
    @ArgumentsSource(RecyclerProvider.class)
    @DisplayName("is empty when new")
    void isEmpty(@NotNull final Recycler<Object> recycler) {
        assertEquals(0, recycler.size());
    }


    @ParameterizedTest
    @ArgumentsSource(RecyclerProvider.class)
    @DisplayName("not empty after pushing elements")
    void notEmpty(@NotNull final Recycler<Object> recycler) {
        recycler.free(new Object());
        assertEquals(1, recycler.size());

        recycler.free(new Object());
        assertEquals(2, recycler.size());

        recycler.free(new Object());
        assertEquals(3, recycler.size());
    }

    @ParameterizedTest
    @ArgumentsSource(RecyclerProvider.class)
    @DisplayName("not empty after pushing elements (limited bucket size)")
    void notEmpty_lim(@NotNull final Recycler<Object> recycler) {
        recycler.free(new Object());
        assertEquals(1, recycler.size());

        recycler.free(new Object());
        assertEquals(2, recycler.size());

        recycler.free(new Object());
        assertEquals(3, recycler.size());
    }

    /**
     * {@code Recycler} provider for test methods.
     */
    static class RecyclerProvider implements ArgumentsProvider {

        /**
         * Provides a {@code Recycler} argument with a limited bucket size (1)
         * if the test method's name ends in {@code "_lim"}, otherwise the
         * default bucket size is used.
         *
         * @see Recyclers#createLinear(Class, Supplier)
         */
        @Override
        public Stream<? extends Arguments> provideArguments(@NotNull final ExtensionContext context) {
            final String methodName = context.getTestMethod().orElseThrow().getName();
            final Recycler<?> recycler;
            if (methodName.endsWith("_lim"))
                recycler = Recyclers.createLinear(Object.class, 1, Object::new);
            else
                recycler = Recyclers.createLinear(Object.class, Object::new);

            return Stream.of(recycler).map(Arguments::of);
        }
    }
}
