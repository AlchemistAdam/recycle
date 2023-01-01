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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.*;
import org.junit.jupiter.params.provider.CsvSource;

import java.lang.annotation.*;
import java.util.NoSuchElementException;

import dk.martinu.recycle.PoolAny;
import dk.martinu.recycle.RecyclerStack;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for recycler stacks.
 */
@DisplayName("RecyclerStack Test")
public class RecyclerStackTest {

    @DisplayName("is empty after clear")
    @ParameterizedTest
    @CsvSource({
            "def",
            "lim"
    })
    void emptyAfterClear(@NotNull @StackValue final RecyclerStack<Integer> stack) {
        stack.push(0);
        stack.push(0);
        stack.push(0);

        stack.clear();
        assertTrue(stack.isEmpty());
        assertEquals(0, stack.size());
    }

    @DisplayName("is empty when new")
    @ParameterizedTest
    @CsvSource({
            "def",
            "lim"
    })
    void isEmpty(@NotNull @StackValue final RecyclerStack<Integer> stack) {
        assertTrue(stack.isEmpty());
        assertEquals(0, stack.size());
    }

    @DisplayName("can pop")
    @ParameterizedTest
    @CsvSource({
            "def",
            "lim"
    })
    void pop(@NotNull @StackValue final RecyclerStack<Integer> stack) {
        // push 1 by 1
        stack.push(0);
        stack.push(1);
        stack.push(2);
        assertEquals(2, stack.pop());
        assertEquals(1, stack.pop());
        assertEquals(0, stack.pop());
        assertTrue(stack.isEmpty());
        assertEquals(0, stack.size());

        // push array
        stack.push(new Integer[] {3, 4, 5}, 3);
        assertEquals(5, stack.pop());
        assertEquals(4, stack.pop());
        assertEquals(3, stack.pop());
        assertTrue(stack.isEmpty());
        assertEquals(0, stack.size());
    }

    @DisplayName("can pop array")
    @ParameterizedTest
    @CsvSource({
            "-1, def", // negative
            " 6, def", // greater than array.length
            // default
            "0, def",
            "2, def",
            "4, def",
            // limited
            "0, lim",
            "2, lim",
            "4, lim",
    })
    void popArray(final int n, @NotNull @StackValue final RecyclerStack<Integer> stack) {
        final Integer[] numbers = {0, 1, 2, 3};
        final Integer[] array = new Integer[numbers.length];
        final int popCount = Math.min(numbers.length, Math.max(n, 0));

        // push and pop
        stack.push(numbers, numbers.length);
        stack.pop(array, n);

        // assert correct number of elements are popped from stack
        assertEquals(numbers.length - popCount, stack.size());
        if (numbers.length - popCount == 0)
            assertTrue(stack.isEmpty());

        // assert array contains elements in pop range
        for (int i = 0; i < popCount; i++)
            assertNotNull(array[i], String.format("null element at index [%1$d]", i));

        // assert array contains null outside of pop range
        for (int i = popCount; i < array.length; i++)
            assertNull(array[i], String.format("not null element at index [%1$d]", i));
    }

    @DisplayName("pop throws NoSuchElementException when empty")
    @ParameterizedTest
    @CsvSource({
            "def",
            "lim"
    })
    void popThrowsWhenEmpty(@NotNull @StackValue final RecyclerStack<Integer> stack) {
        assertThrows(NoSuchElementException.class, stack::pop);
    }

    @DisplayName("can push")
    @ParameterizedTest
    @CsvSource({
            "def",
            "lim"
    })
    void push(@NotNull @StackValue final RecyclerStack<Integer> stack) {
        stack.push(0);
        assertFalse(stack.isEmpty());
        assertEquals(1, stack.size());

        stack.push(0);
        assertFalse(stack.isEmpty());
        assertEquals(2, stack.size());

        stack.push(0);
        assertFalse(stack.isEmpty());
        assertEquals(3, stack.size());
    }

    @DisplayName("can push array")
    @ParameterizedTest
    @CsvSource({
            "-1, def", // negative
            " 6, def", // greater than array.length
            // default
            "0, def",
            "2, def",
            "4, def",
            // limited
            "0, lim",
            "2, lim",
            "4, lim",
    })
    void pushArray(final int n, @NotNull @StackValue final RecyclerStack<Integer> stack) {
        final Integer[] array = {0, 0, 0, 0};
        final int pushCount = Math.min(array.length, Math.max(n, 0));

        stack.push(array, n);
        assertEquals(pushCount, stack.size());
    }

    @DisplayName("can remove")
    @ParameterizedTest
    @CsvSource({
            "-1, def", // negative
            " 6, def", // greater than array.length
            // default
            "0, def",
            "2, def",
            "4, def",
            // limited
            "0, lim",
            "2, lim",
            "4, lim",
    })
    void remove(final int n, @NotNull @StackValue final RecyclerStack<Integer> stack) {
        final Integer[] array = {0, 1, 2, 3};
        final int removeCount = Math.min(array.length, Math.max(n, 0));

        stack.push(array, 4);
        stack.remove(n);

        // assert correct number of elements are removed from stack
        assertEquals(array.length - removeCount, stack.size());
        if (array.length - removeCount == 0)
            assertTrue(stack.isEmpty());
    }

    @Target({ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    @ConvertWith(StringToStackConverter.class)
    public @interface StackValue { }

    @SuppressWarnings("rawtypes") // cannot select from parameterized class
    public static class StringToStackConverter extends TypedArgumentConverter<String, RecyclerStack> {

        protected StringToStackConverter() {
            super(String.class, RecyclerStack.class);
        }

        @Contract(value = "_ -> new", pure = true)
        @NotNull
        @Override
        protected RecyclerStack<Integer> convert(final String source) throws ArgumentConversionException {
            return switch (source) {
                // default
                case "def" -> new RecyclerStack<>(x -> new Integer[128], PoolAny.get());
                // limited bucket size
                case "lim" -> new RecyclerStack<>(x -> new Integer[1], PoolAny.get());
                // cannot convert to stack
                default -> throw new ArgumentConversionException("invalid stack source {" + source + "}");
            };
        }
    }
}
