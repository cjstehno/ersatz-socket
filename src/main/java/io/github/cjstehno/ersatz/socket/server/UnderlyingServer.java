package io.github.cjstehno.ersatz.socket.server;

public interface UnderlyingServer {

    /**
     * Used to start the server, if not already started.
     */
    void start();

    /**
     * Used to stop the server, if it has been started.
     */
    void stop();

    /**
     * Used to retrieve the actual applied port for the server.
     *
     * @return the port
     */
    int getActualPort();
}
