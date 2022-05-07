package io.github.cjstehno.ersatz.socket.demo.server;

import io.github.cjstehno.ersatz.socket.demo.ResponseData;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class ResponseEncoder extends MessageToByteEncoder<ResponseData> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ResponseData msg, ByteBuf out) throws Exception {
        val value = msg.getIntValue();
        log.info("<server> Encoded-Response: {}", value);

        out.writeInt(value);
    }
}
