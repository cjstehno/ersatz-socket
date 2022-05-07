package io.github.cjstehno.ersatz.socket.demo.server;

import io.github.cjstehno.ersatz.socket.demo.RequestData;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class RequestDecoder extends ReplayingDecoder<RequestData> {

    @Override protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        RequestData data = new RequestData();

        data.setIntValue(in.readInt());

        int strLen = in.readInt();
        data.setStringValue(in.readBytes(strLen).toString(UTF_8));

        log.info("<server> Decoded-Request: {}", data);

        out.add(data);
    }
}
