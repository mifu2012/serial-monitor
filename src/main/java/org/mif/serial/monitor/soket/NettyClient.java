package org.mif.serial.monitor.soket;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @description: netty client
 * @author: mif
 * @date: 2019/5/15 23:23
 */
public class NettyClient {

    public static Channel channel;

    private final String host; //服务器端IP地址
    private final int port;  //服务器端端口号

    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
    }


    public Channel run() {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            //往pipeline链中添加一个解码器
                            pipeline.addLast("decoder", new StringDecoder());
                            //往pipeline链中添加一个编码器
                            pipeline.addLast("encoder", new StringEncoder());
                            //往pipeline链中添加自定义的handler(业务处理类)
                            pipeline.addLast(new ChatClientHandler());
                        }
                    });

            ChannelFuture cf = bootstrap.connect(host, port).sync();
            channel = cf.channel();
            System.out.println("------" + channel.localAddress().toString().substring(1) + "------");
//                channel.writeAndFlush(msg + "\r\n");
            return channel;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
//            group.shutdownGracefully();
        }
        return null;
    }
}
