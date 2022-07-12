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

import io.github.cjstehno.ersatz.socket.junit.ErsatzSocketServerExtension;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(ErsatzSocketServerExtension.class)
class ErsatzSocketServerTest {

    // FIXME: refactor so that there is only 1 decoder/encoder - though they may handle multiple message tyeps

    private ErsatzSocketServer server = new ErsatzSocketServer(cfg -> {
        // encode the response to the connection event
        cfg.encoder((message, stream) -> {
            if (message instanceof Integer) {
                val out = new DataOutputStream(stream);
                out.writeInt((int) message);
                out.flush();

            } else if (message instanceof String) {
                val out = new DataOutputStream(stream);
                val bytes = ((String) message).getBytes(UTF_8);
                out.writeInt(bytes.length);
                out.write(bytes);
            } else {
                throw new UnsupportedOperationException("Unsupported message type: " + message.getClass().getSimpleName());
            }
        });

        cfg.decoder(stream -> {
            val input = new DataInputStream(stream);
            val length = input.readInt();
            val bytes = input.readNBytes(length);
            return new String(bytes, UTF_8);
        });
    });

    @Test void usage() throws IOException {
        server.interactions(ix -> {
            ix.onConnect(ctx -> {
                ctx.send(3);
            });

            ix.onMessage(startsWith("Message-"), (ctx, message) -> {
                ctx.send(message + "-modified");
            });
        });

        val client = new AlphaClient(server.getPort());
        client.connect();

        val responses = client.getResponses();
        assertEquals(3, responses.size());
        assertTrue(responses.containsAll(Set.of(
            "Message-0-modified", "Message-1-modified", "Message-2-modified"
        )));
    }

    @Test void usageWithMultipleBlocks() throws IOException {
        server.interactions(ix -> {
            ix.onConnect(ctx -> {
                ctx.send(2);
            });

            ix.onMessage(equalTo("Message-0"), (ctx, message) -> {
                ctx.send("response-0");
            });

            ix.onMessage(equalTo("Message-1"), (ctx, message) -> {
                ctx.send("response-1");
            });
        });

        val client = new AlphaClient(server.getPort());
        client.connect();

        val responses = client.getResponses();
        assertEquals(2, responses.size());
        assertTrue(responses.containsAll(Set.of("response-0", "response-1")));
    }

    @Test void usageWithMore() throws IOException {
        server.interactions(ix -> {
            ix.onConnect(ctx -> {
                ctx.send(5);
            });

            ix.onMessage(startsWith("Message-"), (ctx, message) -> {
                ctx.send(message + "-modified");
            });
        });

        val client = new AlphaClient(server.getPort());
        client.connect();

        val responses = client.getResponses();
        assertEquals(5, responses.size());
        assertTrue(responses.containsAll(Set.of(
            "Message-0-modified", "Message-1-modified", "Message-2-modified", "Message-3-modified", "Message-4-modified"
        )));
    }
}