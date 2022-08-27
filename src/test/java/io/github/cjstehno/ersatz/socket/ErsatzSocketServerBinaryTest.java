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

import io.github.cjstehno.ersatz.socket.client.ErsatzSocketClient;
import io.github.cjstehno.ersatz.socket.client.TestMessage;
import io.github.cjstehno.ersatz.socket.fixtures.BinaryCodec;
import io.github.cjstehno.ersatz.socket.junit.ErsatzSocketServerExtension;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.atomic.AtomicInteger;

import static io.github.cjstehno.ersatz.socket.client.TestMessage.MessageType.*;
import static io.github.cjstehno.ersatz.socket.client.TestMessage.TypeMatcher.ofType;
import static java.lang.Integer.parseInt;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.equalTo;

@ExtendWith(ErsatzSocketServerExtension.class) @Slf4j
class ErsatzSocketServerBinaryTest {

    // FIXME: this is a prototype for binary client support

    private ErsatzSocketServer server = new ErsatzSocketServer(cfg -> {
        cfg.ssl();

        cfg.encoder(TestMessage.class, BinaryCodec.ENCODER);
        cfg.decoder(BinaryCodec.DECODER);
    });

    @Test void binaryClient() throws Exception {
        server.interactions(ix -> {
            ix.onConnect(ctx -> ctx.send(new TestMessage(SEND, "3")));

            ix.onMessage(ofType(MESSAGE), (ctx, message) -> ctx.send(new TestMessage(REPLY, message.getContent())));
        });

        val replyCount = new AtomicInteger(0);

        val client = new ErsatzSocketClient<TestMessage>(cfg -> {
            cfg.port(server.getPort());
            cfg.ssl(server.isSsl());
            cfg.decoder(BinaryCodec.DECODER);
            cfg.encoder(BinaryCodec.ENCODER);
        });

        // when I get the "send" message -> send that number of messages
        client.onMessage((sender, message) -> {
            switch (message.getType()) {
                case SEND -> {
                    log.info("Sending {} replies", message.getContent());
                    for (int i = 0; i < parseInt(message.getContent()); i++) {
                        sender.send(new TestMessage(MESSAGE, "value-" + i));
                    }
                }
                case REPLY -> {
                    log.info("Client-Received-Reply: {}", message);
                    replyCount.incrementAndGet();
                }
                default -> throw new IllegalArgumentException("Unknown message: " + message);
            }
        });

        client.connect();

        await().untilAtomic(replyCount, equalTo(3));

        client.disconnect();
    }
}