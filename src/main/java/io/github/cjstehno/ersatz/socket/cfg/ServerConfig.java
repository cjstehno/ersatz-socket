/**
 * Copyright (C) 2022 Christopher J. Stehno
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.cjstehno.ersatz.socket.cfg;

import io.github.cjstehno.ersatz.socket.encdec.Decoder;
import io.github.cjstehno.ersatz.socket.encdec.Encoder;

import java.util.function.Consumer;

public interface ServerConfig {

    // FIXME: ssl?
    // FIXME: helper decoder for decoding multiple messages based on an "envelope"

    /**
     * Configures the server port to be used. Generally, you should not set this unless you really need to (and know
     * what you are doing). The default of 0 will find a "random" available port to use.
     *
     * By overriding the default you can set yourself up for port collisions.
     *
     * @param value the port value
     * @return a reference to this config
     */
    ServerConfig port(final int value);

    /**
     * Configures the number of worker threads (a single thread will also be started for the server itself). The default
     * value of 0 will allow threads to be created as needed (cached for reuse), which is recommended.
     *
     * @param value the worker thread count
     * @return a reference to this config
     */
    ServerConfig workerThreads(final int value);

    /**
     * Configures the auto-starting of the server after interactions are applied. The default value is true, so you
     * really only need to disable this feature - note that you will need to call the <pre>start()</pre> method in
     * your code if you do so.
     *
     * @param enabled whether the auto-start is enabled
     * @return a reference to this config
     */
    ServerConfig autoStart(boolean enabled);

    /**
     *
     *
     * @param encoder the encoder
     * @return a reference to this config
     */
    ServerConfig encoder(final Encoder encoder);

    /**
     *
     * @param messageType the request message type
     * @param decoder the decoder
     * @param <T> the message type
     * @return a reference to this config
     */
    <T> ServerConfig decoder(final Decoder<T> decoder);

    /**
     * Configures the interactions that are supported with the server (along with responses).
     *
     * @param consumer the configuration consumer
     * @return a reference to this config
     */
    ServerConfig interactions(final Consumer<Interactions> consumer);

    /// FIXME: reportToConsole - is there anything to report?
}
