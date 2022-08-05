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
import io.github.cjstehno.ersatz.socket.server.jio.IoUnderlyingServer;
import io.github.cjstehno.ersatz.socket.server.UnderlyingServer;
import io.github.cjstehno.ersatz.socket.server.mina.MinaUnderlyingServer;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * The entry point for using the socket server testing framework.
 */
public class ErsatzSocketServer implements Closeable {

    private final UnderlyingServer underlyingServer;
    private final ServerConfigImpl serverConfig;

    public ErsatzSocketServer() {
        this.serverConfig = new ServerConfigImpl();
        serverConfig.setStarter(this::start);

        // FIXME: need way to specify
//        this.underlyingServer = new IoUnderlyingServer(serverConfig);
        this.underlyingServer = new MinaUnderlyingServer(serverConfig);
    }

    public ErsatzSocketServer(final Consumer<ServerConfig> consumer) {
        this();
        if (consumer != null) {
            consumer.accept(serverConfig);
        }
    }

    public int getPort() {
        return underlyingServer.getActualPort();
    }

    public ErsatzSocketServer interactions(final Consumer<Interactions> interactions) {
        serverConfig.interactions(interactions);

        if (serverConfig.isAutoStart()) {
            underlyingServer.start();
        }

        return this;
    }

    public void resetInteractions() {
        serverConfig.resetInteractions();
    }

    public ErsatzSocketServer start() {
        underlyingServer.start();
        return this;
    }

    public void stop() {
        underlyingServer.stop();
    }

    @Override public void close() throws IOException {
        stop();
    }
}
