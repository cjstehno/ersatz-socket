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
package io.github.cjstehno.ersatz.socket.client;

import io.github.cjstehno.ersatz.socket.cfg.SslConfig;
import io.github.cjstehno.ersatz.socket.impl.SslConfigImpl;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.session.IoSession;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

/**
 * FIXME: document
 * a simple/configurable client for testing various scenarios...
 *
 */
@RequiredArgsConstructor @Slf4j
public class TestingClientOld {

    @Getter private final Set<String> responses = new CopyOnWriteArraySet<>();

    private List<Consumer<TestMessage>> onMessageListeners = new LinkedList<>();

    public void onConnection(final Runnable op) {
        onConnectListeners.add(op);
    }

    public void onMessage(final Consumer<TestMessage> consumer) {
        onMessageListeners.add(consumer);
    }

    public void send(final TestMessage message) {
        session.write(message.toMessage());
    }

    private void onConnection() {
        onConnectListeners.forEach(Runnable::run);
    }

    private void onMessage(final TestMessage message) {
        onMessageListeners.forEach(li -> li.accept(message));
    }

    @Value
    public static class TestMessage {
        String prefix;
        String value;

        public static TestMessage from(final String string) {
            val parts = string.split(":");
            return new TestMessage(parts[0].trim(), parts[1].trim());
        }

        String toMessage() {
            return prefix + ": " + value + "\n";
        }
    }


}
