/**
 * Copyright (C) 2022 Christopher J. Stehno
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.cjstehno.ersatz.socket.cfg;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * Configuration values for the SSL support (see <code>ServerConfig::ssl(...)</code> for parent configuration.
 */
public interface SslConfig {

    /**
     * The default password used for the keystore and truststore, if they are not specified.
     */
    String DEFAULT_PASSWORD = "ersatz";

    /**
     * Configures the location (URL) of the keystore to be used.
     *
     * @param keystore the keystore URL
     * @return a reference to this config
     */
    default SslConfig keystoreLocation(final String keystore) {
        try {
            return keystoreLocation(new URL(keystore));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Configures the location (URI) of the keystore to be used.
     *
     * @param uri the keystore URL
     * @return a reference to this config
     */
    default SslConfig keystoreLocation(final URI uri) {
        try {
            return keystoreLocation(uri.toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Configures the location (URL) of the keystore to be used.
     *
     * @param url the keystore URL
     * @return a reference to this config
     */
    SslConfig keystoreLocation(final URL url);

    /**
     * Configures the keystore password.
     *
     * @param password the keystore password
     * @return a reference to this config
     */
    SslConfig keystorePassword(final String password);

    /**
     * Configures the location (URL) of the truststore to be used.
     *
     * @param truststore the truststore URL
     * @return a reference to this config
     */
    default SslConfig truststoreLocation(final String truststore) {
        try {
            return truststoreLocation(new URL(truststore));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Configures the location (URI) of the truststore to be used.
     *
     * @param truststore the truststore URI
     * @return a reference to this config
     */
    default SslConfig truststoreLocation(final URI truststore) {
        try {
            return truststoreLocation(truststore.toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Configures the location (URL) of the truststore to be used.
     *
     * @param url the truststore URL
     * @return a reference to this config
     */
    SslConfig truststoreLocation(final URL url);

    /**
     * Configures the truststore password.
     *
     * @param password the keystore password
     * @return a reference to this config
     */
    SslConfig truststorePassword(final String password);
}
