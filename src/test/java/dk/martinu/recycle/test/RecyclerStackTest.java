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
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.util.NoSuchElementException;
import java.util.stream.Stream;

import dk.martinu.recycle.PoolAny;
import dk.martinu.recycle.RecyclerStack;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for recycler stacks.
 */
@DisplayName("RecyclerStack Test")
public class RecyclerStackTest {

    @ParameterizedTest
    @ArgumentsSource(RecyclerStackProvider.class)
    @DisplayName("is empty after clear")
    void emptyAfterClear(@NotNull final RecyclerStack<Integer> stack) {
        stack.push(0);
        stack.push(0);
        stack.push(0);
        assertFalse(stack.isEmpty());
        assertNotEquals(0, stack.size());

        stack.clear();
        assertTrue(stack.isEmpty());
        assertEquals(0, stack.size());
    }

    @ParameterizedTest
    @ArgumentsSource(RecyclerStackProvider.class)
    @DisplayName("is empty after clear (limited bucket size)")
    void emptyAfterClear_lim(@NotNull final RecyclerStack<Integer> stack) {
        stack.push(0);
        stack.push(0);
        stack.push(0);
        assertFalse(stack.isEmpty());
        assertNotEquals(0, stack.size());

        stack.clear();
        assertTrue(stack.isEmpty());
        assertEquals(0, stack.size());
    }

    @ParameterizedTest
    @ArgumentsSource(RecyclerStackProvider.class)
    @DisplayName("is empty when new")
    void isEmpty(@NotNull final RecyclerStack<Integer> stack) {
        assertTrue(stack.isEmpty());
        assertEquals(0, stack.size());
    }

    @ParameterizedTest
    @ArgumentsSource(RecyclerStackProvider.class)
    @DisplayName("is not empty after pushing")
    void isNotEmpty(@NotNull final RecyclerStack<Integer> stack) {
        stack.push(0);
        assertEquals(1, stack.size());

        stack.push(0);
        assertEquals(2, stack.size());

        stack.push(0);
        assertEquals(3, stack.size());
    }

    @ParameterizedTest
    @ArgumentsSource(RecyclerStackProvider.class)
    @DisplayName("is not empty after pushing (limited bucket size)")
    void isNotEmpty_lim(@NotNull final RecyclerStack<Integer> stack) {
        stack.push(0);
        assertEquals(1, stack.size());

        stack.push(0);
        assertEquals(2, stack.size());

        stack.push(0);
        assertEquals(3, stack.size());
    }

    @ParameterizedTest
    @ArgumentsSource(RecyclerStackProvider.class)
    @DisplayName("can pop")
    void pop(@NotNull final RecyclerStack<Integer> stack) {
        stack.push(0);
        stack.push(1);
        stack.push(2);
        assertFalse(stack.isEmpty());

        assertEquals(2, stack.pop());
        assertEquals(1, stack.pop());
        assertEquals(0, stack.pop());
        assertTrue(stack.isEmpty());
    }

    @ParameterizedTest
    @ArgumentsSource(RecyclerStackProvider.class)
    @DisplayName("pop() throws NoSuchElementException when empty")
    void popThrowsWhenEmpty(@NotNull final RecyclerStack<Integer> stack) {
        assertThrows(NoSuchElementException.class, stack::pop);
    }

    @ParameterizedTest
    @ArgumentsSource(RecyclerStackProvider.class)
    @DisplayName("can pop (limited bucket size)")
    void pop_lim(@NotNull final RecyclerStack<Integer> stack) {
        stack.push(0);
        stack.push(1);
        stack.push(2);
        assertFalse(stack.isEmpty());

        assertEquals(2, stack.pop());
        assertEquals(1, stack.pop());
        assertEquals(0, stack.pop());
        assertTrue(stack.isEmpty());
    }

    @ParameterizedTest
    @ArgumentsSource(RecyclerStackProvider.class)
    @DisplayName("can push")
    void push(@NotNull final RecyclerStack<Integer> stack) {
        stack.push(0);
        stack.push(0);
        stack.push(0);
        assertFalse(stack.isEmpty());
    }

    @ParameterizedTest
    @ArgumentsSource(RecyclerStackProvider.class)
    @DisplayName("can push (limited bucket size)")
    void push_lim(@NotNull final RecyclerStack<Integer> stack) {
        stack.push(0);
        stack.push(0);
        stack.push(0);
        assertFalse(stack.isEmpty());
    }

    @ParameterizedTest
    @ArgumentsSource(RecyclerStackProvider.class)
    @DisplayName("can remove when empty")
    void removeEmpty(@NotNull final RecyclerStack<Integer> stack) {
        stack.remove(10);
        assertTrue(stack.isEmpty());
    }

    @ParameterizedTest
    @ArgumentsSource(RecyclerStackProvider.class)
    @DisplayName("can remove with argument equal to size")
    void removeExact(@NotNull final RecyclerStack<Integer> stack) {
        stack.push(0);
        stack.push(0);
        stack.push(0);
        assertFalse(stack.isEmpty());

        stack.remove(3);
        assertTrue(stack.isEmpty());
        assertEquals(0, stack.size());
    }

    @ParameterizedTest
    @ArgumentsSource(RecyclerStackProvider.class)
    @DisplayName("can remove with argument equal to size (limited bucket size)")
    void removeExact_lim(@NotNull final RecyclerStack<Integer> stack) {
        stack.push(0);
        stack.push(0);
        stack.push(0);
        assertFalse(stack.isEmpty());

        stack.remove(3);
        assertTrue(stack.isEmpty());
        assertEquals(0, stack.size());
    }

    @ParameterizedTest
    @ArgumentsSource(RecyclerStackProvider.class)
    @DisplayName("can remove with argument greater than size")
    void removeGreater(@NotNull final RecyclerStack<Integer> stack) {
        stack.push(0);
        stack.push(0);
        stack.push(0);
        assertFalse(stack.isEmpty());

        stack.remove(10);
        assertTrue(stack.isEmpty());
    }

    @ParameterizedTest
    @ArgumentsSource(RecyclerStackProvider.class)
    @DisplayName("can remove with argument greater than size (limited bucket size)")
    void removeGreater_lim(@NotNull final RecyclerStack<Integer> stack) {
        stack.push(0);
        stack.push(0);
        stack.push(0);
        assertFalse(stack.isEmpty());

        stack.remove(10);
        assertTrue(stack.isEmpty());
    }

    @ParameterizedTest
    @ArgumentsSource(RecyclerStackProvider.class)
    @DisplayName("can remove with argument less than size")
    void removeLess(@NotNull final RecyclerStack<Integer> stack) {
        stack.push(0);
        stack.push(0);
        stack.push(0);
        assertFalse(stack.isEmpty());

        stack.remove(2);
        assertFalse(stack.isEmpty());
        assertEquals(1, stack.size());
    }

    @ParameterizedTest
    @ArgumentsSource(RecyclerStackProvider.class)
    @DisplayName("can remove with argument less than size (limited bucket size)")
    void removeLess_lim(@NotNull final RecyclerStack<Integer> stack) {
        stack.push(0);
        stack.push(0);
        stack.push(0);
        assertFalse(stack.isEmpty());

        stack.remove(2);
        assertFalse(stack.isEmpty());
        assertEquals(1, stack.size());
    }

    /**
     * {@code RecyclerStack<Integer>} argument provider for test methods.
     */
    static class RecyclerStackProvider implements ArgumentsProvider {

        /**
         * Provides a {@code RecyclerStack} argument with a limited bucket size
         * (1) if the test method's name ends in {@code "_lim"}, otherwise the
         * bucket size is 128.
         */
        @Override
        public Stream<? extends Arguments> provideArguments(@NotNull final ExtensionContext context) {
            final String methodName = context.getTestMethod().orElseThrow().getName();
            final RecyclerStack<Integer> stack;
            if (methodName.endsWith("_lim"))
                stack = new RecyclerStack<>(x -> new Integer[1], PoolAny.get());
            else
                stack = new RecyclerStack<>(x -> new Integer[128], PoolAny.get());

            return Stream.of(stack).map(Arguments::of);
        }
    }
}
