package com.zman.net.pull.netty.codec;

import com.zman.pull.stream.ISink;
import com.zman.pull.stream.ISource;
import com.zman.pull.stream.impl.DefaultSink;
import com.zman.pull.stream.impl.DefaultSource;
import io.netty.buffer.ByteBuf;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.ws.Holder;
import java.nio.ByteBuffer;

import static com.zman.pull.stream.util.Pull.pull;

public class NettyEncoderTest {

    @Test
    public void encode(){

        ISource<byte[]> source = new DefaultSource<>();

        Holder<ByteBuf> resultHolder = new Holder<>();
        ISink<ByteBuf> sink = new DefaultSink<>(data->{resultHolder.value=data;return false;},()->{}, t->{});

        pull(source, new NettyEncoder(), sink);

        // source push int
        ByteBuffer sourceBuffer = ByteBuffer.allocate(4);
        sourceBuffer.putInt(Integer.MAX_VALUE).flip();
        source.push(sourceBuffer.array());

        // verify
        ByteBuf resultBuffer = resultHolder.value;
        Assert.assertEquals(4, resultBuffer.readInt());
        Assert.assertEquals(Integer.MAX_VALUE, resultBuffer.readInt());
    }

}
