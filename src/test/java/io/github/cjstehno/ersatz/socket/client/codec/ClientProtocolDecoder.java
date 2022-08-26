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
