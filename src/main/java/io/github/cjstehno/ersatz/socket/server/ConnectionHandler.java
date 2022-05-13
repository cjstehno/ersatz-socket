package io.github.cjstehno.ersatz.socket.server;

import io.github.cjstehno.ersatz.socket.impl.InteractionsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE) @Slf4j
class ConnectionHandler implements Runnable {

    private final InteractionsImpl interactions;
    private final AtomicBoolean running;
    private final Socket socket;

    @Override public void run() {
        try (socket) {
            log.info("Connected to {}.", socket.getRemoteSocketAddress());

            try (val output = socket.getOutputStream()) {
                val context = new ConnectionContextImpl(interactions, output);

                // handle the connection interactions
                interactions.getConnectInteraction().accept(context);

                // FIXME: throw useful exception
                val decoder = interactions.decoder().orElseThrow();

                // read from the socket
                try (val input = new BufferedInputStream(socket.getInputStream())) {
                    while (running.get()) {
                        try {
                            // handle the message interactions
                            val message = decoder.decode(input);

                            // FIXME: throws if no matches (?) - or should just be ignored?
                            val messageInteraction = interactions.findMessageInteraction(message).orElseThrow();
                            messageInteraction.accept(context, message);

                        } catch (EOFException eof) {
                            // ignore?
                        } catch (IOException ioe) {
                            // single message decoding should not kill server
                            log.error("Decoding error: {}", ioe.getMessage(), ioe);
                        }
                    }
                }
            }

        } catch (IOException e) {
            // FIXME: more?
            log.error("Connection error: {}", e.getMessage(), e);
        } finally {
            log.info("Disconnected.");
        }
    }
}