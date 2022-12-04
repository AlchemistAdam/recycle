package dk.martinu.recycle;

/**
 * The base interface of a recycler. Recyclers are similar to stacks; both are
 * collections of elements, and allow the collection to be modified with
 * push and pop operations. Recyclers differ from stacks when attempting to
 * pop an element when the collection is empty. Instead of throwing an
 * exception or returning a default value, recyclers delegate the task of
 * returning a value elsewhere. The push and pop operations of a recycler are
 * named {@code free} and {@code get} respectively.
 * <p>
 * Recyclers are, as the name implies, meant to be used as a collection of
 * reusable and short-lived objects to reduce memory upkeep by using the same
 * objects multiple times instead of creating new ones.
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
 *     // recycle point object so it can be reused
 *     recycler.free(point);
 * </pre>
 * Note that it is perfectly legal to free objects that where not retrieved
 * from a recycler with {@code get()}, in some cases it might even be
 * preferable to do so. It is, however, highly recommended freeing all objects
 * retrieved from a recycler as they would otherwise get garbage collected, and
 * thus defeat the purpose of a recycler.
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
     * Marks the specified element as recycled, meaning it is no longer in use
     * and is free to be reused.
     *
     * @param element the object to recycle
     */
    void free(T element);

    /**
     * Returns a potentially recycled element.
     */
    T get();

    /**
     * Returns the number of available elements in the recycler.
     */
    int size();
}
