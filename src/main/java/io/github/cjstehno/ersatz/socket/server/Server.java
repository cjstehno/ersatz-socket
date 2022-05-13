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
public class Server {

    private final int port;
    private final int workers;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicInteger actualPort = new AtomicInteger();
    private ExecutorService executor;
    private Future<?> serverFuture;

    public Server() {
        this(0, 0);
    }

    public int getPort() {
        return actualPort.get();
    }

    public Server start(final boolean waitUntilStarted) {
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

                try (val output = new BufferedOutputStream(socket.getOutputStream())) {
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

    private static class MessageDecoder {
        // FIXME: replace with Function?

        public String decode(final InputStream stream) throws IOException {
            val data = new DataInputStream(stream);
            val length = data.readInt();
            val bytes = data.readNBytes(length);
            return new String(bytes, UTF_8);
        }
    }
}
