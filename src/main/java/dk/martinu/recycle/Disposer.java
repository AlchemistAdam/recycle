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

package dk.martinu.recycle;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.IntUnaryOperator;

/**
 * Daemon {@code Thread} implementation used by retention policies that dispose
 * elements at a fixed time interval.
 *
 * @author Adam Martinu
 * @see RetentionPolicyTimed
 * @since 1.0
 */
public class Disposer extends Thread {

    /**
     * Reference to the stack whose elements will be disposed.
     */
    @NotNull
    public final RecyclerStack<?> stack;
    /**
     * Time interval in milliseconds to wait between disposals.
     */
    public final long timeMs;
    /**
     * An operator that accepts the size of a {@link RecyclerStack} and returns
     * how many elements to dispose.
     */
    @NotNull
    public final IntUnaryOperator operator;
    /**
     * Boolean flag that keeps this thread looping  while {@code true}. Set to
     * {@code false} with {@link #terminate()}.
     */
    protected volatile boolean run = true;

    /**
     * Constructs a new {@code Disposer} to dispose elements from the specified
     * stack. The {@code Disposer} will at least {@code timeMs} between
     * disposals and use {@code operator} to determine how many elements to
     * dispose.
     *
     * @param stack    the stack whose elements will be disposed
     * @param timeMs   time interval between disposals in milliseconds
     * @param operator operator to determine how many elements to dispose
     * @throws IllegalArgumentException if {@code timeMs < 1}
     * @throws NullPointerException     if {@code stack} or {@code operator} is
     *                                  {@code null}
     */
    public Disposer(@NotNull final RecyclerStack<?> stack, final long timeMs, @NotNull final IntUnaryOperator operator) {
        this.stack = Objects.requireNonNull(stack, "stack is null");
        if (timeMs < 1)
            throw new IllegalArgumentException("timeMs is less than 1 {" + timeMs + "}");
        this.timeMs = timeMs;
        this.operator = Objects.requireNonNull(operator, "operator is null");

        setDaemon(true);
    }

    /**
     * Disposes elements at fixed intervals in a loop. Will keep running until
     * {@link #terminate()} is called.
     */
    @Override
    public void run() {
        long time = System.currentTimeMillis();
        long delta;
        while (run) {
            delta = System.currentTimeMillis() - time;
            if (delta >= timeMs) {
                // double-check run, might be set to false in RetentionPolicy.uninstall()
                if (run)
                    synchronized (stack) {
                        stack.remove(operator.applyAsInt(stack.size()));
                    }
                time = System.currentTimeMillis();
            }
            else
                synchronized (this) {
                    try {
                        wait(timeMs - delta);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
        }
    }

    /**
     * Terminates this disposer, preventing any elements from being disposed
     * after this call. Does nothing if this {@code Disposer} is already
     * terminated.
     */
    public void terminate() {
        if (!run)
            return;

        synchronized (this) {
            if (run) {
                run = false;
                notifyAll();
            }
        }
    }
}
