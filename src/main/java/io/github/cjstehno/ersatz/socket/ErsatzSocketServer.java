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
package io.github.cjstehno.ersatz.socket;

import io.github.cjstehno.ersatz.socket.cfg.Interactions;
import io.github.cjstehno.ersatz.socket.cfg.ServerConfig;
import io.github.cjstehno.ersatz.socket.impl.ServerConfigImpl;
import io.github.cjstehno.ersatz.socket.server.UnderlyingServer;
import io.github.cjstehno.ersatz.socket.server.jio.JioUnderlyingServer;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * The entry point for using the socket server testing framework.
 */
@Slf4j
public class ErsatzSocketServer implements Closeable {

    // FIXME: need to pull mina into an extension project - or just make it the default?
    // - make the decision after implementing ssl in both

    private final ServerConfigImpl serverConfig = new ServerConfigImpl();
    private final UnderlyingServer underlyingServer;

    public ErsatzSocketServer() {
        this(cfg -> {});
    }

    public ErsatzSocketServer(final Consumer<ServerConfig> consumer) {
        consumer.accept(serverConfig);

        serverConfig.setStarter(this::start);

        this.underlyingServer = instantiateServer(serverConfig);
    }

    private static UnderlyingServer instantiateServer(final ServerConfigImpl config) {
        val serverClass = config.getServerClass();
        try {
            val instance = serverClass.getDeclaredConstructor(ServerConfigImpl.class).newInstance(config);
            log.debug("Using instance of {} as the server.", serverClass);
            return instance;
        } catch (Exception ex) {
            log.warn("Unable to instantiate server ({}) - using default.", serverClass);
            return new JioUnderlyingServer(config);
        }
    }

    public int getPort() {
        return underlyingServer.getActualPort();
    }

    public boolean isSsl(){
        return serverConfig.isSsl();
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
