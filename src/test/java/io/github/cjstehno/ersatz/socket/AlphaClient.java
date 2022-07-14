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
package io.github.cjstehno.ersatz.socket;

import io.github.cjstehno.ersatz.socket.encdec.Decoder;
import io.github.cjstehno.ersatz.socket.encdec.Encoder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * gets message on connection with number
 * sends that number of messages
 * expects each message to get a modified version as a response
 */
@RequiredArgsConstructor @Slf4j
public class AlphaClient {

    private static final Decoder<Integer> CONNECT_MESSAGE_DECODER = stream -> {
        val input = new DataInputStream(stream);
        return input.readInt();
    };
    private static final Encoder MESSAGE_ENCODER = (message, stream) -> {
        val output = new DataOutputStream(stream);
        val bytes = ((String) message).getBytes(UTF_8);
        output.writeInt(bytes.length);
        output.write(bytes);
    };
    private static final Decoder<String> MESSAGE_DECODER = stream -> {
        val data = new DataInputStream(stream);
        val length = data.readInt();
        val bytes = data.readNBytes(length);
        return new String(bytes, UTF_8);
    };

    private final int port;
    @Getter private final Set<String> responses = new CopyOnWriteArraySet<>();

    public void connect() throws IOException {
        log.info("Connecting on port {}...", port);
        try (val socket = new Socket("localhost", port)) {
            log.info("Connected on port {}...", port);

            val input = new BufferedInputStream(socket.getInputStream());
            int count = CONNECT_MESSAGE_DECODER.decode(input);

            log.info("Received connection message - sending {} messages...", count);

            val output = socket.getOutputStream();
            for (int m = 0; m < count; m++) {
                MESSAGE_ENCODER.encode("Message-" + m, output);
            }

            // listen for responses
            for (int m = 0; m < count; m++) {
                val response = MESSAGE_DECODER.decode(input);
                log.info("Response: {}", response);
                responses.add(response);
            }

            log.info("Disconnecting.");
        }
    }
}
