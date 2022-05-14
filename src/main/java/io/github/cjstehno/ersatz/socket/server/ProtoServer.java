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
package io.github.cjstehno.ersatz.socket.server;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;
import static java.nio.charset.StandardCharsets.UTF_8;

// FIXME: document emphemeral port and ad-hoc threads (zeros)
@RequiredArgsConstructor @Slf4j
public class ProtoServer {

    private final int port;
    private final int workers;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicInteger actualPort = new AtomicInteger();
    private ExecutorService executor;
    private Future<?> serverFuture;

    public ProtoServer() {
        this(0, 0);
    }

    public int getPort() {
        return actualPort.get();
    }

    public ProtoServer start(final boolean waitUntilStarted) {
        if (!running.get()) {
            log.debug("Starting...");

            if (workers > 0) {
                val actualWorkers = workers + 1;
                executor = Executors.newFixedThreadPool(actualWorkers);
                log.info("Started thread pool with {} workers...", actualWorkers);
            } else {
                executor = Executors.newCachedThreadPool();
                log.info("Started thread pool with cached workers...");
            }

            serverFuture = executor.submit(() -> {
                log.debug("Starting server thread...");

                try (val serverSkt = new ServerSocket(port)) {
                    actualPort.set(serverSkt.getLocalPort());
                    running.set(true);
                    log.info("Started on port {}.", serverSkt.getLocalPort());

                    while (running.get()) {
                        executor.submit(new ConnectionHandler(running, serverSkt.accept()));
                    }

                    log.debug("Server thread is terminating.");

                } catch (IOException e) {
                    // FIXME: more?
                    log.error("Server error: {}", e.getMessage(), e);
                }

            });


            if (waitUntilStarted) {
                log.debug("Waiting for server to start...");
                while (!running.get()) {
                    try {
                        // TODO: better way to do this?
                        //noinspection BusyWait
                        sleep(100);
                    } catch (InterruptedException e) {
                        // just try again
                    }
                }
                log.debug("Server started - moving on...");
            }
        }

        return this;
    }

    public void stop() {
        if (running.get()) {
            log.debug("Stopping...");
            running.set(false);

            if (serverFuture != null && !serverFuture.isDone() && !serverFuture.isCancelled()) {
                serverFuture.cancel(true);
                log.debug("Stopped server thread...");
            }

            executor.shutdownNow();
            log.debug("Stopped thread pool...");

            log.info("Stopped.");
        }
    }

    // FIXME: better name
    // FIXME: this shoudl probably come from the server via a factory method?
    @RequiredArgsConstructor @Slf4j
    private static class ConnectionContext {
        // FIXME:  async or thread-safe

        private final OutputStream output;

        public void onConnect() {
            // fIXME: do something on connection
            // FIXME: how woudl I send a message (or more) on connect?
            // FIXME: consider how to send delayed message to client after connect
            log.info("Connected");

            // send message with number of items to be sent
            try {
                val out = new DataOutputStream(output);
                out.writeInt(10);
                out.flush();
            } catch (IOException ioe) {
                log.error("Error: {}", ioe.getMessage(), ioe);
            }
        }

        public void onMessage(final Object message) {
            // FIXME: impl
            // FIXME: how would I send response (or other) on message (matched)
            log.info("Message: {}", message);

            // respond with modified message
            try {
                val out = new DataOutputStream(output);
                val bytes = (message.toString() + "-modified").getBytes(UTF_8);
                out.writeInt(bytes.length);
                out.write(bytes);
            } catch (IOException ioe) {
                log.error("Error: {}", ioe.getMessage(), ioe);
            }
        }

        public void onDisconnect() {
            // FIXME: ?
            log.info("Disconnected.");
        }
    }

    @RequiredArgsConstructor @Slf4j
    private static class ConnectionHandler implements Runnable {

        private final AtomicBoolean running;
        private final Socket socket;
        private final MessageDecoder decoder = new MessageDecoder();

        @Override public void run() {
            try (socket) {
                log.info("Connected to {}.", socket.getRemoteSocketAddress());

                try (val output = socket.getOutputStream()) {
                    val context = new ConnectionContext(output);
                    context.onConnect();

                    // read from the socket
                    try (val input = new BufferedInputStream(socket.getInputStream())) {
                        while (running.get()) {
                            try {
                                val message = decoder.decode(input);
                                context.onMessage(message);
                            } catch (EOFException eof) {
                                // ignore?
                            } catch (IOException ioe) {
                                // single message decoding should not kill server
                                log.error("Decoding error: {}", ioe.getMessage(), ioe);
                            }
                        }
                    }

                    context.onDisconnect();
                }

            } catch (IOException e) {
                // FIXME: more?
                log.error("Connection error: {}", e.getMessage(), e);
            } finally {
                log.info("Disconnected.");
            }
        }
    }

    static class MessageDecoder {
        // FIXME: replace with Function?

        public String decode(final InputStream stream) throws IOException {
            val data = new DataInputStream(stream);
            val length = data.readInt();
            val bytes = data.readNBytes(length);
            return new String(bytes, UTF_8);
        }
    }
}
