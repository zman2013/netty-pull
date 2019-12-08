package com.zman.net.pull.netty;

import com.zman.pull.stream.impl.DefaultDuplex;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

public class NettyDuplex extends DefaultDuplex<ByteBuf> {


    public NettyDuplex(Channel channel) {

        sink().onNext(byteBuf -> {
            channel.writeAndFlush(byteBuf); // todo this function is async, so there exists buffer exceeding case.
            return false;
        }).onClosed(throwable -> {
            channel.close();
            sink().close(throwable);
            source().close(throwable);
        });

        source().onBufferEmpty(channel::read)
                .onClosed(throwable -> {
                    channel.close();
                    sink().close(throwable);
                    source().close(throwable);
                });
    }


}
