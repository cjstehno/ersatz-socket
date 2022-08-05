package io.github.cjstehno.ersatz.socket.server.mina;

import io.github.cjstehno.ersatz.socket.impl.ServerConfigImpl;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderAdapter;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
class ErsatzProtocolDecoder extends ProtocolDecoderAdapter {

    private final ServerConfigImpl serverConfig;

    @Override
    public void decode(final IoSession session, final IoBuffer in, final ProtocolDecoderOutput out) throws Exception {
        val messge = serverConfig.decoder().orElseThrow().decode(in.asInputStream());
        out.write(messge);
    }
}
