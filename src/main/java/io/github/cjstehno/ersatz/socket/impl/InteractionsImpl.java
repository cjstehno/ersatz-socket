package io.github.cjstehno.ersatz.socket.impl;

import io.github.cjstehno.ersatz.socket.cfg.ConnectionContext;
import io.github.cjstehno.ersatz.socket.cfg.Interactions;
import io.github.cjstehno.ersatz.socket.encdec.Decoder;
import io.github.cjstehno.ersatz.socket.encdec.Encoder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.hamcrest.Matcher;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class InteractionsImpl implements Interactions {

    private final ServerConfigImpl serverConfig;
    private final Map<Class<?>, Encoder> encoders = new HashMap<>();
    private Decoder<?> decoder;

    @Getter private Consumer<ConnectionContext> connectInteraction = (ctx) -> {
        // no-op by default
    };
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
        if( encoders.containsKey(type)){
            return Optional.of(encoders.get(type));
        } else {
            return serverConfig.findEncoder(type);
        }
    }

    public Optional<Decoder<?>> decoder() {
        if( decoder != null){
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
}
