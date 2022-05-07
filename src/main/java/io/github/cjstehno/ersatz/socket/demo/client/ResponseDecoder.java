package io.github.cjstehno.ersatz.socket.demo.client;

import io.github.cjstehno.ersatz.socket.demo.ResponseData;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ResponseDecoder extends ReplayingDecoder<ResponseData> {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        ResponseData data = new ResponseData();
        data.setIntValue(in.readInt());
        log.info("<client> Decoding response: {}", data);

        out.add(data);
    }
}
