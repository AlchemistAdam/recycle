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

import java.util.concurrent.atomic.AtomicInteger;

import dk.martinu.recycle.Recycler;
import dk.martinu.recycle.Recyclers;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for recyclers in general. Due to the close relation between
 * {@code Recycler} objects and {@code RecyclerStack} objects, this is
 * essentially a test for both classes.
 */
public class RecyclerTest {

    // asserts get() returns previously freed values
    @Test
    public void free() {
        final AtomicInteger ai = new AtomicInteger(10);
        final Recycler<Integer> recycler = Recyclers.createLinear(Integer.class, ai::getAndIncrement);

        final int i10 = recycler.get();
        final int i11 = recycler.get();
        final int i12 = recycler.get();

        recycler.free(i10);
        recycler.free(i11);
        recycler.free(i12);

        assertEquals(i12, recycler.get());
        assertEquals(i11, recycler.get());
        assertEquals(i10, recycler.get());
        assertEquals(13, recycler.get());
    }

    // same as free test but with limited bucket size
    @Test
    public void free_lim() {
        final AtomicInteger ai = new AtomicInteger(10);
        final Recycler<Integer> recycler = Recyclers.createLinear(Integer.class, 1, ai::getAndIncrement);

        final int i10 = recycler.get();
        final int i11 = recycler.get();
        final int i12 = recycler.get();

        recycler.free(i10);
        recycler.free(i11);
        recycler.free(i12);

        assertEquals(i12, recycler.get());
        assertEquals(i11, recycler.get());
        assertEquals(i10, recycler.get());
        assertEquals(13, recycler.get());
    }

    // asserts get() returns values from supplier
    @Test
    public void get() {
        final AtomicInteger ai = new AtomicInteger(10);
        final Recycler<Integer> recycler = Recyclers.createLinear(Integer.class, ai::getAndIncrement);

        assertEquals(10, recycler.get());
        assertEquals(11, recycler.get());
        assertEquals(12, recycler.get());
    }

    // asserts identity equality of objects retained in a recycler
    @Test
    public void identity() {
        final Recycler<Object> recycler = Recyclers.createLinear(Object.class, Object::new);

        // supply a new object
        final Object obj_0 = recycler.get();

        // supply another object
        final Object obj_1 = recycler.get();
        // assert identity is not equal
        assertNotSame(obj_0, obj_1);

        // push object 1 onto stack
        recycler.free(obj_1);
        // pop object 1 from stack
        final Object obj_2 = recycler.get();
        // assert identity is equal (no new object was supplied)
        assertSame(obj_1, obj_2);
    }

    // asserts recycler returns the correct size
    @Test
    public void size() {
        final Recycler<Object> recycler = Recyclers.createLinear(Object.class, Object::new);

        assertEquals(0, recycler.size());

        recycler.free(new Object());
        assertEquals(1, recycler.size());

        recycler.free(new Object());
        recycler.free(new Object());
        assertEquals(3, recycler.size());

        recycler.clear();
        assertEquals(0, recycler.size());
    }

    // same as size test but with limited bucket size
    @Test
    public void size_lim() {
        final Recycler<Object> recycler = Recyclers.createLinear(Object.class, 1, Object::new);

        assertEquals(0, recycler.size());

        recycler.free(new Object());
        assertEquals(1, recycler.size());

        recycler.free(new Object());
        recycler.free(new Object());
        assertEquals(3, recycler.size());

        recycler.clear();
        assertEquals(0, recycler.size());
    }
}
