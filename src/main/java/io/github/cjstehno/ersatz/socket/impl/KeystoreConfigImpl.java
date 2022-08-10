package io.github.cjstehno.ersatz.socket.impl;

import io.github.cjstehno.ersatz.socket.cfg.KeystoreConfig;
import lombok.Getter;

import java.net.URL;

public class KeystoreConfigImpl implements KeystoreConfig {

    @Getter private URL location;
    @Getter private char[] password = DEFAULT_PASSWORD.toCharArray();

    @Override public KeystoreConfig location(URL location) {
        this.location = location;
        return this;
    }

    @Override public KeystoreConfig password(char[] password) {
        this.password = password;
        return this;
    }
}
