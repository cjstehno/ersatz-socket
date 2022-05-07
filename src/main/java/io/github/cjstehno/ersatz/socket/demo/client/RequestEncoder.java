package io.github.cjstehno.ersatz.socket.demo.client;

import io.github.cjstehno.ersatz.socket.demo.RequestData;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class RequestEncoder extends MessageToByteEncoder<RequestData> {

    @Override
    protected void encode(ChannelHandlerContext ctx, RequestData msg, ByteBuf out) throws Exception {
        log.info("<client> Encoding request: {}", msg);

        out.writeInt(msg.getIntValue());
        out.writeInt(msg.getStringValue().length());
        out.writeBytes(msg.getStringValue().getBytes(UTF_8));
    }
}
