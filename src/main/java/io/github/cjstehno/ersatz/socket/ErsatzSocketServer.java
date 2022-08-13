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

import io.github.cjstehno.ersatz.socket.cfg.Interactions;
import io.github.cjstehno.ersatz.socket.cfg.ServerConfig;
import io.github.cjstehno.ersatz.socket.impl.ServerConfigImpl;
import io.github.cjstehno.ersatz.socket.server.UnderlyingServer;
import io.github.cjstehno.ersatz.socket.server.mina.MinaUnderlyingServer;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * The entry point for using the socket server testing framework.
 */
@Slf4j
public class ErsatzSocketServer implements Closeable {

    private final ServerConfigImpl serverConfig = new ServerConfigImpl();
    private final UnderlyingServer underlyingServer;

    /**
     * Creates a server with default configuration.
     */
    public ErsatzSocketServer() {
        this(cfg -> {
        });
    }

    /**
     * Creates a server with the specified configuration.
     *
     * @param consumer the configuration
     */
    public ErsatzSocketServer(final Consumer<ServerConfig> consumer) {
        consumer.accept(serverConfig);
        serverConfig.setStarter(this::start);
        underlyingServer = new MinaUnderlyingServer(serverConfig);
    }

    /**
     * Used to retrieve the actual server port. Generally, the server will be started with a configured port of "0" -
     * this method will provide the port used.
     *
     * @return the actual server port.
     */
    public int getPort() {
        return underlyingServer.getActualPort();
    }

    /**
     * Whether the server is running with SSL enabled.
     *
     * @return true if SSL is enabled.
     */
    public boolean isSsl() {
        return serverConfig.isSsl();
    }

    /**
     * Used to configured the expected and available interactions with the server - if the server is configured to
     * "auto-start" (default), it will be started when this method returns.
     *
     * @param interactions the server interactions
     * @return a reference to this server instance
     */
    public ErsatzSocketServer interactions(final Consumer<Interactions> interactions) {
        serverConfig.interactions(interactions);

        if (serverConfig.isAutoStart()) {
            underlyingServer.start();
        }

        return this;
    }

    /**
     * Used to clear the configured interactions, without restarting the server.
     */
    public void resetInteractions() {
        serverConfig.resetInteractions();
    }

    /**
     * Starts the server (if not already started). If auto-start is enabled (default) you don't need to call this if
     * you are configuring interactions.
     *
     * @return a reference to this server instance
     */
    public ErsatzSocketServer start() {
        underlyingServer.start();
        return this;
    }

    /**
     * Used to stop the server, if it is running.
     */
    public void stop() {
        underlyingServer.stop();
    }

    /**
     * Used to support the <code>Closable</code> interface - simply calls the <code>stop()</code> method.
     *
     * @throws IOException if there is a problem stopping
     */
    @Override public void close() throws IOException {
        stop();
    }
}
