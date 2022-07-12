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
package io.github.cjstehno.ersatz.socket.server;

import io.github.cjstehno.ersatz.socket.impl.ServerConfigImpl;
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

    private final ServerConfigImpl serverConfig;
    private final AtomicBoolean running;
    private final Socket socket;

    @Override public void run() {
        try (socket) {
            log.info("Connected to {}.", socket.getRemoteSocketAddress());

            try (val output = socket.getOutputStream()) {
                val context = new ConnectionContextImpl(serverConfig, output);

                // handle the connection interactions
                val interactions = serverConfig.getInteractions();
                interactions.getConnectInteraction().accept(context);

                // FIXME: throw useful exception
                val decoder = serverConfig.decoder().orElseThrow();

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