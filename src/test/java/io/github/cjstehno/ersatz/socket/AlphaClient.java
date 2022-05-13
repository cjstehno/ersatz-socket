package io.github.cjstehno.ersatz.socket;

import io.github.cjstehno.ersatz.socket.encdec.Decoder;
import io.github.cjstehno.ersatz.socket.encdec.Encoder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * gets message on connection with number
 * sends that number of messages
 * expects each message to get a modified version as a response
 */
@RequiredArgsConstructor @Slf4j
public class AlphaClient {

    private static final Decoder<Integer> CONNECT_MESSAGE_DECODER = stream -> {
        val input = new DataInputStream(stream);
        return input.readInt();
    };
    private static final Encoder MESSAGE_ENCODER = (message, stream) -> {
        val output = new DataOutputStream(stream);
        val bytes = ((String) message).getBytes(UTF_8);
        output.writeInt(bytes.length);
        output.write(bytes);
    };
    private static final Decoder<String> MESSAGE_DECODER = stream -> {
        val data = new DataInputStream(stream);
        val length = data.readInt();
        val bytes = data.readNBytes(length);
        return new String(bytes, UTF_8);
    };

    private final int port;
    @Getter private final Set<String> responses = new CopyOnWriteArraySet<>();

    public void connect() throws IOException {
        try (val socket = new Socket("localhost", port)) {
            log.info("Connected on port {}...", port);

            val input = new BufferedInputStream(socket.getInputStream());
//            int count = CONNECT_MESSAGE_DECODER.decode(input);
            int count = 3;

            log.info("Received connection message - sending {} messages...", count);

            val output = socket.getOutputStream();
            for (int m = 0; m < count; m++) {
                MESSAGE_ENCODER.encode("Message-" + m, output);
            }

            // listen for responses
            for (int m = 0; m < count; m++) {
                val response = MESSAGE_DECODER.decode(input);
                log.info("Response: {}", response);
                responses.add(response);
            }

            log.info("Disconnecting.");
        }
    }
}
