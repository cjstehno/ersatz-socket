package io.github.cjstehno.ersatz.socket.cfg;

import io.github.cjstehno.ersatz.socket.encdec.Decoder;
import io.github.cjstehno.ersatz.socket.encdec.Encoder;

import java.util.function.Consumer;

public interface ServerConfig {

    // avoid - 0 for ephemeral (Default)
    ServerConfig port(final int value);

    // generally use default (0) which creates cached threads
    ServerConfig workerThreads(final int value);

    ServerConfig autoStart(boolean enabled);

    ServerConfig encoder(final Class<?> messageType, final Encoder encoder);

    /**
     * FIXME: document
     * <p>
     * Only one decoder is allowed in a configuration. This is due to the lack of ability to determine whether
     * a decoder matches the incoming message without decoding it.
     * I feel that it is fair limitation at this point - if your server can accept different message types that
     * have no unified interface, you can simply write a separate test for each message type.
     *
     * @param messageType
     * @param decoder
     * @param <T>
     * @return
     */
    <T> ServerConfig decoder(final Class<T> messageType, final Decoder<T> decoder);

    ServerConfig interactions(final Consumer<Interactions> consumer);

    /// FIXME: reportToConsole
}
