package dk.martinu.recycle.test;

import org.junit.jupiter.api.Test;

import dk.martinu.recycle.*;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Test class for recyclers in general.
 */
public class RecyclerTest {

    // asserts identity equality of objects retained in a recycler
    @Test
    public void identity() {
        final Recycler<Object> recycler = Recyclers.createLinear(
                Object.class, PoolAny.get(), Object::new);

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
}
