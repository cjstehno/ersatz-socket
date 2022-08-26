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
