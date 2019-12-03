package com.zman.net.pull.netty;

import com.zman.pull.stream.IDuplex;
import com.zman.pull.stream.ISink;
import com.zman.pull.stream.ISource;
import com.zman.pull.stream.bean.ReadResult;
import com.zman.pull.stream.bean.ReadResultEnum;
import com.zman.pull.stream.impl.DefaultStreamBuffer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

import java.nio.charset.StandardCharsets;

public class NettyDuplex implements IDuplex {

    private Channel channel;

    private boolean end;

    private ISource source;

    private ISink sink;

    private DefaultStreamBuffer defaultStreamBuffer = new DefaultStreamBuffer();

    public NettyDuplex(Channel channel){
        this.channel = channel;

        defaultStreamBuffer.on("update", (data)->{
            if( sink != null ){
                ISink iSink = sink;
                sink = null;
                iSink.notifyAvailable();
            }
        });
    }

    /**
     * 从source读取数据
     * @param source readable stream
     */
    @Override
    public void read(ISource source) {
        this.source = source;

        ReadResult<ByteBuf> result;
        do {
            result = source.get(end, this);
            if (ReadResultEnum.Available.equals(result.status)) {
                ByteBuf tmp = result.data.copy();
                byte[] bytes = new byte[result.data.readableBytes()];
                result.data.readBytes(bytes);
                channel.writeAndFlush(tmp);
            }
        }while(result.status.equals(ReadResultEnum.Available));

    }

    /**
     * sink收到waiting之后，当Source再次有了数据后会调用sink的{@link #notifyAvailable}方法进行通知
     * 然后sink可以立刻读取数据
     */
    @Override
    public void notifyAvailable() {
        if( source != null ){
            read(source);
        }
    }

    /**
     * 返回一条数据
     *
     * @param end  控制source是否结束数据的生产
     * @param sink <code>ISink</code>的引用，当<code>ISource</code>没有数据可以提供时会保存sink的引用
     * @return 本次读取数据的结果：Available 获取到数据，Waiting 等待回调，End 结束
     */
    @Override
    public ReadResult get(boolean end, ISink sink) {
        this.sink = sink;

        Object data = defaultStreamBuffer.poll();
        if( data != null ){
            return new ReadResult(data);
        }else {
            channel.read();
            return ReadResult.Waiting;
        }
    }

    public void push(Object data){
        defaultStreamBuffer.offer(data);
    }
}
