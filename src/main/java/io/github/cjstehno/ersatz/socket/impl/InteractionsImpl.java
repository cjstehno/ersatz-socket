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
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hamcrest.Matcher;

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
    @Getter private Consumer<ConnectionContext> connectInteraction = NO_OP_CONNECT_INTERACTION;
    private final Map<Matcher<?>, BiConsumer<ConnectionContext, Object>> messageInteractions = new LinkedHashMap<>();

    @Override public Interactions onConnect(final Consumer<ConnectionContext> consumer) {
        connectInteraction = consumer;
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override public <T> Interactions onMessage(Matcher<T> matching, BiConsumer<ConnectionContext, T> consumer) {
        messageInteractions.put(matching, (BiConsumer<ConnectionContext, Object>) consumer);
        return this;
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
    }
}
