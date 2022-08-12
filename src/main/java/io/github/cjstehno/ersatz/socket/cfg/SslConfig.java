package io.github.cjstehno.ersatz.socket.cfg;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

// FIXME: document
public interface SslConfig {

    String DEFAULT_PASSWORD = "ersatz";

    default SslConfig keystoreLocation(final String keystore) {
        try {
            return keystoreLocation(new URL(keystore));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    default SslConfig keystoreLocation(final URI uri) {
        try {
            return keystoreLocation(uri.toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    SslConfig keystoreLocation(final URL url);

    SslConfig keystorePassword(final String password);

    default SslConfig truststoreLocation(final String truststore) {
        try {
            return truststoreLocation(new URL(truststore));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    default SslConfig truststoreLocation(final URI truststore) {
        try {
            return truststoreLocation(truststore.toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    SslConfig truststoreLocation(final URL url);

    SslConfig truststorePassword(final String password);
}
