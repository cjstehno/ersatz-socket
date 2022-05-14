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

import io.github.cjstehno.ersatz.socket.impl.InteractionsImpl;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErsatzSocketServerTest {

    /*
        // encoders/decoders can be defined in config or interactions block (overrides)

        val server = new ErsatzSocketServer();
        server.interactions(cfg -> {
            cfg.encoder(int.class, ...); // writes int to output stream
            cfg.decoder(String.class, ...); // reads string from input stream

            cfg.onConnect(session -> {
                // int.class encoder will be used
                session.send(10);
                // do other stuff
            });

            // number of message (type may be part of matcher?)
            cfg.onMessage(matching, (session, message) -> {
                session.send("a response");
                // do stuff
            })
        });

        how to account for multiple onMessage matchers - dont want to corrupt message buffer
        matcher trys to decode message and match its data - if it matches the decoded message is passed to the consumer
     */

    private ErsatzSocketServer server;

    @BeforeEach void beforeEach() {
        server = new ErsatzSocketServer(cfg -> {
            // encode the response to the connection event
            cfg.encoder(Integer.class, (message, stream) -> {
                val out = new DataOutputStream(stream);
                out.writeInt((int) message);
                out.flush();
            });

            // decode the incoming message
            cfg.decoder(String.class, stream -> {
                val input = new DataInputStream(stream);
                val length = input.readInt();
                val bytes = input.readNBytes(length);
                return new String(bytes, UTF_8);
            });

            // encode the outgoing response message
            cfg.encoder(String.class, (message, stream) -> {
                val out = new DataOutputStream(stream);
                val bytes = ((String)message).getBytes(UTF_8);
                out.writeInt(bytes.length);
                out.write(bytes);
            });
        });
    }

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

        assertEquals(3, client.getResponses().size());
        assertTrue(client.getResponses().containsAll(Set.of(
            "Message-0-modified", "Message-1-modified", "Message-2-modified"
        )));
    }

    @AfterEach void afterEach() throws IOException {
        server.close();
    }
}