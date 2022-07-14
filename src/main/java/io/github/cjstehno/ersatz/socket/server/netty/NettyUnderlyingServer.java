package io.github.cjstehno.ersatz.socket.server.netty;

import io.github.cjstehno.ersatz.socket.impl.ServerConfigImpl;
import io.github.cjstehno.ersatz.socket.server.UnderlyingServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static io.netty.channel.ChannelOption.SO_BACKLOG;
import static io.netty.channel.ChannelOption.SO_KEEPALIVE;

// FIXME: wire in
@RequiredArgsConstructor @Slf4j
public class NettyUnderlyingServer implements UnderlyingServer {

    private final ServerConfigImpl serverConfig;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicInteger actualPort = new AtomicInteger();
    private ExecutorService executor;
    private Future<?> serverFuture;

    @Override public void start() {
        if (!running.get()) {
            log.debug("Starting...");

            startThreadPool();

            val latch = new CountDownLatch(1);

            serverFuture = executor.submit(() -> {
                // FIXME: tune the thread counts
                EventLoopGroup bossGroup = new NioEventLoopGroup();
                EventLoopGroup workerGroup = new NioEventLoopGroup();
                try {
                    ServerBootstrap b = new ServerBootstrap();
                    b.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<>() {
                            @Override protected void initChannel(Channel ch) throws Exception {
                                ch.pipeline().addLast(
                                    new NettyErsatzDecoder(serverConfig),
                                    new NettyErsatzEncoder(serverConfig),
                                    new ErsatzHandler(serverConfig)
                                );
                            }
                        })
                        .option(SO_BACKLOG, 128)
                        .childOption(SO_KEEPALIVE, true);

                    ChannelFuture f = b.bind(serverConfig.getPort()).sync();

                    val localPort = ((InetSocketAddress) (f.channel().localAddress())).getPort();
                    actualPort.set(localPort);
                    running.set(true);
                    latch.countDown();

                    log.info("Started on port {}", localPort);

                    f.channel().closeFuture().sync();

                    log.info("Right before catch");

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    log.info("Shutting down.");
                    workerGroup.shutdownGracefully();
                    bossGroup.shutdownGracefully();
                }
            });

            // let's wait for the server to start before moving on
            try {
                latch.await();
            } catch (InterruptedException e) {
                log.warn("Problem waiting for server to start: {}", e.getMessage(), e);
            }
        }
    }

    @Override public void stop() {
        if (running.get()) {
            log.debug("Stopping...");
            running.set(false);

            if (serverFuture != null && !serverFuture.isDone() && !serverFuture.isCancelled()) {
                serverFuture.cancel(true);
                log.debug("Stopped server thread...");
            }

            executor.shutdownNow();
            log.debug("Stopped thread pool...");

            log.info("Stopped.");
        }
    }

    @Override public int getActualPort() {
        return actualPort.get();
    }

    private void startThreadPool() {
        // FIXME: I dont need all this for netty
        if (serverConfig.getWorkerThreads() > 0) {
            val actualWorkers = serverConfig.getWorkerThreads() + 1;
            executor = Executors.newFixedThreadPool(actualWorkers);
            log.info("Started thread pool with {} workers...", actualWorkers);
        } else {
            executor = Executors.newCachedThreadPool();
            log.info("Started thread pool with cached workers...");
        }
    }
}
