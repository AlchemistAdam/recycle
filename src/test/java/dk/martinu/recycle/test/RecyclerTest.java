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
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.*;
import org.junit.jupiter.params.provider.CsvSource;

import java.lang.annotation.*;
import java.util.concurrent.atomic.AtomicInteger;

import dk.martinu.recycle.Recycler;
import dk.martinu.recycle.Recyclers;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for recyclers.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Recycler Test")
public class RecyclerTest {

    @DisplayName("is empty after clear")
    @ParameterizedTest
    @CsvSource({
            "def",
            "lim"
    })
    void emptyAfterClear(@NotNull @RecyclerValue final Recycler<Integer> recycler) {
        // push elements
        recycler.retain(0);
        recycler.retain(0);
        recycler.retain(0);

        // pop all
        recycler.clear();
        assertEquals(0, recycler.size());
    }

    @DisplayName("can get array of values from stack")
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
    void getArrayFromStack(final int n, @NotNull @RecyclerValue final Recycler<Integer> recycler) {
        final Integer[] numbers = {0, 1, 2, 3};
        final Integer[] array = new Integer[numbers.length];
        final int popCount = Math.min(numbers.length, Math.max(n, 0));

        // push and pop
        recycler.retain(numbers);
        recycler.get(array, n);

        // assert correct number of elements are popped from recycler stack
        assertEquals(numbers.length - popCount, recycler.size());

        // assert array contains elements in pop range
        for (int i = 0; i < popCount; i++)
            assertNotNull(array[i], String.format("null element at index [%1$d]", i));

        // assert array contains null outside of pop range
        for (int i = popCount; i < array.length; i++)
            assertNull(array[i], String.format("not null element at index [%1$d]", i));
    }

    @DisplayName("can get values in array from supplier")
    @ParameterizedTest
    @CsvSource({
            "def",
            "lim"
    })
    void getArrayFromSupplier(@NotNull @RecyclerValue final Recycler<Integer> recycler) {
        assertArrayEquals(new Integer[] {0, 1, 2, 3}, recycler.get(new Integer[4]));
        assertArrayEquals(new Integer[] {4, 5, null, null}, recycler.get(new Integer[4], 2));
    }

    @DisplayName("can get values from stack")
    @ParameterizedTest
    @CsvSource({
            "def",
            "lim"
    })
    void getFromStack(@NotNull @RecyclerValue final Recycler<Integer> recycler) {
        // push 1 by 1
        recycler.retain(0);
        recycler.retain(1);
        recycler.retain(2);
        assertEquals(2, recycler.get());
        assertEquals(1, recycler.get());
        assertEquals(0, recycler.get());

        // push array
        recycler.retain(new Integer[] {3, 4, 5});
        assertEquals(5, recycler.get());
        assertEquals(4, recycler.get());
        assertEquals(3, recycler.get());
    }

    @DisplayName("can get values from supplier")
    @ParameterizedTest
    @CsvSource({
            "def",
            "lim"
    })
    void getFromSupplier(@NotNull @RecyclerValue final Recycler<Integer> recycler) {
        assertEquals(0, recycler.get());
        assertEquals(1, recycler.get());
        assertEquals(2, recycler.get());
    }

    @DisplayName("is empty when new")
    @ParameterizedTest
    @CsvSource({
            "def",
            "lim"
    })
    void isEmpty(@NotNull @RecyclerValue final Recycler<Integer> recycler) {
        assertEquals(0, recycler.size());
    }

    @DisplayName("can retain elements")
    @ParameterizedTest
    @CsvSource({
            "def",
            "lim"
    })
    void retain(@NotNull @RecyclerValue final Recycler<Integer> recycler) {
        recycler.retain(0);
        assertEquals(1, recycler.size());

        recycler.retain(0);
        assertEquals(2, recycler.size());

        recycler.retain(0);
        assertEquals(3, recycler.size());

        recycler.retain(new Integer[] {0, 0, 0, 0});
        assertEquals(7, recycler.size());

        recycler.retain(new Integer[] {0, 0, 0, 0}, 2);
        assertEquals(9, recycler.size());
    }

    @DisplayName("can retain elements from array")
    @ParameterizedTest
    @CsvSource({
            "def",
            "lim"
    })
    void retainArray(@NotNull @RecyclerValue final Recycler<Integer> recycler) {
        recycler.retain(new Integer[] {0, 0, 0, 0});
        assertEquals(4, recycler.size());

        recycler.retain(new Integer[] {0, 0, 0, 0}, 2);
        assertEquals(6, recycler.size());
    }

    @Target({ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    @ConvertWith(StringToRecyclerConverter.class)
    public @interface RecyclerValue { }

    @SuppressWarnings("rawtypes") // cannot select from parameterized class
    public static class StringToRecyclerConverter extends TypedArgumentConverter<String, Recycler> {

        protected StringToRecyclerConverter() {
            super(String.class, Recycler.class);
        }

        @Contract(value = "_ -> new", pure = true)
        @NotNull
        @Override
        protected Recycler<Integer> convert(final String source) throws ArgumentConversionException {
            final AtomicInteger ai = new AtomicInteger();
            return switch (source) {
                // default
                case "def" -> Recyclers.createConstant(Integer.class, ai::getAndIncrement);
                // limited bucket size
                case "lim" -> Recyclers.createConstant(Integer.class, 1, ai::getAndIncrement);
                // cannot convert to recycler
                default -> throw new ArgumentConversionException("invalid recycler source {" + source + "}");
            };
        }
    }
}
