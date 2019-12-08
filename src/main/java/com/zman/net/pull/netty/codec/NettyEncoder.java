package com.zman.net.pull.netty.codec;

import com.zman.pull.stream.ISink;
import com.zman.pull.stream.bean.ReadResult;
import com.zman.pull.stream.bean.ReadResultEnum;
import com.zman.pull.stream.impl.DefaultThrough;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class NettyEncoder extends DefaultThrough<byte[], ByteBuf> {

    @Override
    public ReadResult get(boolean end, Throwable throwable, ISink sink) {
        ReadResult readResult = super.get(end, throwable, sink);

        if(ReadResultEnum.Available.equals(readResult.status)){
            byte[] bytes = ((byte[])readResult.data);
            ByteBuf byteBuf = Unpooled.buffer();
            byteBuf.writeInt(bytes.length);
            byteBuf.writeBytes(bytes);
            readResult.data = byteBuf;
        }

        return readResult;
    }
}
