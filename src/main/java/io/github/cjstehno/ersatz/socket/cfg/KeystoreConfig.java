package io.github.cjstehno.ersatz.socket.cfg;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

// FIXME: document
public interface KeystoreConfig {

    String DEFAULT_PASSWORD = "ersatz";

    default KeystoreConfig location(final String keystore) {
        try {
            return location(new URL(keystore));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    default KeystoreConfig location(final URI uri) {
        try {
            return location(uri.toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    KeystoreConfig location(final URL url);

    default KeystoreConfig password(final String password) {
        return password(password.toCharArray());
    }

    KeystoreConfig password(final char[] password);
}
