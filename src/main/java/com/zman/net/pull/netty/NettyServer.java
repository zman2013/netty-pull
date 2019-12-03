package com.zman.net.pull.netty;

import com.zman.net.pull.AbstractServer;
import com.zman.pull.stream.IDuplex;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class NettyServer extends AbstractServer {


    private ServerHandler serverHandler = new ServerHandler();

    /**
     * 启动服务器，监听端口
     *
     * @param port 监听的端口
     */
    @Override
    public void listen(int port) {

        new ServerBootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.AUTO_READ, false)
                .localAddress(new InetSocketAddress(port))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel socketChannel) {
                        socketChannel.pipeline().addLast(serverHandler);
                    }
                })
                .bind();
    }

    @Sharable
    class ServerHandler extends ChannelInboundHandlerAdapter {
        private Map<ChannelId, NettyDuplex> duplexMap = new HashMap<>();
        public void channelActive(ChannelHandlerContext ctx) {
            Channel channel = ctx.channel();
            NettyDuplex duplex = new NettyDuplex(channel);
            duplexMap.put(channel.id(), duplex);
            onAcceptCallback.accept(channel.id().asShortText(), duplex);
        }

        public void channelInactive(ChannelHandlerContext ctx) {
            Channel channel = ctx.channel();
            IDuplex duplex = duplexMap.remove(channel.id());
            onDisconnectedCallback.accept(channel.id().asShortText(), duplex);
        }

        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            ChannelId channelId = ctx.channel().id();
            duplexMap.get(channelId).push(msg);
        }
    }
}
