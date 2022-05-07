package io.github.cjstehno.ersatz.socket.demo.server;

import io.github.cjstehno.ersatz.socket.demo.RequestData;
import io.github.cjstehno.ersatz.socket.demo.ResponseData;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import static io.netty.channel.ChannelFutureListener.CLOSE;

@Slf4j
public class ProcessingHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        RequestData requestData = (RequestData) msg;
        log.info("<server> Processing Request: {}", requestData);

        ResponseData responseData = new ResponseData();
        responseData.setIntValue(requestData.getIntValue() * 10);

        ChannelFuture future = ctx.writeAndFlush(responseData);
        future.addListener(CLOSE);
    }
}
