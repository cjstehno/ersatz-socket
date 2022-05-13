package io.github.cjstehno.ersatz.socket.server;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class ServerTest {

    private Server server;

    @BeforeEach void beforeEach() {
        server = new Server(10101, 2);
        server.start();
    }

    @AfterEach void afterEach() {
        server.stop();
    }

    @Test void running() throws IOException {
        log.info("Starting test...");

        val encoder = new MessageEncoder();
        val connectDecoder = new ConnectMessageDecoder();

        try (val socket = new Socket("localhost", 10101)) {
            log.info("Connected...");

            val input = new BufferedInputStream(socket.getInputStream());
            int count = connectDecoder.decode(input);

            log.info("Received connection message - sending {} messages...", count);

            try (val output = new BufferedOutputStream(socket.getOutputStream())) {
                for (int m = 0; m < count; m++) {
                    encoder.encode("Message-" + m, output);
                }
                output.flush();
            }
        }

        log.info("Done with test.");
    }

    /*
        connect
        wait for connection message
        then fire messages
        wait for responses
        verify responses
     */

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
