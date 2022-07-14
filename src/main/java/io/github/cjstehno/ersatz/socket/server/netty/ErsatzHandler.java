package io.github.cjstehno.ersatz.socket.server.netty;

import io.github.cjstehno.ersatz.socket.cfg.ConnectionContext;
import io.github.cjstehno.ersatz.socket.impl.ServerConfigImpl;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@RequiredArgsConstructor @Slf4j
public class ErsatzHandler extends ChannelInboundHandlerAdapter {

    private final ServerConfigImpl serverConfig;

    @Override public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        // FIXME: handle connection interaction ??
        log.info("Channel-Active - Connected?");

        val connectionCtx = new ConnectionContext() {
            @Override public void send(final Object message) {
                ctx.writeAndFlush(message).addListener(ChannelFutureListener.CLOSE);
            }
        };

        serverConfig.getInteractions().getConnectInteraction().accept(connectionCtx);
    }

    @Override public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        log.info("Channel-Read...");

        val connectionCtx = new ConnectionContext() {
            @Override public void send(final Object message) {
                ctx.writeAndFlush(message).addListener(ChannelFutureListener.CLOSE);
            }
        };

        val interaction = serverConfig.getInteractions().findMessageInteraction(msg).orElseThrow();
        interaction.accept(connectionCtx, msg);
    }

    @Override public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        log.info("User-Event: {}", evt);
    }


}
