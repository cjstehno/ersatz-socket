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

import lombok.val;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.Date;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

class MultiMessageEncoderTest {

    private static final Encoder stringEncoder = (message, stream) -> stream.write(((String) message).getBytes(UTF_8));
    private static final Encoder longEncoder = (message, stream) -> stream.write(BigInteger.valueOf((long) message).toByteArray());

    @Test void multiMessageEncoder() throws Exception {
        val encoder = MultiMessageEncoder.byMessageType()
            .encoderFor(String.class, stringEncoder)
            .encoderFor(Long.class, longEncoder);

        // test the string
        try (val out = new ByteArrayOutputStream()) {
            encoder.encode("Hello!", out);
            assertArrayEquals("Hello!".getBytes(UTF_8), out.toByteArray());
        }

        // test the long
        try (val out = new ByteArrayOutputStream()) {
            encoder.encode(8675309L, out);
            assertArrayEquals(BigInteger.valueOf(8675309L).toByteArray(), out.toByteArray());
        }

        // test non-existing
        try (val out = new ByteArrayOutputStream()) {
            val thrown = assertThrows(CodecUnavailableException.class, () -> encoder.encode(new Date(), out));
            assertEquals("No encoder exists for message type (Date).", thrown.getMessage());
        }
    }
}