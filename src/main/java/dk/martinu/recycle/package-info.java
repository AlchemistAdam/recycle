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