package io.github.cjstehno.ersatz.socket.impl;

import io.github.cjstehno.ersatz.socket.cfg.SslConfig;
import lombok.Getter;

import java.net.URL;

public class SslConfigImpl implements SslConfig {

    @Getter private URL keystoreLocation;
    @Getter private URL truststoreLocation;
    @Getter private String keystorePassword = DEFAULT_PASSWORD;
    @Getter private String truststorePassword = DEFAULT_PASSWORD;

    @Override public SslConfig keystoreLocation(URL location) {
        this.keystoreLocation = location;
        return this;
    }

    @Override public SslConfig truststoreLocation(URL url) {
        this.truststoreLocation = url;
        return this;
    }

    @Override public SslConfig keystorePassword(String password) {
        this.keystorePassword = password;
        return this;
    }

    @Override public SslConfig truststorePassword(String password) {
        this.truststorePassword = password;
        return this;
    }
}
