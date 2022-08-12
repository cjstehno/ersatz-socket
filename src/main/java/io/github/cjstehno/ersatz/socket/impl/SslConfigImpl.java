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
