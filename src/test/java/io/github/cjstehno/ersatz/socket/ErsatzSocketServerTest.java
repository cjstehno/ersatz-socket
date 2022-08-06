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
import io.github.cjstehno.ersatz.socket.server.jio.JioUnderlyingServer;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Integer.parseInt;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;

@ExtendWith(ErsatzSocketServerExtension.class) @Slf4j
class ErsatzSocketServerTest {

    // FIXME: write this with ONLY mina implementation - dont write your own socket server framework!!!!
    // FIXME: write two or three different "protocols" to test with along with fixtures
    // TODO: any mina settings to expose or change defauls of
    // TODO: how does this decision promote any refactoring of codecs?
    // TODO: side-research - http support with an http client - could ersatz just be a special case of this?

    private ErsatzSocketServer server = new ErsatzSocketServer(cfg -> {
        cfg.server(JioUnderlyingServer.class);

        cfg.encoder(String.class, (message, stream) -> {
            stream.write(((String) message).getBytes(US_ASCII));
            stream.flush();
        });

        cfg.decoder(stream -> {
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
        });
    });

    /*
    On connection the server asks for 3 messages.
    When the client receives the connection message it sends the 3 messages
    When the server receives the 3 messages it replies and the client reads them.
    */
    @Test void usageJioClient() throws Exception {
        server.interactions(ix -> {
            ix.onConnect(ctx -> ctx.send("send: 3\n"));

            ix.onMessage(CoreMatchers.startsWith("message:"), (ctx, message) -> {
                ctx.send("reply: " + message.substring(message.indexOf(':') + 1) + "\n");
            });
        });

        val replyCount = new AtomicInteger(0);
        val client = new AlphaClient(server.getPort());

        // when I get the "send" message -> send that number of messages
        client.onMessage(message -> {
            switch (message.getPrefix()) {
                case "send" -> {
                    for (int i = 0; i < parseInt(message.getValue()); i++) {
                        client.send(new BravoClient.BravoMessage("message", "value-" + i));
                    }
                }
                case "reply" -> {
                    log.info("Client-Received-Reply: {}", message);
                    replyCount.incrementAndGet();
                }
                default -> {
                    throw new IllegalArgumentException("Unknown message: " + message);
                }
            }
        });

        client.connect();

        await().untilAtomic(replyCount, equalTo(3));

        client.disconnect();
    }

    /*
    On connection the server asks for 3 messages.
    When the client receives the connection message it sends the 3 messages
    When the server receives the 3 messages it replies and the client reads them.
 */
    @Test void usageMinaClient() throws Exception {
        server.interactions(ix -> {
            ix.onConnect(ctx -> ctx.send("send: 3\n"));

            ix.onMessage(startsWith("message:"), (ctx, message) -> {
                ctx.send("reply: " + message.substring(message.indexOf(':') + 1) + "\n");
            });
        });

        val replyCount = new AtomicInteger(0);
        val client = new BravoClient(server.getPort());

        // when I get the "send" message -> send that number of messages
        client.onMessage(message -> {
            switch (message.getPrefix()) {
                case "send" -> {
                    for (int i = 0; i < parseInt(message.getValue()); i++) {
                        client.send(new BravoClient.BravoMessage("message", "value-" + i));
                    }
                }
                case "reply" -> {
                    log.info("Client-Received-Reply: {}", message);
                    replyCount.incrementAndGet();
                }
                default -> {
                    throw new IllegalArgumentException("Unknown message: " + message);
                }
            }
        });

        client.connect();

        await().untilAtomic(replyCount, equalTo(3));

        client.disconnect();
    }
}