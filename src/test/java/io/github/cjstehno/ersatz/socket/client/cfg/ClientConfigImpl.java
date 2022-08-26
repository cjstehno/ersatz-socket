package io.github.cjstehno.ersatz.socket.client.cfg;

import io.github.cjstehno.ersatz.socket.cfg.SslConfig;
import io.github.cjstehno.ersatz.socket.encdec.CodecUnavailableException;
import io.github.cjstehno.ersatz.socket.encdec.Decoder;
import io.github.cjstehno.ersatz.socket.encdec.Encoder;
import io.github.cjstehno.ersatz.socket.impl.SslConfigImpl;
import lombok.Getter;

import java.util.function.Consumer;

public class ClientConfigImpl implements ClientConfig {

    @Getter private boolean ssl;
    @Getter private SslConfigImpl sslConfig;
    @Getter private int port;
    private Encoder encoder;
    private Decoder<?> decoder;

    @Override public ClientConfig ssl(boolean enabled, Consumer<SslConfig> config) {
        this.ssl = enabled;
        this.sslConfig = new SslConfigImpl();
        config.accept(sslConfig);
        return this;
    }

    @Override public ClientConfig port(int value) {
        this.port = value;
        return this;
    }

    @Override public ClientConfig encoder(Encoder encoder) {
        this.encoder = encoder;
        return this;
    }

    @Override public <T> ClientConfig decoder(Decoder<T> decoder) {
        this.decoder = decoder;
        return this;
    }

    public Encoder encoder() {
        if (encoder != null) {
            return encoder;
        } else {
            throw new CodecUnavailableException("No encoder has been configured.");
        }
    }

    public Decoder<?> decoder() {
        if (decoder != null) {
            return decoder;
        } else {
            throw new CodecUnavailableException("No decoder has been configured.");
        }
    }
}
