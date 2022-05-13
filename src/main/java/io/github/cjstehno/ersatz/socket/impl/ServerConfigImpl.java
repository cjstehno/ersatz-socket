package io.github.cjstehno.ersatz.socket.impl;

import io.github.cjstehno.ersatz.socket.cfg.Interactions;
import io.github.cjstehno.ersatz.socket.cfg.ServerConfig;
import io.github.cjstehno.ersatz.socket.encdec.Decoder;
import io.github.cjstehno.ersatz.socket.encdec.Encoder;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class ServerConfigImpl implements ServerConfig {

    @Getter private int port = 0;
    @Getter private int workerThreads = 0;
    @Getter private boolean autoStart = true;
    private final Map<Class<?>, Encoder> encoders = new HashMap<>();
    private final AtomicReference<Decoder<?>> decoder = new AtomicReference<>();
    @Getter private final InteractionsImpl interactions;
    private Runnable starter;

    public ServerConfigImpl() {
        interactions = new InteractionsImpl(decoder, encoders);
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

    @Override public <T> ServerConfig decoder(final Class<T> messageType, final Decoder<T> decoder) {
        this.decoder.set(decoder);
        return this;
    }

    @Override public ServerConfig interactions(Consumer<Interactions> consumer) {
        consumer.accept(interactions);

        if (autoStart) {
            starter.run();
        }

        return this;
    }
}
