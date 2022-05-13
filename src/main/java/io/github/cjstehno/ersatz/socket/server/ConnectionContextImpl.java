package io.github.cjstehno.ersatz.socket.server;

import io.github.cjstehno.ersatz.socket.cfg.ConnectionContext;
import io.github.cjstehno.ersatz.socket.impl.InteractionsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.IOException;
import java.io.OutputStream;

@RequiredArgsConstructor @Slf4j
class ConnectionContextImpl implements ConnectionContext {

    private final InteractionsImpl interactions;
    private final OutputStream output;

    @Override public void send(final Object message) {
        try {
            log.info("Sending: {}", message);

            // FIXME: throw useful exception (here and below)
            val encoder = interactions.findEncoder(message.getClass()).orElseThrow();
            encoder.encode(message, output);

        } catch (IOException e) {
            // FIXME:
            log.error("Unable to send message ({}): {}", message, e.getMessage(), e);
        }
    }
}