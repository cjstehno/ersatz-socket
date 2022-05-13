package io.github.cjstehno.ersatz.socket.server;

import io.github.cjstehno.ersatz.socket.impl.ServerConfigImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor @Slf4j
public class UnderlyingServerImpl implements UnderlyingServer {

    // FIXME: support restarting

    private final ServerConfigImpl serverConfig;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicInteger actualPort = new AtomicInteger();
    private ExecutorService executor;
    private Future<?> serverFuture;

    @Override public void start() {
        if (!running.get()) {
            log.debug("Starting...");

            startThreadPool();

            val latch = new CountDownLatch(1);

            serverFuture = executor.submit(() -> {
                log.debug("Starting server thread...");

                try (val serverSkt = new ServerSocket(serverConfig.getPort())) {
                    actualPort.set(serverSkt.getLocalPort());

                    running.set(true);
                    latch.countDown();
                    log.info("Started on port {}.", serverSkt.getLocalPort());

                    while (running.get()) {
                        executor.submit(new ConnectionHandler(serverConfig.getInteractions(), running, serverSkt.accept()));
                    }

                    log.debug("Server thread is terminating.");

                } catch (IOException e) {
                    // FIXME: more?
                    log.error("Server error: {}", e.getMessage(), e);
                }

            });

            // let's wait for the server to start before moving on
            try {
                latch.await();
            } catch (InterruptedException e) {
                log.warn("Problem waiting for server to start: {}", e.getMessage(), e);
            }
        }
    }

    private void startThreadPool() {
        if (serverConfig.getWorkerThreads() > 0) {
            val actualWorkers = serverConfig.getWorkerThreads() + 1;
            executor = Executors.newFixedThreadPool(actualWorkers);
            log.info("Started thread pool with {} workers...", actualWorkers);
        } else {
            executor = Executors.newCachedThreadPool();
            log.info("Started thread pool with cached workers...");
        }
    }

    @Override public void stop() {
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

    @Override public int getActualPort() {
        return actualPort.get();
    }
}
