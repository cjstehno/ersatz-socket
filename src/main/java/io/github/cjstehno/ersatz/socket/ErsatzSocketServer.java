package io.github.cjstehno.ersatz.socket;

import io.github.cjstehno.ersatz.socket.cfg.Interactions;
import io.github.cjstehno.ersatz.socket.cfg.ServerConfig;
import io.github.cjstehno.ersatz.socket.impl.ServerConfigImpl;
import io.github.cjstehno.ersatz.socket.server.UnderlyingServer;
import io.github.cjstehno.ersatz.socket.server.UnderlyingServerImpl;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.Consumer;

public class ErsatzSocketServer implements Closeable {

    private final UnderlyingServer underlyingServer;
    private final ServerConfigImpl serverConfig;

    public ErsatzSocketServer() {
        this.serverConfig = new ServerConfigImpl();
        serverConfig.setStarter(this::start);

        this.underlyingServer = new UnderlyingServerImpl(serverConfig);
    }

    public ErsatzSocketServer(final Consumer<ServerConfig> consumer) {
        this();
        if (consumer != null) {
            consumer.accept(serverConfig);
        }
    }

    public int getPort(){
        return underlyingServer.getActualPort();
    }

    // FIXME: maybe an InetAddress helper or something (e.g. localhost+ port)

    public ErsatzSocketServer interactions(final Consumer<Interactions> interactions){
        serverConfig.interactions(interactions);

        if (serverConfig.isAutoStart()) {
            underlyingServer.start();
        }

        return this;
    }

    // FIXME: clearInteractions


    public ErsatzSocketServer start(){
        underlyingServer.start();
        return this;
    }

    public void stop(){
        underlyingServer.stop();
    }


    // FIXME: verify

    @Override public void close() throws IOException {
        stop();
    }
}
