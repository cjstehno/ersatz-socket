/**
 * Copyright (C) 2022 Christopher J. Stehno
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.cjstehno.ersatz.socket.fixtures;

import io.github.cjstehno.ersatz.socket.client.TestMessage;
import io.github.cjstehno.ersatz.socket.encdec.Decoder;
import io.github.cjstehno.ersatz.socket.encdec.Encoder;
import lombok.val;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class BinaryCodec {

    public static Decoder<TestMessage> DECODER = stream -> {
        try (val input = new DataInputStream(stream)) {
            return new TestMessage(
                TestMessage.MessageType.values()[input.readInt()],
                input.readUTF()
            );
        }
    };

    public static Encoder ENCODER = (message, stream) -> {
        try (val output = new DataOutputStream(stream)) {
            val msg = (TestMessage) message;
            output.writeInt(msg.getType().ordinal());
            output.writeUTF(msg.getContent());
        }
    };
}
