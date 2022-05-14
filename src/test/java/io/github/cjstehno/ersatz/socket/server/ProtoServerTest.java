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

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class ProtoServerTest {

    private ProtoServer server;

    @BeforeEach void beforeEach() {
        server = new ProtoServer().start(true);
    }

    @AfterEach void afterEach() {
        server.stop();
    }

    @Test void running() throws IOException {
        log.info("Starting test...");

        val encoder = new MessageEncoder();
        val decoder = new ProtoServer.MessageDecoder();
        val connectDecoder = new ConnectMessageDecoder();

        val responses = new CopyOnWriteArraySet<String>();

        try (val socket = new Socket("localhost", server.getPort())) {
            log.info("Connected...");

            val input = new BufferedInputStream(socket.getInputStream());
            int count = connectDecoder.decode(input);

            log.info("Received connection message - sending {} messages...", count);

            val output = socket.getOutputStream();
            for (int m = 0; m < count; m++) {
                encoder.encode("Message-" + m, output);
            }

            // listen for responses
            for (int m = 0; m < count; m++) {
                val response = decoder.decode(input);
                log.info("Response: {}", response);
                responses.add(response);
            }
        }

        log.info("Done with test.");

        assertEquals(10, responses.size());
        assertTrue(responses.containsAll(Set.of(
            "Message-0-modified", "Message-1-modified", "Message-2-modified", "Message-3-modified",
            "Message-4-modified", "Message-5-modified", "Message-6-modified", "Message-7-modified",
            "Message-8-modified", "Message-9-modified"
        )));
    }

    private static class MessageEncoder {

        public void encode(final String message, final OutputStream stream) throws IOException {
            val output = new DataOutputStream(stream);
            val bytes = message.getBytes(UTF_8);
            output.writeInt(bytes.length);
            output.write(bytes);
        }
    }

    private static class ConnectMessageDecoder {

        public int decode(final InputStream stream) throws IOException {
            val input = new DataInputStream(stream);
            return input.readInt();
        }
    }
}
