package io.github.cjstehno.ersatz.socket.demo;

import io.github.cjstehno.ersatz.socket.demo.client.DemoClient;
import io.github.cjstehno.ersatz.socket.demo.server.DemoServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
            client.stop();
        }

        if (server != null) {
            server.stop();
        }

        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
    }

    @Test void usage() throws InterruptedException {
        executor.submit(client);

        Thread.sleep(2000);
    }
}