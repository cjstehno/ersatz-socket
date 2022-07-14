package io.github.cjstehno.ersatz.socket.server.netty;

import io.github.cjstehno.ersatz.socket.impl.ServerConfigImpl;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@RequiredArgsConstructor @Slf4j
public class NettyErsatzEncoder extends MessageToByteEncoder<Object> {

    private final ServerConfigImpl serverConfig;

    @Override
    protected void encode(final ChannelHandlerContext ctx, final Object msg, final ByteBuf out) throws Exception {
        val encoder = serverConfig.encoder().orElseThrow();

        log.info("Encoding message: {}", msg);

        // FIXME: does this need to be closed?
        try (val output = new ByteBufOutputStream(out)) {
            encoder.encode(msg, output);
        }
    }
}
