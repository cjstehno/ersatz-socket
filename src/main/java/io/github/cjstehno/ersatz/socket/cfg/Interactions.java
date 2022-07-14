/**
 * Copyright (C) 2022 Christopher J. Stehno
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.cjstehno.ersatz.socket.cfg;

import org.hamcrest.Matcher;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface Interactions {

    /**
     * Configures interactions to be performed when a client connects to the server. Only one such interaction is allowed.
     * If it is configured twice, the last one configured will be used.
     *
     * @param consumer the configuration consumer
     * @return a reference to this config
     */
    Interactions onConnect(Consumer<ConnectionContext> consumer);

    // TODO: onDisconnect() ?

    /**
     * Configures interactions to be performed when a client sends a request message to the server matching the
     * configured matcher. If the matcher matches the message, the consumer will called with the ConnectionContext and
     * the decoded message object.
     *
     * @param matching the matcher for the decoded message object
     * @param consumer the configuration consumer (called when matched)
     * @return a reference to this config
     * @param <T> the type of the message
     */
    <T> Interactions onMessage(Matcher<T> matching, BiConsumer<ConnectionContext, T> consumer);
}
