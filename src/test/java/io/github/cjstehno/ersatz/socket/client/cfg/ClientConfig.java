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
package io.github.cjstehno.ersatz.socket.client.cfg;

import io.github.cjstehno.ersatz.socket.cfg.ServerConfig;
import io.github.cjstehno.ersatz.socket.cfg.SslConfig;
import io.github.cjstehno.ersatz.socket.encdec.Decoder;
import io.github.cjstehno.ersatz.socket.encdec.Encoder;

import java.util.function.Consumer;

public interface ClientConfig {

    /**
     * Configures the client to enable SSL with the default configurations.
     *
     * @return a reference to this config
     */
    default ClientConfig ssl() {
        return ssl(true);
    }

    /**
     * Configures the client to enable SSL with the provided configurations.
     *
     * @return a reference to this config
     */
    default ClientConfig ssl(final Consumer<SslConfig> config) {
        return ssl(true, config);
    }

    /**
     * Configures the client to enable or disable SSL, with the default configurations (when enabled).
     *
     * @return a reference to this config
     */
    default ClientConfig ssl(final boolean enabled) {
        return ssl(enabled, ks -> {
        });
    }

    /**
     * Configures the client to enable or disable SSL, with the provided configurations (when enabled).
     *
     * @return a reference to this config
     */
    ClientConfig ssl(final boolean enabled, final Consumer<SslConfig> config);

    /**
     * Configures the client port to be used for connecting to the server.
     *
     * @param value the port value
     * @return a reference to this config
     */
    ClientConfig port(final int value);

    /**
     * FIXME: document
     *
     * @param encoder the encoder
     * @return a reference to this config
     */
    ClientConfig encoder(final Encoder encoder);

    /**
     * FIXME: document
     *
     * @param decoder the decoder
     * @param <T>     the message type
     * @return a reference to this config
     */
    <T> ClientConfig decoder(final Decoder<T> decoder);
}
