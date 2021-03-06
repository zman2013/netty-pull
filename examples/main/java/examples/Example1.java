package examples;

import com.zman.net.pull.netty.NettyClient;
import com.zman.net.pull.netty.NettyServer;
import com.zman.pull.stream.impl.DefaultSink;
import com.zman.pull.stream.impl.DefaultSource;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.io.IOException;

import static com.zman.pull.stream.util.Pull.pull;

public class Example1 {

    public void server() throws IOException {
        new NettyServer()
                .onAccept((connectId,duplex ) -> pull(duplex.source(), duplex.sink()))
                .listen(8081);

        System.in.read();
    }

    public void consoleClient() {
        DefaultSink<ByteBuf> sink = new DefaultSink<>(System.out::println, Throwable::printStackTrace);

        new NettyClient()
                .onConnected(duplex -> pull( duplex.source(), sink))
                .connect("localhost", 8081);
    }

    @Test
    public void clientAndServer() throws InterruptedException, IOException {
        int port = 8081;
        new NettyServer()
                .onAccept((connectionId,duplex )-> pull(duplex, duplex))
                .listen(port);

        DefaultSource<ByteBuf> source = new DefaultSource<>();
        DefaultSink<ByteBuf> sink = new DefaultSink<>(buf -> {
            int i = buf.readInt();
            System.out.println(i);
        }, Throwable::printStackTrace);

        new NettyClient()
                .onConnected(duplex -> pull(source, duplex, sink))
                .connect("localhost", port);

        for( int i = 0; i < 100; i ++ ) {
            ByteBuf buf = Unpooled.buffer(4);
            buf.writeInt(i);
            source.push(buf);
            Thread.sleep(1000);
        }

        System.in.read();
    }

}
