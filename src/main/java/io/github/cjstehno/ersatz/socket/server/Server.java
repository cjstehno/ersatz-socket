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

import static java.nio.charset.StandardCharsets.UTF_8;

@RequiredArgsConstructor @Slf4j
public class Server {

    // FIXME: allow 0 port for ephemeral
    // FIXME: allow 0 workers for "as many as it takes" cached pool

    private final int port;
    private final int workers;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final ServerContext context = new ServerContext();
    private ExecutorService executor;
    private Future<?> serverFuture;

    public void start() {
        if (!running.get()) {
            log.debug("Starting...");

            val actualWorkers = workers + 1;

            executor = Executors.newFixedThreadPool(actualWorkers);
            log.debug("Started thread pool with {} workers...", actualWorkers);

            executor.submit(() -> {
                log.debug("Starting server thread...");

                try (val serverSkt = new ServerSocket(port)) {
                    running.set(true);
                    log.info("Started on port {} with {} worker threads.", port, actualWorkers);

                    while (running.get()) {
                        executor.submit(new ConnectionHandler(context, running, serverSkt.accept()));
                    }

                    log.debug("Server thread is terminating.");

                } catch (IOException e) {
                    // FIXME: more?
                    log.error("Server error: {}", e.getMessage(), e);
                }

            });

            // FIXME: would be nice to have an option to block until started
        }
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
    @Slf4j
    private static class ServerContext {
        // FIXME:  async or thread-safe

        public void onConnect() {
            // fIXME: do something on connection
            log.info("Connected");
        }

        // FIXME: how do you respond?

        public void onMessage(final Object message) {
            // FIXME: impl
            log.info("Message: {}", message);
        }
    }

    @RequiredArgsConstructor @Slf4j
    private static class ConnectionHandler implements Runnable {

        private final ServerContext context;
        private final AtomicBoolean running;
        private final Socket socket;
        private final MessageDecoder decoder = new MessageDecoder();

        @Override public void run() {
            try (socket) {
                log.info("Connected to {}.", socket.getRemoteSocketAddress());

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
