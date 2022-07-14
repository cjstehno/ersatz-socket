package io.github.cjstehno.ersatz.socket.server.netty;

import io.github.cjstehno.ersatz.socket.impl.ServerConfigImpl;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.List;

@RequiredArgsConstructor @Slf4j
public class NettyErsatzDecoder extends ReplayingDecoder<Object> {

    private final ServerConfigImpl serverConfig;

    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) throws Exception {
        val decoder = serverConfig.decoder().orElseThrow();

        // FIXME: does this need to be closed?
        try (val input = new ByteBufInputStream(in)) {
            val decoded = decoder.decode(input);
            log.info("Decoded message: {}", decoded);
            out.add(decoded);
        }
    }
}
