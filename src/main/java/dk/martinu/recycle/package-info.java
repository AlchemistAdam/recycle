/**
 * Contains classes and interfaces for the base Recycler API.
 * <p>
 * To use the API, obtain a {@code Recycler} from one of the static factory
 * methods in {@link dk.martinu.recycle.Recyclers}, or write your own
 * implementation by implementing the {@link dk.martinu.recycle.Recycler}
 * interface. See the interface description for an example on how to use it.
 * <p>
 * The behavior of {@code Recycler} instances can be controlled with
 * {@link dk.martinu.recycle.RetentionPolicy} objects, of which this API
 * provides several. See the class description for a list of default
 * implementations.
 *
 * @version 1.0
 * @since 1.0
 */
package dk.martinu.recycle;