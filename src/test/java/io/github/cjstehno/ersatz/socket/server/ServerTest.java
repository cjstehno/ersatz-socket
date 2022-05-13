package io.github.cjstehno.ersatz.socket.server;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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

        try (val socket = new Socket("localhost", 10101)) {
            log.info("Connected - sending data...");

            try (val output = new BufferedOutputStream(socket.getOutputStream())) {
                encoder.encode("Alpha", output);
                encoder.encode("Bravo", output);
                encoder.encode("Charlie", output);
                output.flush();
            }
        }

        log.info("Done with test.");
    }

    private static class MessageEncoder {

        public void encode(final String message, final OutputStream stream) throws IOException {
            val output = new DataOutputStream(stream);
            val bytes = message.getBytes(UTF_8);
            output.writeInt(bytes.length);
            output.write(bytes);
        }
    }

    /*
        request / response :
            int - length
            bytes - string (utf-8)

     */
}
