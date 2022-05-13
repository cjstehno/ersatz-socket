package io.github.cjstehno.ersatz.socket.encdec;

import java.io.IOException;
import java.io.InputStream;

@FunctionalInterface
public interface Decoder<T> {

    T decode(final InputStream stream) throws IOException;
}
