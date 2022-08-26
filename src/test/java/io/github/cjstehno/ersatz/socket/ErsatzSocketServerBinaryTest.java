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
import io.github.cjstehno.ersatz.socket.junit.ErsatzSocketServerExtension;
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
class ErsatzSocketServerBinaryTest {

    // FIXME: this is a prototype for binary client support

    private ErsatzSocketServer server = new ErsatzSocketServer(cfg -> {
        cfg.ssl();

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

    @Test void binaryClient() throws Exception {
        server.interactions(ix -> {
            ix.onConnect(ctx -> ctx.send("send: 3\n"));

            ix.onMessage(startsWith("message:"), (ctx, message) -> {
                ctx.send("reply: " + message.substring(message.indexOf(':') + 1) + "\n");
            });
        });

        val replyCount = new AtomicInteger(0);

        val client = new ErsatzSocketClient(cfg -> {
            cfg.port(server.getPort());
            cfg.ssl(server.isSsl());
//            cfg.decoder();
//            cfg.encoder();
        });

        client.onConnect(ctx -> {
            System.out.println("connected handled");
        });

        client.onMessage((ctx, message) -> {
            System.out.println("handling message: " + message);
        });

        client.connect();

        await().untilAtomic(replyCount, equalTo(3));

        client.disconnect();
    }
}