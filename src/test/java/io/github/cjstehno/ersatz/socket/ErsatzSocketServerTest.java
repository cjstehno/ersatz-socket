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

import io.github.cjstehno.ersatz.socket.client.ErsatzSocketClient;
import io.github.cjstehno.ersatz.socket.client.TestMessage;
import io.github.cjstehno.ersatz.socket.encdec.Decoder;
import io.github.cjstehno.ersatz.socket.encdec.Encoder;
import io.github.cjstehno.ersatz.socket.fixtures.BinaryCodec;
import io.github.cjstehno.ersatz.socket.fixtures.TextCodec;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;

import static io.github.cjstehno.ersatz.socket.client.TestMessage.MessageType.MESSAGE;
import static io.github.cjstehno.ersatz.socket.client.TestMessage.TypeMatcher.ofType;
import static io.github.cjstehno.ersatz.socket.client.TestMessage.*;
import static java.lang.Integer.parseInt;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertTrue;

// TODO: any mina settings to expose or change defauls of
// TODO: how does this decision promote any refactoring of codecs?

@Slf4j
class ErsatzSocketServerTextTest {

    private ErsatzSocketServer server;

    private void setupServer(final boolean ssl, final Decoder<TestMessage> decoder, final Encoder encoder) {
        server = new ErsatzSocketServer(cfg -> {
            cfg.ssl(ssl);

            cfg.decoder(decoder);
            cfg.encoder(TestMessage.class, encoder);
        });
        log.info("Setup the server.");
    }

    // FIXME: pull this into a separate extension for just doing this
    @AfterEach
    public void afterEach() throws Exception {
        if (server != null) {
            server.close();
            server = null;
            log.info("Tore-down the server.");
        }
    }

    @ParameterizedTest(name = "{index}: general usage - {0}")
    @MethodSource("argumentsProvider") @SuppressWarnings("unused")
    void generalUsage(final String label, final boolean ssl, final Decoder<TestMessage> decoder, final Encoder encoder) throws Exception {
        setupServer(ssl, decoder, encoder);

        val messageCount = 3;

        server.interactions(ix -> {
            ix.onConnect(ctx -> ctx.send(createSend(String.valueOf(messageCount))));
            ix.onMessage(ofType(MESSAGE), (ctx, message) -> ctx.send(createReply(message.getContent())));
        });

        val client = new ErsatzSocketClient<TestMessage>(cfg -> {
            cfg.port(server.getPort());
            cfg.ssl(ssl);
            cfg.decoder(decoder);
            cfg.encoder(encoder);
        });

        try {
            val latch = new CountDownLatch(messageCount);

            // when I get the "send" message -> send that number of messages
            client.onMessage((sender, message) -> {
                switch (message.getType()) {
                    case SEND -> {
                        try {
                            log.info("Sending {} replies", message.getContent());
                            for (int i = 0; i < parseInt(message.getContent()); i++) {
                                sender.send(createMessage("value-" + i));
                            }
                        } catch (Exception ex) {
                            log.error("Problem sending messages from client: {}", ex.getMessage(), ex);
                        }
                    }
                    case REPLY -> {
                        try {
                            log.info("Client-Received-Reply: {}", message);
                            latch.countDown();
                        } catch (Exception ex) {
                            log.error("Problem handling server reply on client: {}", ex.getMessage(), ex);
                        }
                    }
                    default -> throw new IllegalArgumentException("Unknown message: " + message);
                }
            });

            client.connect();

            assertTrue(latch.await(30, SECONDS), "Timed-out before receiving expected replies.");

        } finally {
            client.disconnect();
        }
    }

    // FIXME: issue
    private static Stream<Arguments> argumentsProvider() {
        return Stream.of(
            // text
            Arguments.of("text codec without ssl", false, TextCodec.DECODER, TextCodec.ENCODER),
            Arguments.of("text codec with ssl", true, TextCodec.DECODER, TextCodec.ENCODER)/*,

            // binary
            Arguments.of("binary codec without ssl", false, BinaryCodec.DECODER, BinaryCodec.ENCODER),
            Arguments.of("binary codec with ssl", true, BinaryCodec.DECODER, BinaryCodec.ENCODER)*/
        );
    }
}
