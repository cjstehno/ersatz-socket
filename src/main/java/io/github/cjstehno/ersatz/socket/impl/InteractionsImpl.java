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
package io.github.cjstehno.ersatz.socket.impl;

import io.github.cjstehno.ersatz.socket.cfg.ConnectionContext;
import io.github.cjstehno.ersatz.socket.cfg.Interactions;
import io.github.cjstehno.ersatz.socket.encdec.Decoder;
import io.github.cjstehno.ersatz.socket.encdec.Encoder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hamcrest.Matcher;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class InteractionsImpl implements Interactions {

    private static final Consumer<ConnectionContext> NO_OP_CONNECT_INTERACTION = (ctx) -> {
        // no-op by default
    };
    private final ServerConfigImpl serverConfig;
    private final Map<Class<?>, Encoder> encoders = new HashMap<>();
    private Decoder<?> decoder;
    @Getter private Consumer<ConnectionContext> connectInteraction = NO_OP_CONNECT_INTERACTION;
    private final Map<Matcher<?>, BiConsumer<ConnectionContext, Object>> messageInteractions = new LinkedHashMap<>();

    @Override public Interactions onConnect(final Consumer<ConnectionContext> consumer) {
        connectInteraction = consumer;
        return this;
    }

    @Override public <T> Interactions onMessage(Matcher<T> matching, BiConsumer<ConnectionContext, T> consumer) {
        messageInteractions.put(matching, (BiConsumer<ConnectionContext, Object>) consumer);
        return this;
    }

    // overwrites
    @Override public <T> Interactions encoder(final Class<T> messageType, final Encoder encoder) {
        encoders.put(messageType, encoder);
        return this;
    }

    // overwrites
    @Override public <T> Interactions decoder(final Class<T> messageType, final Decoder<T> decoder) {
        this.decoder = decoder;
        return this;
    }

    public Optional<Encoder> findEncoder(final Class<?> type) {
        if (encoders.containsKey(type)) {
            return Optional.of(encoders.get(type));
        } else {
            return serverConfig.findEncoder(type);
        }
    }

    public Optional<Decoder<?>> decoder() {
        if (decoder != null) {
            return Optional.of(decoder);
        } else {
            return serverConfig.decoder();
        }
    }

    public Optional<BiConsumer<ConnectionContext, Object>> findMessageInteraction(final Object message) {
        ///  find the interaction with matcher matching message
        return messageInteractions.entrySet().stream()
            .filter(ent -> ent.getKey().matches(message))
            .map(Map.Entry::getValue)
            .findAny();
    }

    public void reset() {
        connectInteraction = NO_OP_CONNECT_INTERACTION;
        messageInteractions.clear();
        decoder = null;
        encoders.clear();
    }
}
