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
package io.github.cjstehno.ersatz.socket.fixtures;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cjstehno.ersatz.socket.client.TestMessage;
import io.github.cjstehno.ersatz.socket.encdec.Decoder;
import io.github.cjstehno.ersatz.socket.encdec.Encoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TextCodec {

    // FIXME: create a simple-test codec and json-codec for testing

    // FIXME: refactor this if kept
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static Decoder<TestMessage> DECODER = stream -> MAPPER.readValue(stream, TestMessage.class);

    public static Encoder ENCODER = (message, stream) -> {
        MAPPER.writeValue(stream, message);
        stream.flush();
    };


//    public static Decoder<TestMessage> DECODER = stream -> {
//        try (val reader = new BufferedReader(new InputStreamReader(stream, US_ASCII))) {
//            val line = reader.readLine();
//            log.info("Decoded-Line: [{}]", line);
//            val type = valueOf(line.substring(0, line.indexOf(":")));
//            val content = line.substring(line.indexOf(":") + 1);
//            return new TestMessage(type, content);
//        }
//    };

//    public static Decoder<TestMessage> DECODER = stream -> {
//        val buffer = new StringBuilder();
//
//        int x = stream.read();
//        while (x != -1) {
//            char ch = (char) x;
//            if (ch != '\n') {
//                buffer.append(ch);
//                x = stream.read();
//            } else {
//                x = stream.read();
//                break;
//            }
//        }
//
//        val line = buffer.toString();
//        log.info("Decoded-Line: {}", line);
//        val type = valueOf(line.substring(0, line.indexOf(":")));
//        val content = line.substring(line.indexOf(":") + 1);
//        return new TestMessage(type, content);
//    };


//    public static Encoder ENCODER = (message, stream) -> {
//        log.info("Encoding-Message: {}", message);
//
//        val msg = (TestMessage) message;
////        try (val writer = new BufferedWriter(new OutputStreamWriter(stream, US_ASCII))) {
////            writer.write(msg.getType().name() + ":" + msg.getContent() + "\n");
////            writer.flush();
////        }
//        val line = msg.getType().name() + ":" + msg.getContent() + "\n";
//        log.info("Encoded-Message (line): [{}]", line);
//        stream.write(line.getBytes(US_ASCII));
////        stream.flush();
//    };
}
