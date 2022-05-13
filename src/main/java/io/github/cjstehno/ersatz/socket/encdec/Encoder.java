package io.github.cjstehno.ersatz.socket.encdec;

import java.io.IOException;
import java.io.OutputStream;

@FunctionalInterface
public interface Encoder {

    void encode(final Object message, final OutputStream stream) throws IOException;
}
