/**
 * Copyright (C) 2022 Christopher J. Stehno
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.cjstehno.ersatz.socket.server.mina;

import io.github.cjstehno.ersatz.socket.impl.ServerConfigImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteToClosedSessionException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE) @Slf4j
class ConnectionHandler extends IoHandlerAdapter {

    private final ExecutorService executor = Executors.newCachedThreadPool(); // FIXME: refactor if kept
    private final ServerConfigImpl serverConfig;

    @Override public void sessionOpened(final IoSession session) throws Exception {
        serverConfig.getInteractions().getConnectInteraction().accept(session::write);
    }

    @Override public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        if (!(cause instanceof WriteToClosedSessionException)) {
            // TODO: is there a way to remove this?
            log.error("Exception-caught: {} -> {}", session, cause.getMessage(), cause);
        }
    }

    @Override public void messageReceived(final IoSession session, final Object message) throws Exception {
        log.info("Message-received: {} -> {}", session, message);

        // FIXME: on another thread?
        executor.submit(() -> {
            serverConfig.getInteractions().findMessageInteraction(message)
                .orElseThrow()
                .accept(session::write, message);
        });
    }

    @Override public void messageSent(IoSession session, Object message) throws Exception {
        log.info("Message-sent: {} -> {}", session, message);
    }
}
