package io.github.cjstehno.ersatz.socket.cfg;

import io.github.cjstehno.ersatz.socket.encdec.Decoder;
import io.github.cjstehno.ersatz.socket.encdec.Encoder;
import org.hamcrest.Matcher;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface Interactions {

    // FIXME: called(n)

    <T> Interactions encoder(Class<T> messageType, Encoder encoder);

    <T> Interactions decoder(Class<T> messageType, Decoder<T> decoder);

    // FIXME: document - when a client connects this consumer will be triggered with teh connection context
    // this should only be called once (or it will override)
    Interactions onConnect(Consumer<ConnectionContext> consumer);

    <T> Interactions onMessage(Matcher<T> matching, BiConsumer<ConnectionContext, T> consumer);
}
