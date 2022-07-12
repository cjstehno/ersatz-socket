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
package io.github.cjstehno.ersatz.socket.impl;

import io.github.cjstehno.ersatz.socket.cfg.Interactions;
import io.github.cjstehno.ersatz.socket.cfg.ServerConfig;
import io.github.cjstehno.ersatz.socket.encdec.Decoder;
import io.github.cjstehno.ersatz.socket.encdec.Encoder;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class ServerConfigImpl implements ServerConfig {

    @Getter private int port = 0;
    @Getter private int workerThreads = 0;
    @Getter private boolean autoStart = true;
    @Getter private final InteractionsImpl interactions;
    private final Map<Class<?>, Encoder> encoders = new HashMap<>();
    private Decoder<?> decoder;
    private Runnable starter;

    public ServerConfigImpl() {
        interactions = new InteractionsImpl(this);
    }

    public void setStarter(final Runnable starter) {
        this.starter = starter;
    }

    @Override public ServerConfig port(final int value) {
        port = value;
        return this;
    }

    @Override public ServerConfig workerThreads(final int value) {
        workerThreads = value;
        return this;
    }

    @Override public ServerConfig autoStart(boolean enabled) {
        autoStart = true;
        return this;
    }

    @Override public ServerConfig encoder(final Class<?> messageType, final Encoder encoder) {
        encoders.put(messageType, encoder);
        return this;
    }

    // FIXME: remove the mssage type its not used
    @Override public <T> ServerConfig decoder(final Class<T> messageType, final Decoder<T> decoder) {
        this.decoder = decoder;
        return this;
    }

    @Override public ServerConfig interactions(Consumer<Interactions> consumer) {
        consumer.accept(interactions);

        if (autoStart) {
            starter.run();
        }

        return this;
    }

    public Optional<Encoder> findEncoder(final Class<?> type) {
        return Optional.ofNullable(encoders.get(type));
    }

    public Optional<Decoder<?>> decoder() {
        return Optional.ofNullable(decoder);
    }

    public void resetInteractions(){
        interactions.reset();
    }
}
