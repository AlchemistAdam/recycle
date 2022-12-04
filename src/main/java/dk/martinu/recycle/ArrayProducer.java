package dk.martinu.recycle;

import org.jetbrains.annotations.NotNull;

/**
 * Functional interface that returns an array with a component type {@code T}.
 *
 * @param <T> the component type
 * @author Adam Martinu
 * @since 1.0
 */
@FunctionalInterface
public interface ArrayProducer<T> {

    @NotNull
    T[] get();
}
