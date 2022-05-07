package io.github.cjstehno.ersatz.socket.demo.client;

import io.github.cjstehno.ersatz.socket.demo.RequestData;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        RequestData msg = new RequestData();
        msg.setIntValue(123);
        msg.setStringValue("all work and no play makes jack a dull boy");

        log.info("<client> Sending: {}", msg);

        ChannelFuture future = ctx.writeAndFlush(msg);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("<client> Response: {}", msg);
        ctx.close();
    }
}
