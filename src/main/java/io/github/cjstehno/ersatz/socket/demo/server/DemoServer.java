package io.github.cjstehno.ersatz.socket.demo.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static io.netty.channel.ChannelOption.SO_BACKLOG;
import static io.netty.channel.ChannelOption.SO_KEEPALIVE;

@RequiredArgsConstructor @Slf4j
public class DemoServer implements Runnable {

    private final int port;
    private ServerBootstrap bootstrap;

    public void run() {
        log.info("Running...");

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(
                            new RequestDecoder(),
                            new ResponseEncoder(),
                            new ProcessingHandler()
                        );
                    }
                })
                .option(SO_BACKLOG, 128)
                .childOption(SO_KEEPALIVE, true);

            ChannelFuture f = bootstrap.bind(port).sync();
            f.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            log.error("Error: {}", e.getMessage(), e);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public void stop() throws InterruptedException {
        log.info("Stopping...");
        if (bootstrap != null) {
            bootstrap.group().shutdownGracefully().sync();
            bootstrap.childGroup().shutdownGracefully().sync();
        }
        log.info("Stopped.");
    }
}
