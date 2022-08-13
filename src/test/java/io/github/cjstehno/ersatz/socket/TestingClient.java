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
package io.github.cjstehno.ersatz.socket;

import io.github.cjstehno.ersatz.socket.cfg.SslConfig;
import io.github.cjstehno.ersatz.socket.impl.SslConfigImpl;
import io.github.cjstehno.ersatz.socket.server.mina.MinaUnderlyingServer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
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
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

import static io.github.cjstehno.ersatz.socket.server.mina.MinaUnderlyingServer.DEFAULT_KEYSTORE;
import static io.github.cjstehno.ersatz.socket.server.mina.MinaUnderlyingServer.DEFAULT_TRUSTSTORE;
import static java.nio.charset.StandardCharsets.US_ASCII;

@RequiredArgsConstructor @Slf4j
public class TestingClient {

    // FIXME: add support for binary protocol
    // FIXME: test binary protocol

    @Getter private final Set<String> responses = new CopyOnWriteArraySet<>();
    private List<Runnable> onConnectListeners = new LinkedList<>();
    private List<Consumer<TestMessage>> onMessageListeners = new LinkedList<>();
    private final SslConfigImpl sslConfig = new SslConfigImpl();
    private final int port;
    private final boolean ssl;
    private final Consumer<SslConfig> sslConfigurator;
    private IoConnector connector;
    private IoSession session;

    public void connect() throws Exception {
        connector = new NioSocketConnector();

        val filterChain = connector.getFilterChain();

        if (ssl) {
            // apply the ssl config and add the filter
            sslConfigurator.accept(sslConfig);
            filterChain.addLast("ssl", new SslFilter(sslContext(sslConfig)));
        }

        // TODO: logging?
        filterChain.addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(US_ASCII)));

        connector.setHandler(new IoHandlerAdapter() {
            @Override public void sessionOpened(IoSession session) throws Exception {
                log.info("Client-Connected on port {}...", port);
                onConnection();
            }

            @Override public void messageReceived(final IoSession session, final Object message) throws Exception {
                log.info("Client-Received: {}", message);
                onMessage(TestMessage.from((String) message));
            }
        });

        // FIXME: timeout after N tries
        for (; ; ) {
            try {
                ConnectFuture future = connector.connect(new InetSocketAddress("localhost", port));
                future.awaitUninterruptibly();
                session = future.getSession();
                break;
            } catch (RuntimeException e) {
                log.error("Failed to connect.");
                e.printStackTrace();
                Thread.sleep(5000);
            }
        }
    }

    public void onConnection(final Runnable op) {
        onConnectListeners.add(op);
    }

    public void onMessage(final Consumer<TestMessage> consumer) {
        onMessageListeners.add(consumer);
    }

    public void send(final TestMessage message) {
        session.write(message.toMessage());
    }

    public void disconnect() {
        log.info("Disconnecting...");
        session.getCloseFuture().addListener(future -> log.info("Disconnected."));

        session.closeNow();
        session.getCloseFuture().awaitUninterruptibly();
        connector.dispose();
        log.info("Closed.");
    }

    private void onConnection() {
        onConnectListeners.forEach(Runnable::run);
    }

    private void onMessage(final TestMessage message) {
        onMessageListeners.forEach(li -> li.accept(message));
    }

    @Value
    public static class TestMessage {
        String prefix;
        String value;

        public static TestMessage from(final String string) {
            val parts = string.split(":");
            return new TestMessage(parts[0].trim(), parts[1].trim());
        }

        String toMessage() {
            return prefix + ": " + value + "\n";
        }
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
