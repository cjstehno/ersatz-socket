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
import org.hamcrest.Matcher;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface Interactions {

    // FIXME: called(n)
    // FIXME: listeners

    <T> Interactions encoder(Class<T> messageType, Encoder encoder);

    <T> Interactions decoder(Class<T> messageType, Decoder<T> decoder);

    // FIXME: document - when a client connects this consumer will be triggered with teh connection context
    // this should only be called once (or it will override)
    Interactions onConnect(Consumer<ConnectionContext> consumer);

    <T> Interactions onMessage(Matcher<T> matching, BiConsumer<ConnectionContext, T> consumer);
}
