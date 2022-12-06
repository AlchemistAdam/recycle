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

import org.junit.jupiter.api.Test;

import dk.martinu.recycle.PoolAny;
import dk.martinu.recycle.RecyclerStack;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test class for recycler stacks. Due to the close relation between
 * {@code Recycler} objects and {@code RecyclerStack} objects, most tests
 * related to stacks are done in {@link RecyclerTest}.
 */
public class RecyclerStackTest {

    // asserts remove() works as intended
    @Test
    public void remove() {
        final RecyclerStack<Object> stack = new RecyclerStack<>(() -> new Object[128], PoolAny.get());

        // assert method can be called on empty stack
        assertDoesNotThrow(() -> {
            stack.remove(10);
        });

        stack.push(new Object());
        stack.push(new Object());
        stack.push(new Object());
        assertEquals(3, stack.size());

        // assert method can be called on non-empty stack
        assertDoesNotThrow(() -> stack.remove(10));
        assertEquals(0, stack.size());

        stack.push(new Object());
        stack.push(new Object());
        stack.push(new Object());
        stack.push(new Object());
        assertEquals(4, stack.size());

        // assert method removes correct number of elements
        assertDoesNotThrow(() -> stack.remove(1));
        assertEquals(3, stack.size());
        assertDoesNotThrow(() -> stack.remove(2));
        assertEquals(1, stack.size());
    }

    // same as remove test but with limited bucket size
    @Test
    public void remove_lim() {
        final RecyclerStack<Object> stack = new RecyclerStack<>(() -> new Object[1], PoolAny.get());

        // assert method can be called on empty stack
        assertDoesNotThrow(() -> {
            stack.remove(10);
        });

        stack.push(new Object());
        stack.push(new Object());
        stack.push(new Object());
        assertEquals(3, stack.size());

        // assert method can be called on non-empty stack
        assertDoesNotThrow(() -> stack.remove(10));
        assertEquals(0, stack.size());

        stack.push(new Object());
        stack.push(new Object());
        stack.push(new Object());
        stack.push(new Object());
        assertEquals(4, stack.size());

        // assert method removes correct number of elements
        assertDoesNotThrow(() -> stack.remove(1));
        assertEquals(3, stack.size());
        assertDoesNotThrow(() -> stack.remove(2));
        assertEquals(1, stack.size());
    }
}
