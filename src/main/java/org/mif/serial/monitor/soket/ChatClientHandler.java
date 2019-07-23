package org.mif.serial.monitor.soket;

import gnu.io.SerialPort;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.mif.serial.monitor.serialport.DataView;

import java.io.OutputStream;

//自定义一个客户端业务处理类
public class ChatClientHandler extends ChannelHandlerAdapter {


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println(msg.toString().trim());
        //通过串口发送数据
        SerialPort serialPort = DataView.serialPort;
        String buf = (String) msg;

        OutputStream outputStream = serialPort.getOutputStream();
        outputStream.write(buf.getBytes());

        super.channelRead(ctx, msg);
    }
}