/**
 * Copyright (C) 2022 Christopher J. Stehno
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.cjstehno.ersatz.socket.cfg;

import io.github.cjstehno.ersatz.socket.encdec.Decoder;
import io.github.cjstehno.ersatz.socket.encdec.Encoder;
import org.hamcrest.Matcher;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface Interactions {

    /**
     * Configures a response encoder for a specific message type. If an encoder is configured for the same message type
     * at the server level, this encoder will override it.
     *
     * @param messageType the response message type to be encoded
     * @param encoder the encoder
     * @return a reference to this config
     */
    <T> Interactions encoder(Class<T> messageType, Encoder encoder);

    /**
     * Configures the decoder to be used. Due to the nature of the request data streams only one decoder may (and MUST)
     * be defined for decoding all incoming messages.
     *
     * If your scenario has a server that accepts multiple request formats with no commonality, you should create a
     * separate test case for each with its own decoder defined.
     *
     * If a decoder is defined at the server config level, this decoder will override it.
     *
     * @param messageType the request message type
     * @param decoder the decoder
     * @param <T> the message type
     * @return a reference to this config
     */
    <T> Interactions decoder(Class<T> messageType, Decoder<T> decoder);

    /**
     * Configures interactions to be performed when a client connects to the server. Only one such interaction is allowed.
     * If it is configured twice, the last one configured will be used.
     *
     * @param consumer the configuration consumer
     * @return a reference to this config
     */
    Interactions onConnect(Consumer<ConnectionContext> consumer);

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
