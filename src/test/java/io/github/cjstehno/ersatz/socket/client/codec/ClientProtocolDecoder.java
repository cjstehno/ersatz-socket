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
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderAdapter;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

@RequiredArgsConstructor
public class ClientProtocolDecoder extends ProtocolDecoderAdapter {

    // TODO: consider refactoring to allow using same in server and client (maybe just a common codec provider interface)

    private final ClientConfigImpl clientConfig;

    @Override
    public void decode(final IoSession session, final IoBuffer in, final ProtocolDecoderOutput out) throws Exception {
        out.write(clientConfig.decoder().decode(in.asInputStream()));
    }
}
