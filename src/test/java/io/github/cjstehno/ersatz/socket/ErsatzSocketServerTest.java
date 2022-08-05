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

import io.github.cjstehno.ersatz.socket.junit.ErsatzSocketServerExtension;
import io.github.cjstehno.ersatz.socket.server.mina.MinaUnderlyingServer;
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

    private ErsatzSocketServer server = new ErsatzSocketServer(cfg -> {
/*        cfg.encoder(
            byMessageType()
                .encoderFor(Integer.class, (message, stream) -> {
                    val out = new DataOutputStream(stream);
                    out.writeInt((int) message);
                    out.flush();
                })
                .encoderFor(String.class, (message, stream) -> {
                    val out = new DataOutputStream(stream);
                    val bytes = ((String) message).getBytes(UTF_8);
                    out.writeInt(bytes.length);
                    out.write(bytes);
                })
        );*/

        // TODO: make parallel set of tests with both implementations
//        cfg.underlyingServer(IoUnderlyingServer.class);
        cfg.server(MinaUnderlyingServer.class);

        cfg.encoder(Integer.class, (message, stream) -> {
            val out = new DataOutputStream(stream);
            out.writeInt((int) message);
            out.flush();
        });
        cfg.encoder(String.class, (message, stream) -> {
            val out = new DataOutputStream(stream);
            val bytes = ((String) message).getBytes(UTF_8);
            out.writeInt(bytes.length);
            out.write(bytes);
        });

        cfg.decoder(stream -> {
            val input = new DataInputStream(stream);
            val length = input.readInt();
            val bytes = input.readNBytes(length);
            return new String(bytes, UTF_8);
        });
    });

    @Test void usageAlpha() throws IOException {
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

    @Test void usageBravo() throws Exception {
        server.interactions(ix -> {
            ix.onConnect(ctx -> {
                ctx.send(3);
            });

            ix.onMessage(startsWith("Message-"), (ctx, message) -> {
                ctx.send(message + "-modified");
            });
        });

        val client = new BravoClient(server.getPort());
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