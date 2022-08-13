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
import io.github.cjstehno.ersatz.socket.cfg.SslConfig;
import io.github.cjstehno.ersatz.socket.encdec.CodecUnavailableException;
import io.github.cjstehno.ersatz.socket.encdec.Decoder;
import io.github.cjstehno.ersatz.socket.encdec.Encoder;
import io.github.cjstehno.ersatz.socket.encdec.MultiMessageEncoder;
import lombok.Getter;

import java.util.function.Consumer;

public class ServerConfigImpl implements ServerConfig {

    @Getter private int port = 0;
    @Getter private int workerThreads = 0;
    @Getter private boolean autoStart = true;
    @Getter private final InteractionsImpl interactions;
    private Encoder encoder;
    private Decoder<?> decoder;
    private Runnable starter;
    @Getter private boolean ssl;
    @Getter private SslConfigImpl sslConfig;

    public ServerConfigImpl() {
        interactions = new InteractionsImpl(this);
    }

    public void setStarter(final Runnable starter) {
        this.starter = starter;
    }

    @Override public ServerConfig ssl(final boolean enabled, final Consumer<SslConfig> config) {
        this.ssl = enabled;
        sslConfig = new SslConfigImpl();
        config.accept(sslConfig);
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
            this.encoder = MultiMessageEncoder.byMessageType().encoderFor(messageType, encoder);

        } else if (this.encoder instanceof MultiMessageEncoder) {
            // we just add the new type encoder
            ((MultiMessageEncoder) this.encoder).encoderFor(messageType, encoder);

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

    public Encoder encoder() {
        if (encoder != null) {
            return encoder;
        } else {
            throw new CodecUnavailableException("No encoder has been configured.");
        }
    }

    public Decoder<?> decoder() {
        if (decoder != null) {
            return decoder;
        } else {
            throw new CodecUnavailableException("No decoder has been configured.");
        }
    }

    public void resetInteractions() {
        interactions.reset();
    }
}
