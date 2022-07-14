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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * An <code>Encoder</code> that allows for simple configuration of specific encoders based on the message type.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageTypeEncoder implements Encoder {
    // TODO: rename?

    private final Map<Class<?>, Encoder> encoders = new HashMap<>();

    public static MessageTypeEncoder byMessageType() {
        return new MessageTypeEncoder();
    }

    public MessageTypeEncoder encoderFor(final Class<?> messageType, final Encoder encoder) {
        encoders.put(messageType, encoder);
        return this;
    }

    /**
     * Finds the encoder for the type assignable from the incoming message type. The first match found will be used. If
     * no encoder is matched, an exception will be thrown.
     *
     * @param message the message to be encoded
     * @param stream  the output stream where the message will be written
     * @throws IOException if there is a problem encoding the message
     */
    @Override public void encode(final Object message, final OutputStream stream) throws IOException {
        // TODO: better exception?
        findEncoder(message.getClass()).orElseThrow().encode(message, stream);
    }

    private Optional<Encoder> findEncoder(final Class<?> messageType) {
        return encoders.entrySet().stream()
            .filter(ent -> messageType.isAssignableFrom(ent.getKey()))
            .map(Map.Entry::getValue)
            .findAny();
    }
}
