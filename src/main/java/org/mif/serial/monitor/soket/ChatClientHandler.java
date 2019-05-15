package org.mif.serial.monitor.soket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

//自定义一个客户端业务处理类
public class ChatClientHandler extends SimpleChannelInboundHandler<String> {

    @Override
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, String s) throws Exception {

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println(msg.toString().trim());
        super.channelRead(ctx, msg);
    }
}