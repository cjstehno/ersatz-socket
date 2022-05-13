package io.github.cjstehno.ersatz.socket.demo;

import io.github.cjstehno.ersatz.socket.demo.client.DemoClient;
import io.github.cjstehno.ersatz.socket.demo.server.DemoServer;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
class DemoServerTest {

    private ExecutorService executor;
    private DemoServer server;
    private DemoClient client;

    @BeforeEach void beforeEach() {
        executor = Executors.newCachedThreadPool();

        server = new DemoServer(10101);
        executor.submit(server);

        client = new DemoClient("localhost", 10101);
    }

    @AfterEach void afterEach() throws InterruptedException {
        if (client != null) {
            client.disconnect();
        }

        if (server != null) {
            server.stop();
        }

        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
    }

    @Test void usage() throws InterruptedException {
        client.connect();
        log.info("Done.");
    }

    @Test void customClient() throws Exception {
        Thread.sleep(1000);

        val clientSkt = new Socket("127.0.0.1", 10101);
        val inputStream = new DataInputStream(clientSkt.getInputStream());
        val running = new AtomicBoolean(true);

        executor.submit(() -> {
            while (running.get()) {
                try {
                    val value = inputStream.readInt();
                    log.info("Response: {}", value);
                } catch (IOException e) {
                    log.error("Error: {}", e.getMessage(), e);
                }
            }
        });

        try (val output = new DataOutputStream(clientSkt.getOutputStream())) {
            log.info("Sending request...");
            output.writeInt(42);

            val stringBytes = "the answer to everything".getBytes(UTF_8);
            output.writeInt(stringBytes.length);
            output.write(stringBytes);

            output.flush();
        }

        Thread.sleep(1000);

        running.set(false);
    }
}