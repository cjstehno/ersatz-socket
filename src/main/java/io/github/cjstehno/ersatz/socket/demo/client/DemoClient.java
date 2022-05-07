package io.github.cjstehno.ersatz.socket.demo.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static io.netty.channel.ChannelOption.SO_KEEPALIVE;

@RequiredArgsConstructor @Slf4j
public class DemoClient implements Runnable {

    private final String host;
    private final int port;
    private Bootstrap bootstrap;

    public void run() {
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            bootstrap = new Bootstrap();
            bootstrap.group(workerGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(SO_KEEPALIVE, true);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(
                        new RequestEncoder(),
                        new ResponseDecoder(),
                        new ClientHandler()
                    );
                }
            });

            ChannelFuture f = bootstrap.connect(host, port).sync();
            f.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            log.error("Error: {}", e.getMessage(), e);
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    public void stop() throws InterruptedException {
        bootstrap.group().shutdownGracefully().sync();
    }
}
