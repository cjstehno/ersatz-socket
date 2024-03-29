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
package io.github.cjstehno.ersatz.socket.client.codec;

import io.github.cjstehno.ersatz.socket.client.cfg.ClientConfigImpl;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import java.io.ByteArrayOutputStream;

@RequiredArgsConstructor
public class ClientProtocolEncoder extends ProtocolEncoderAdapter {

    private final ClientConfigImpl clientConfig;

    @Override
    public void encode(final IoSession session, final Object message, final ProtocolEncoderOutput out) throws Exception {
        try (val outstream = new ByteArrayOutputStream()) {
            clientConfig.encoder().encode(message, outstream);
            out.write(IoBuffer.wrap(outstream.toByteArray()));
        }
    }
}
