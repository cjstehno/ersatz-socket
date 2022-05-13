package io.github.cjstehno.ersatz.socket.cfg;

import java.io.IOException;

public interface ConnectionContext {

    // FIXME: handle errors
    void send(final Object message);
}
