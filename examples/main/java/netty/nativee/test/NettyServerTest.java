package netty.nativee.test;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class NettyServerTest {

    @Sharable
    class ServerHandler extends ChannelInboundHandlerAdapter{

        private boolean schedulerIsStarted = false;

        private Channel channel;

        private int signal;

        /**
         * when the channel active, it will start a scheduler to write a number into socket per 1s.
         */
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {

            System.out.println("accept client");

            channel = ctx.channel();
            if(!schedulerIsStarted) {
                schedulerIsStarted = true;
                Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(
                        ()->{
                            if( channel != null ){
                                ByteBuf buf = Unpooled.buffer(4);
                                buf.writeInt(signal);
                                channel.writeAndFlush(buf).addListener((ChannelFutureListener) future -> {
                                    if(future.isSuccess()){
                                        System.out.println("write to socket successfully " + signal++ );
                                    }else{
                                        System.out.println(new Date()+" write to socket failed " + signal);
                                    }
                                });
                                System.out.println(new Date() + " write to socket: " + signal + " channel : " + channel);
                            }
                        }, 1, 1, TimeUnit.SECONDS
                );
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            channel = null;
            System.out.println("client discconect");
        }
    }

    private ChannelInboundHandlerAdapter serverHandler = new ServerHandler();

    public NettyServerTest() {
        new Thread(() -> {
            EventLoopGroup group = new NioEventLoopGroup();

            try{
                ServerBootstrap b = new ServerBootstrap();
                b.group(group)
                        .channel(NioServerSocketChannel.class)
                        .localAddress(new InetSocketAddress(8081))
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) {
                                socketChannel.pipeline().addLast(serverHandler);
                            }
                        });

                ChannelFuture f = b.bind().sync();
                f.channel().closeFuture().sync();

            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                try {
                    group.shutdownGracefully().sync();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }){}.start();

    }

    @Test
    public void startServer() throws IOException {
        System.in.read();
    }


}
