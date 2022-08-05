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

import io.github.cjstehno.ersatz.socket.cfg.Interactions;
import io.github.cjstehno.ersatz.socket.cfg.ServerConfig;
import io.github.cjstehno.ersatz.socket.encdec.Decoder;
import io.github.cjstehno.ersatz.socket.encdec.Encoder;
import io.github.cjstehno.ersatz.socket.encdec.MessageTypeEncoder;
import io.github.cjstehno.ersatz.socket.server.UnderlyingServer;
import io.github.cjstehno.ersatz.socket.server.jio.IoUnderlyingServer;
import lombok.Getter;

import java.util.Optional;
import java.util.function.Consumer;

import static io.github.cjstehno.ersatz.socket.encdec.MessageTypeEncoder.byMessageType;

public class ServerConfigImpl implements ServerConfig {

    @Getter private int port = 0;
    @Getter private int workerThreads = 0;
    @Getter private boolean autoStart = true;
    @Getter private final InteractionsImpl interactions;
    private Encoder encoder;
    private Decoder<?> decoder;
    private Runnable starter;
    @Getter private Class<? extends UnderlyingServer> serverClass = IoUnderlyingServer.class;

    public ServerConfigImpl() {
        interactions = new InteractionsImpl(this);
    }

    public void setStarter(final Runnable starter) {
        this.starter = starter;
    }

    @Override public ServerConfig server(final Class<? extends UnderlyingServer> serverClass) {
        this.serverClass = serverClass;
        return this;
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

    @Override public ServerConfig encoder(final Encoder encoder) {
        this.encoder = encoder;
        return this;
    }

    @Override public ServerConfig encoder(final Class<?> messageType, final Encoder encoder) {
        if (this.encoder == null) {
            // there is no encoder so assume this is what they want
            this.encoder = byMessageType().encoderFor(messageType, encoder);

        } else if (this.encoder instanceof MessageTypeEncoder) {
            // we just add the new type encoder
            ((MessageTypeEncoder) this.encoder).encoderFor(messageType, encoder);

        } else {
            throw new IllegalArgumentException(
                "The configured encoder (" + this.encoder.getClass().getSimpleName() + ") does not support direct configuration."
            );
        }
        return this;
    }

    @Override public <T> ServerConfig decoder(final Decoder<T> decoder) {
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

    public Optional<Encoder> encoder() {
        return Optional.ofNullable(encoder);
    }

    public Optional<Decoder<?>> decoder() {
        return Optional.ofNullable(decoder);
    }

    public void resetInteractions() {
        interactions.reset();
    }
}
