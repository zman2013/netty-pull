package com.zman.net.pull.netty;

import com.zman.pull.stream.impl.DefaultDuplex;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

import java.util.concurrent.ExecutionException;

public class NettyDuplex extends DefaultDuplex<ByteBuf> {


    public NettyDuplex(Channel channel) {

        sink().onNext(byteBuf -> {
            try {
                channel.writeAndFlush(byteBuf).get();
            } catch (InterruptedException | ExecutionException e) {
                channel.close();
                sink().close(e);
                source().close(e);
                return true;        // stop
            }

            return false;   // go on
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
