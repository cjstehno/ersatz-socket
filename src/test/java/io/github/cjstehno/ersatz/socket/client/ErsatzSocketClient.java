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
package io.github.cjstehno.ersatz.socket.client;

import io.github.cjstehno.ersatz.socket.client.cfg.ClientConfig;
import io.github.cjstehno.ersatz.socket.client.cfg.ClientConfigImpl;
import io.github.cjstehno.ersatz.socket.client.cfg.Sender;
import io.github.cjstehno.ersatz.socket.client.codec.ClientProtocolDecoder;
import io.github.cjstehno.ersatz.socket.client.codec.ClientProtocolEncoder;
import io.github.cjstehno.ersatz.socket.impl.SslConfigImpl;
import io.github.cjstehno.ersatz.socket.server.mina.MinaUnderlyingServer;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteToClosedSessionException;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.ssl.KeyStoreFactory;
import org.apache.mina.filter.ssl.SslContextFactory;
import org.apache.mina.filter.ssl.SslFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static io.github.cjstehno.ersatz.socket.server.mina.MinaUnderlyingServer.DEFAULT_KEYSTORE;
import static io.github.cjstehno.ersatz.socket.server.mina.MinaUnderlyingServer.DEFAULT_TRUSTSTORE;

// FIXME: document
@Slf4j
public class ErsatzSocketClient<D> implements Sender {

    // FIXME: consider a junit extension for helping with these

    private final ClientConfigImpl clientConfig = new ClientConfigImpl();
    private final List<Consumer<Sender>> connectionListeners = new LinkedList<>();
    private final List<BiConsumer<Sender, D>> messageListeners = new LinkedList<>();
    private final ExecutorService executor = Executors.newCachedThreadPool(); // TODO: refactor if kept
    private IoConnector connector;
    private IoSession session;

    public ErsatzSocketClient() {
        this(cfg -> {
        });
    }

    public ErsatzSocketClient(final Consumer<ClientConfig> config) {
        config.accept(clientConfig);
    }

    public ErsatzSocketClient connect() throws Exception {
        connector = new NioSocketConnector(/* TODO: ok? default is procs+1 */);

        val filterChain = connector.getFilterChain();

        // FIXME: logging filter? configurable?

        if (clientConfig.isSsl()) {
            filterChain.addLast("ssl", new SslFilter(sslContext(clientConfig.getSslConfig())));
        }

        filterChain.addLast("codec", new ProtocolCodecFilter(
            new ClientProtocolEncoder(clientConfig),
            new ClientProtocolDecoder(clientConfig)
        ));

        val sender = (Sender) this;
        connector.setHandler(new IoHandlerAdapter() {
            @Override
            public void sessionOpened(IoSession session) throws Exception {
                log.info("Client-Connected on port {}...", clientConfig.getPort());

                // FIXME: on separate thread?
                connectionListeners.forEach(li -> {
                    executor.submit(() -> {
                        try {
                            li.accept(sender);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });
                });
            }

            @Override
            public void messageReceived(final IoSession session, final Object message) throws Exception {
                log.info("Client-Received ({} listeners): {}", messageListeners.size(), message);

                // FIXME: on separate thread?
                messageListeners.forEach(li -> {
                    executor.submit(() -> {
                        try {
                            li.accept(sender, (D) message);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });
                });
            }

            @Override public void exceptionCaught(final IoSession session, final Throwable cause) throws Exception {
                if (!(cause instanceof WriteToClosedSessionException)) {
                    // FIXME: do better
                    cause.printStackTrace();
                } else {
                    //log.warn("Caught-Exception: {}", cause.getMessage(), cause);
                }
            }
        });

        // FIXME: timeout after N tries (or time) - handle this better
        for (; ; ) {
            try {
                ConnectFuture future = connector.connect(new InetSocketAddress("localhost", clientConfig.getPort()));
                future.awaitUninterruptibly();
                session = future.getSession();
                break;
            } catch (RuntimeException e) {
                log.error("Failed to connect.");
                e.printStackTrace();
                Thread.sleep(5000);
            }
        }

        return this;
    }

    /**
     * Configures operation to be performed when a connection is established with
     * the server. If more than one operation
     * is configured, they will be executed in the order they were added.
     *
     * @param consumer the configuration consumer
     * @return a reference to this client
     */
    public ErsatzSocketClient onConnect(final Consumer<Sender> consumer) {
        connectionListeners.add(consumer);
        return this;
    }

    /**
     * FIXME: document
     */
    public ErsatzSocketClient onMessage(final BiConsumer<Sender, D> consumer) {
        messageListeners.add(consumer);
        return this;
    }

    // FIXME: add a means of filtering messagte handlers by matcher?

    // FIXME: onDisconnect()?
    // FIXME: onError()?

    public void send(final Object message) {
        log.info("Sending: {}", message);
        session.write(message);
    }

    public void disconnect() {
        log.info("Disconnecting...");
        session.getCloseFuture().addListener(future -> {
            log.info("Disconnected.");
        });

        session.closeNow();
        session.getCloseFuture().awaitUninterruptibly();
        connector.dispose();
        log.info("Closed.");
    }

    // FIXME: this is duplicated in the client - pull out
    private static SSLContext sslContext(final SslConfigImpl sslConfig) throws Exception {
        log.debug("Configuring SSL context...");

        val keyStoreFile = location(sslConfig.getKeystoreLocation(), DEFAULT_KEYSTORE);
        val trustStoreFile = location(sslConfig.getTruststoreLocation(), DEFAULT_TRUSTSTORE);

        if (keyStoreFile.exists() && trustStoreFile.exists()) {
            val keyStoreFactory = new KeyStoreFactory();
            keyStoreFactory.setDataFile(keyStoreFile);
            keyStoreFactory.setPassword(sslConfig.getKeystorePassword());

            val trustStoreFactory = new KeyStoreFactory();
            trustStoreFactory.setDataFile(trustStoreFile);
            trustStoreFactory.setPassword(sslConfig.getTruststorePassword());

            val sslContextFactory = new SslContextFactory();
            sslContextFactory.setKeyManagerFactoryKeyStore(keyStoreFactory.newInstance());

            val trustStore = trustStoreFactory.newInstance();
            sslContextFactory.setTrustManagerFactoryKeyStore(trustStore);
            sslContextFactory.setKeyManagerFactoryKeyStorePassword(sslConfig.getKeystorePassword());

            val sslContext = sslContextFactory.newInstance();
            log.info("SSL provider: {}", sslContext.getProvider());
            return sslContext;

        } else {
            throw new IllegalStateException("Unable to configure SSL: keystore or truststore does not exist.");
        }
    }

    private static File location(final URL url, final String fallback) throws URISyntaxException {
        return new File((url != null ? url : MinaUnderlyingServer.class.getResource(fallback)).toURI());
    }
}
