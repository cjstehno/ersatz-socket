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
package io.github.cjstehno.ersatz.socket.encdec;

import lombok.NoArgsConstructor;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * An <code>Encoder</code> that allows for simple configuration of specific encoders based on the message type.
 */
@NoArgsConstructor(staticName = "byMessageType")
public class MultiMessageEncoder implements Encoder {

    private final Map<Class<?>, Encoder> encoders = new HashMap<>();

    /**
     * Registers an encoder for a specific message type.
     *
     * @param messageType the message type to map the encoder
     * @param encoder     the mapped encoder
     * @return a reference to the multi-message encoder (for additional configuration)
     */
    public MultiMessageEncoder encoderFor(final Class<?> messageType, final Encoder encoder) {
        encoders.put(messageType, encoder);
        return this;
    }

    /**
     * Finds the encoder for the type assignable from the incoming message type. The first match found will be used. If
     * no encoder is matched, an exception will be thrown.
     *
     * @param message the message to be encoded
     * @param stream  the output stream where the message will be written
     * @throws IOException               if there is a problem encoding the message
     * @throws CodecUnavailableException if the message-type encoder is not found
     */
    @Override public void encode(final Object message, final OutputStream stream) throws IOException {
        encoders.entrySet().stream()
            .filter(ent -> message.getClass().isAssignableFrom(ent.getKey()))
            .map(Map.Entry::getValue)
            .findAny()
            .orElseThrow(() -> new CodecUnavailableException(
                "No encoder exists for message type (" + message.getClass().getSimpleName() + ")."
            ))
            .encode(message, stream);
    }
}
