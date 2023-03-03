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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import static lombok.AccessLevel.PRIVATE;

@EqualsAndHashCode @ToString @Getter @SuppressWarnings("ClassCanBeRecord")
public class TestMessage {

    private final MessageType type;
    private final String content;

    @JsonCreator
    public TestMessage(
        @JsonProperty("type") final MessageType type,
        @JsonProperty("content") final String content
    ) {
        this.type = type;
        this.content = content;
    }

    public static TestMessage createReply(final String content) {
        return new TestMessage(MessageType.REPLY, content);
    }

    public static TestMessage createSend(final String content) {
        return new TestMessage(MessageType.SEND, content);
    }

    public static TestMessage createMessage(final String content) {
        return new TestMessage(MessageType.MESSAGE, content);
    }

    public enum MessageType {
        SEND, MESSAGE, REPLY
    }

    @RequiredArgsConstructor(access = PRIVATE)
    public static class TypeMatcher extends BaseMatcher<TestMessage> {

        private final MessageType type;

        public static TypeMatcher ofType(final MessageType mt) {
            return new TypeMatcher(mt);
        }

        @Override public boolean matches(final Object actual) {
            return ((TestMessage) actual).getType() == type;
        }

        @Override public void describeTo(final Description description) {
            // TODO; something
        }
    }
}
