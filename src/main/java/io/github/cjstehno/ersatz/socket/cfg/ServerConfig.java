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

import io.github.cjstehno.ersatz.socket.encdec.Decoder;
import io.github.cjstehno.ersatz.socket.encdec.Encoder;

import java.util.function.Consumer;

public interface ServerConfig {

    // avoid - 0 for ephemeral (Default)
    ServerConfig port(final int value);

    // generally use default (0) which creates cached threads
    ServerConfig workerThreads(final int value);

    ServerConfig autoStart(boolean enabled);

    ServerConfig encoder(final Class<?> messageType, final Encoder encoder);

    /**
     * FIXME: document
     * <p>
     * Only one decoder is allowed in a configuration. This is due to the lack of ability to determine whether
     * a decoder matches the incoming message without decoding it.
     * I feel that it is fair limitation at this point - if your server can accept different message types that
     * have no unified interface, you can simply write a separate test for each message type.
     *
     * @param messageType
     * @param decoder
     * @param <T>
     * @return
     */
    <T> ServerConfig decoder(final Class<T> messageType, final Decoder<T> decoder);

    ServerConfig interactions(final Consumer<Interactions> consumer);

    /// FIXME: reportToConsole
}
