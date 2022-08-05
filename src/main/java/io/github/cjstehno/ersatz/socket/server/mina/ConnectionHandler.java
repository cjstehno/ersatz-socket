package io.github.cjstehno.ersatz.socket.server.mina;

import io.github.cjstehno.ersatz.socket.impl.ServerConfigImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE) @Slf4j
class ConnectionHandler extends IoHandlerAdapter {

    private final ServerConfigImpl serverConfig;

    @Override public void sessionOpened(final IoSession session) throws Exception {
        serverConfig.getInteractions().getConnectInteraction().accept(session::write);
    }

    @Override public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        log.error("Exception-caught: {} -> {}", session, cause.getMessage(), cause);
    }

    @Override public void messageReceived(final IoSession session, final Object message) throws Exception {
        log.info("Message-received: {} -> {}", session, message);

        val interaction = serverConfig.getInteractions().findMessageInteraction(message).orElseThrow();
        interaction.accept(session::write, message);
    }

    @Override public void messageSent(IoSession session, Object message) throws Exception {
        super.messageSent(session, message);
        log.info("Message-sent: {} -> {}", session, message);
    }
}
