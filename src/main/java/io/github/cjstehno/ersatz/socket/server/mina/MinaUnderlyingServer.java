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
package io.github.cjstehno.ersatz.socket.server.mina;

import io.github.cjstehno.ersatz.socket.ErsatzSocketServer;
import io.github.cjstehno.ersatz.socket.impl.ServerConfigImpl;
import io.github.cjstehno.ersatz.socket.server.UnderlyingServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
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

            executor = Executors.newSingleThreadExecutor();

            val latch = new CountDownLatch(1);

            serverFuture = executor.submit(() -> {
                log.debug("Starting server thread...");

                try {
                    val acceptor = new NioSocketAcceptor(
                        serverConfig.getWorkerThreads() > 0 ? serverConfig.getWorkerThreads() : 1
                    );

                    val filterChain = acceptor.getFilterChain();

                    // FIXME: enabled if ssl enabled
//                    val sslFilter = new SslFilter(sslContext());
//                    sslFilter.setWantClientAuth(true);
//                    filterChain.addFirst("ssl", sslFilter);

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

    private SSLContext sslContext() {
        try {
            val keyStore = KeyStore.getInstance("JKS");

            // FIXME: add config
//            final var location = serverConfig.getKeystoreLocation() != null
//                ? serverConfig.getKeystoreLocation()
//                : ErsatzServer.class.getResource("/ersatz.keystore");

            val location = ErsatzSocketServer.class.getResource("/ersatz.keystore");
            val keystorePass = "ersatz".toCharArray(); // FIXME: config

            try (val instr = location.openStream()) {
                keyStore.load(instr, keystorePass);
            }

            val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keystorePass);

            val sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), null, null);

            return sslContext;

        } catch (IOException | GeneralSecurityException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
