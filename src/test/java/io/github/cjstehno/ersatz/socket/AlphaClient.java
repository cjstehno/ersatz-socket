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
package io.github.cjstehno.ersatz.socket;

import io.github.cjstehno.ersatz.socket.BravoClient.BravoMessage;
import io.github.cjstehno.ersatz.socket.encdec.Decoder;
import io.github.cjstehno.ersatz.socket.encdec.Encoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static java.nio.charset.StandardCharsets.US_ASCII;

/**
 * gets message on connection with number
 * sends that number of messages
 * expects each message to get a modified version as a response
 */
@RequiredArgsConstructor @Slf4j
public class AlphaClient {

    // FIXME: needs ssl support

    private static final Encoder ENCODER = (message, stream) -> {
        stream.write(((String) message).getBytes(US_ASCII));
        stream.flush();
    };
    private static final Decoder<String> DECODER = stream -> {
        val buffer = new StringBuilder();

        int x = stream.read();
        while (x != -1) {
            char ch = (char) x;
            if (ch != '\n') {
                buffer.append(ch);
                x = stream.read();
            } else {
                x = stream.read();
                break;
            }
        }

        return buffer.toString();
    };

    private List<Consumer<BravoMessage>> onMessageListeners = new LinkedList<>();
    private final int port;
    private Socket socket;
    private ExecutorService executor;
    private OutputStream outputStream;

    public void connect() throws IOException {
        executor = Executors.newCachedThreadPool();

        socket = new Socket("localhost", port);
        log.info("Connected on port {}...", port);

        // start listening for incoming messages
        executor.submit(() -> {
            // FIXME: better
            while(true) {
                try {
                    val input = socket.getInputStream();
                    val message = BravoMessage.from(DECODER.decode(input));
                    onMessage(message);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void onMessage(final Consumer<BravoMessage> consumer) {
        onMessageListeners.add(consumer);
    }

    public void send(final BravoMessage message) {
        try {
            ENCODER.encode(message.toMessage(), socket.getOutputStream());
        } catch (IOException e) {
            log.error("Unable to write: {}", e.getMessage(), e);
        }
    }

    public void disconnect() {
        if (executor != null) {
            executor.shutdownNow();
        }

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                // ignore
            }
        }
        log.info("Disconnected.");
    }

    private void onMessage(final BravoMessage message) {
        onMessageListeners.forEach(li -> li.accept(message));
    }
}
