package com.zman.net.pull.netty.codec;

import com.zman.net.pull.netty.NettyDuplex;
import com.zman.pull.stream.ISink;
import com.zman.pull.stream.ISource;
import com.zman.pull.stream.impl.DefaultSink;
import com.zman.pull.stream.impl.DefaultSource;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.ByteBuffer;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.zman.pull.stream.util.Pull.pull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class NettyDecoderTest {

    @Mock
    private Function<byte[],Boolean> onData;

    @Before
    public void before(){
        when(onData.apply(any())).thenReturn(false);
    }

    @Test
    public void decode(){

        ISource<ByteBuf> source = new DefaultSource<>();

        ISink<byte[]> sink = new DefaultSink<>(onData,()->{}, t->{});

        pull(source, new NettyDecoder(), sink);

        // prepare data
        ByteBuf byteBuf = Unpooled.buffer(8);
        byteBuf.writeInt(4);
        byteBuf.writeInt(Integer.MAX_VALUE);

        // source push data
        source.push(byteBuf);

        // verify
        byte[] expected = new byte[4];
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.putInt(Integer.MAX_VALUE).flip();
        buf.get(expected);

        verify(onData, times(1)).apply(expected);

    }

    @Test
    public void decodeTwoPhase(){

        ISource<ByteBuf> source = new DefaultSource<>();

        ISink<byte[]> sink = new DefaultSink<>(onData,()->{},t->{});

        pull(source, new NettyDecoder(), sink);

        // source push data length
        ByteBuf byteBuf = Unpooled.buffer(4);
        byteBuf.writeInt(4);
        source.push(byteBuf);

        // push data
        byteBuf = Unpooled.buffer(4);
        byteBuf.writeInt(Integer.MAX_VALUE);
        source.push(byteBuf);

        // verify
        byte[] expected = new byte[4];
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.putInt(Integer.MAX_VALUE).flip();
        buf.get(expected);

        verify(onData, times(1)).apply(expected);

    }



}
