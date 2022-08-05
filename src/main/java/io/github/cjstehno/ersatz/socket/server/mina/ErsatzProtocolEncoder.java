package io.github.cjstehno.ersatz.socket.server.mina;

import io.github.cjstehno.ersatz.socket.impl.ServerConfigImpl;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import java.io.ByteArrayOutputStream;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
class ErsatzProtocolEncoder extends ProtocolEncoderAdapter {

    private final ServerConfigImpl serverConfig;

    @Override
    public void encode(final IoSession session, final Object message, final ProtocolEncoderOutput out) throws Exception {
        try (val outstream = new ByteArrayOutputStream()) {
            serverConfig.encoder().orElseThrow().encode(message, outstream);
            val buffer = IoBuffer.wrap(outstream.toByteArray());
            out.write(buffer);
        }
    }
}
