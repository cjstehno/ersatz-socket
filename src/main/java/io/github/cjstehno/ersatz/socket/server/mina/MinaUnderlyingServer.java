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
package io.github.cjstehno.ersatz.socket.server.mina;

import io.github.cjstehno.ersatz.socket.impl.ServerConfigImpl;
import io.github.cjstehno.ersatz.socket.server.UnderlyingServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor @Slf4j
public class MinaUnderlyingServer implements UnderlyingServer {

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

                try {
                    val acceptor = new NioSocketAcceptor(/* TODO: configure? */);

                    val filterChain = acceptor.getFilterChain();
                    filterChain.addLast("logger", new LoggingFilter(MinaUnderlyingServer.class));
                    filterChain.addLast("codec", new ProtocolCodecFilter(
                        new ErsatzProtocolEncoder(serverConfig),
                        new ErsatzProtocolDecoder(serverConfig)
                    ));

                    acceptor.setHandler(new ConnectionHandler(serverConfig));

                    val sessionCfg = acceptor.getSessionConfig();
                    sessionCfg.setReadBufferSize(2048);
                    sessionCfg.setIdleTime(IdleStatus.BOTH_IDLE, 10);

                    acceptor.bind(new InetSocketAddress(serverConfig.getPort()));

                    val localPort = acceptor.getLocalAddress().getPort();
                    actualPort.set(localPort);

                    running.set(true);
                    latch.countDown();
                    log.info("Started on port {}.", localPort);

                } catch (Exception ex) {
                    // FIXME: more?
                    log.error("Server error: {}", ex.getMessage(), ex);
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
        // FIXME: is this still useful and/or correct?
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
