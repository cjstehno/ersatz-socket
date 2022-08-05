package io.github.cjstehno.ersatz.socket;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

import static java.nio.charset.StandardCharsets.US_ASCII;

@RequiredArgsConstructor @Slf4j
public class BravoClient {
    // https://mina.apache.org/mina-project/userguide/ch2-basics/ch2.3-sample-tcp-client.html

    @Getter private final Set<String> responses = new CopyOnWriteArraySet<>();
    private List<Runnable> onConnectListeners = new LinkedList<>();
    private List<Consumer<BravoMessage>> onMessageListeners = new LinkedList<>();
    private final int port;
    private IoConnector connector;
    private IoSession session;

    public void connect() throws Exception {
        connector = new NioSocketConnector();

        val filterChain = connector.getFilterChain();
        // FIXME: add ssl filter
        filterChain.addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(US_ASCII)));
//        filterChain.addLast("logger", new LoggingFilter(BravoClient.class));

        connector.setHandler(new IoHandlerAdapter() {
            @Override public void sessionOpened(IoSession session) throws Exception {
                log.info("Client-Connected on port {}...", port);
                onConnection();
            }

            @Override public void messageReceived(final IoSession session, final Object message) throws Exception {
                log.info("Client-Received: {}", message);
                onMessage(BravoMessage.from((String) message));
            }
        });

        // FIXME: timeout after N tries
        for (; ; ) {
            try {
                ConnectFuture future = connector.connect(new InetSocketAddress("localhost", port));
                future.awaitUninterruptibly();
                session = future.getSession();
                break;
            } catch (RuntimeException e) {
                log.error("Failed to connect.");
                e.printStackTrace();
                Thread.sleep(5000);
            }
        }
    }

    public void onConnection(final Runnable op){
        onConnectListeners.add(op);
    }

    public void onMessage(final Consumer<BravoMessage> consumer){
        onMessageListeners.add(consumer);
    }

    public void send(final BravoMessage message) {
        session.write(message.toMessage());
    }

    public void disconnect() {
        log.info("Disconnecting...");
        session.getCloseFuture().addListener(new IoFutureListener<IoFuture>() {
            @Override public void operationComplete(IoFuture future) {
                log.info("Disconnected.");
            }
        });

        session.closeNow();
        session.getCloseFuture().awaitUninterruptibly();
        connector.dispose();
        log.info("Closed.");
    }

    private void onConnection() {
        onConnectListeners.forEach(Runnable::run);
    }

    private void onMessage(final BravoMessage message) {
        onMessageListeners.forEach(li -> li.accept(message));
    }

    @Value
    public static class BravoMessage {
        String prefix;
        String value;

        public static BravoMessage from(final String string) {
            val parts = string.split(":");
            return new BravoMessage(parts[0].trim(), parts[1].trim());
        }

        String toMessage(){
            return prefix + ": " + value + "\n";
        }
    }
}
