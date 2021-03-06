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
package io.github.cjstehno.ersatz.socket.server;

import io.github.cjstehno.ersatz.socket.cfg.ConnectionContext;
import io.github.cjstehno.ersatz.socket.impl.InteractionsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.OutputStream;

@RequiredArgsConstructor @Slf4j
class ConnectionContextImpl implements ConnectionContext {

    private final InteractionsImpl interactions;
    private final OutputStream output;

    @Override public void send(final Object message) {
        try {
            log.info("Sending: {}", message);

            // FIXME: throw useful exception (here and below)
            val encoder = interactions.findEncoder(message.getClass()).orElseThrow();
            encoder.encode(message, output);

        } catch (Exception e) {
            // FIXME:
            log.error("Unable to send message ({}): {}", message, e.getMessage(), e);
        }
    }
}