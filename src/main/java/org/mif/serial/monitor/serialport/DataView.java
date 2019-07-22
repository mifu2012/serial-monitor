package org.mif.serial.monitor.serialport;

import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import org.apache.commons.lang3.StringUtils;
import org.mif.serial.monitor.Constants;
import org.mif.serial.monitor.serialexception.ReadDataFromSerialPortFailure;
import org.mif.serial.monitor.serialexception.SerialPortInputStreamCloseFailure;
import org.mif.serial.monitor.soket.NettyClient;
import org.mif.serial.monitor.vo.EquipmentVO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

/**
 * 监测数据显示类
 *
 * @author Zhong
 */
public class DataView extends Frame {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private List<EquipmentVO> pclList; //PLC列表

    private List<String> commList;    //保存可用端口号
    public static SerialPort serialPort = null;    //保存串口对象


    private Choice plcChoice = new Choice();    //PLC编码（下拉框）

    private Font font = new Font("微软雅黑", Font.BOLD, 25);

    private TextField baudRate = new TextField("暂无数据", Label.CENTER);    //波特率
    private JComboBox equipmentLength = new JComboBox(new String[]{"0x0000","0x1000"});    //长度
    private JComboBox parityBit = new JComboBox(new String[]{"0x0000","0x0400","0x0500"});    //检验位
    private JComboBox stopBit = new JComboBox(new String[]{"0x0000","0x1000","0x2000","0x3000"});    //停止位
    private JComboBox linkedMethod = new JComboBox(new String[]{"0x00","0x01"});    //链接方式

    private Choice commChoice = new Choice();    //串口选择（下拉框）

    private Button openSerialButton = new Button("打开串口");
    private Button updateButton = new Button("修改");

    private static String plcChannelId;


    Image offScreen = null;    //重画时的画布

    //设置window的icon
    Toolkit toolKit = getToolkit();
    Image icon = toolKit.getImage("computer.png");

    /**
     * 类的构造方法
     */
    public DataView() {
        this.setBounds(200, 70, 800, 620);    //设定程序在桌面出现的位置
        this.setTitle("CDIO工程项目");    //设置程序标题
        this.setBackground(Color.white);    //设置背景色

        this.addWindowListener(new WindowAdapter() {
            //添加对窗口状态的监听
            public void windowClosing(WindowEvent arg0) {
                //当窗口关闭时
                System.exit(0);    //退出程序
            }

        });

        this.setResizable(false);    //窗口大小不可更改
        this.setVisible(true);    //显示窗口

        //程序初始化时就扫描一次有效串口
        commList = SerialTool.findPort();
        HttpClientUtils httpClientUtils = HttpClientUtils.getInstance();
        //程序初始化时就扫描一次有效串口
        pclList = httpClientUtils.getPlcList();
    }

    /**
     * 主菜单窗口显示；
     * 添加Label、按钮、下拉条及相关事件监听；
     */
    public void dataFrame() {
        this.setBounds(200, 70, 800, 620);
        this.setTitle("PC辅助软件");
        this.setIconImage(icon);
        this.setBackground(Color.white);
        this.setLayout(null);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent arg0) {
                if (serialPort != null) {
                    //程序退出时关闭串口释放资源
                    SerialTool.closePort(serialPort);
                }
                System.exit(0);
            }

        });

        //添加plc选择选项
        plcChoice.setBounds(160, 67, 200, 200);
        add(plcChoice);
        if (null != pclList) {

            for (int i = 0; i < pclList.size(); i++) {
                EquipmentVO equipmentVO = pclList.get(i);
                plcChoice.add(equipmentVO.getEquipmentNo());
                if (i == 0) {
                    baudRate.setText(equipmentVO.getBaudRate());
                    equipmentLength.setSelectedItem(equipmentVO.getEquipmentLength());
                    parityBit.setSelectedItem(equipmentVO.getParityBit());
                    stopBit.setSelectedItem(equipmentVO.getStopBit());
                    linkedMethod.setSelectedItem(equipmentVO.getLinkedMethod());
                    plcChannelId = equipmentVO.getChannelId();
                }
            }

        } else {
            plcChoice.add("--暂无PLC--");
        }
        add(plcChoice);

        //下拉事件监听
        plcChoice.addItemListener(e -> {
            String equipNO = (String) e.getItem();
            HttpClientUtils httpClientUtils = HttpClientUtils.getInstance();
            EquipmentVO vo = httpClientUtils.getPlcDetail(equipNO);
            if (null == vo) {
                JOptionPane.showMessageDialog(null, "没有获取到PLC数据！", "错误", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            baudRate.setText(vo.getBaudRate());
            equipmentLength.setSelectedItem(vo.getEquipmentLength());
            parityBit.setSelectedItem(vo.getParityBit());
            stopBit.setSelectedItem(vo.getStopBit());
            linkedMethod.setSelectedItem(vo.getLinkedMethod());
            plcChannelId = vo.getChannelId();
            plcChannelId = vo.getChannelId();
        });


        baudRate.setBounds(140, 123, 225, 40);
        baudRate.setFont(font);
        baudRate.setForeground(Color.black);
        add(baudRate);

        equipmentLength.setBounds(520, 123, 225, 40);
        equipmentLength.setFont(font);
        equipmentLength.setForeground(Color.black);
        add(equipmentLength);

        parityBit.setBounds(140, 213, 225, 40);

        parityBit.setFont(font);
        parityBit.setForeground(Color.black);
        add(parityBit);

        stopBit.setBounds(520, 213, 225, 40);
        stopBit.setFont(font);
        stopBit.setForeground(Color.black);
        add(stopBit);

        linkedMethod.setBounds(140, 303, 225, 40);
        linkedMethod.setFont(font);
        linkedMethod.setForeground(Color.black);
        add(linkedMethod);

        //添加串口选择选项
        commChoice.setBounds(160, 397, 200, 200);
        //检查是否有可用串口，有则加入选项中
        if (commList == null || commList.size() < 1) {
            JOptionPane.showMessageDialog(null, "没有搜索到有效串口！", "错误", JOptionPane.INFORMATION_MESSAGE);
        } else {
            for (String s : commList) {
                commChoice.add(s);
            }
        }
        add(commChoice);


        //修改按钮
        updateButton.setBounds(150, 490, 100, 50);
        updateButton.setBackground(Color.orange);
        updateButton.setFont(new Font("微软雅黑", Font.BOLD, 20));
        updateButton.setForeground(Color.darkGray);
        add(updateButton);

        //添加打开串口按钮
        openSerialButton.setBounds(420, 490, 100, 50);
        openSerialButton.setBackground(Color.orange);
        openSerialButton.setFont(new Font("微软雅黑", Font.BOLD, 20));
        openSerialButton.setForeground(Color.darkGray);
        add(openSerialButton);

        //添加打开串口按钮的事件监听
        openSerialButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                //获取串口名称
                String commName = commChoice.getSelectedItem();

                //检查串口名称是否获取正确
                if (commName == null || commName.equals("")) {
                    JOptionPane.showMessageDialog(null, "没有搜索到有效串口！", "错误", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    //检查波特率是否获取正确
//                    if (bpsStr == null || bpsStr.equals("")) {
//                        JOptionPane.showMessageDialog(null, "波特率获取错误！", "错误", JOptionPane.INFORMATION_MESSAGE);
//                    } else {
                    //串口名、波特率均获取正确时
//                    String bpsStr = baudRate.getText(); TODO
                    String bpsStr = null;
                    if (StringUtils.isEmpty(bpsStr) || bpsStr.equals("暂无数据")) {
                        JOptionPane.showMessageDialog(null, "请先完善PLC资料！", "错误", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    int bps = Integer.parseInt(bpsStr);
                    try {

                        //获取指定端口名及波特率的串口对象
                        serialPort = SerialTool.openPort(commName, bps);
                        //在该串口对象上添加监听器
                        SerialTool.addListener(serialPort, new SerialListener());
                        //监听成功进行提示
                        JOptionPane.showMessageDialog(null, "监听成功，稍后将显示监测数据！", "提示", JOptionPane.INFORMATION_MESSAGE);

                        // tcp 注册
                        Channel localhost = new NettyClient(Constants.REMOTE_ADDR, Constants.REMOTE_N_PORT).run();

                        //Http
                        localhost.writeAndFlush(Unpooled.copiedBuffer(("BIND_" + plcChannelId).getBytes()));


                    } catch (Exception e1) {
                        //发生错误时使用一个Dialog提示具体的错误信息
                        JOptionPane.showMessageDialog(null, e1, "错误", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
//                    }
                }

            }
        });


        this.setResizable(false);

    }

    /**
     * 画出主界面组件元素
     */
    @Override
    public void paint(Graphics g) {
        Color c = g.getColor();

        g.setColor(Color.orange);
        g.setFont(new Font("微软雅黑", Font.BOLD, 20));
        g.drawString(" PLC编号： ", 50, 80);

        g.setColor(Color.black);
        g.setFont(new Font("微软雅黑", Font.BOLD, 20));
        g.drawString(" 波特率： ", 50, 150);

        g.setColor(Color.black);
        g.setFont(new Font("微软雅黑", Font.BOLD, 20));
        g.drawString(" 长度： ", 425, 150);

        g.setColor(Color.black);
        g.setFont(new Font("微软雅黑", Font.BOLD, 20));
        g.drawString(" 检验位： ", 50, 240);

        g.setColor(Color.black);
        g.setFont(new Font("微软雅黑", Font.BOLD, 20));
        g.drawString(" 停止位： ", 425, 240);

        g.setColor(Color.black);
        g.setFont(new Font("微软雅黑", Font.BOLD, 20));
        g.drawString(" 链接方式： ", 50, 320);

        g.setColor(Color.orange);
        g.setFont(new Font("微软雅黑", Font.BOLD, 20));
        g.drawString(" 串口选择： ", 50, 410);

    }

    /**
     * 以内部类形式创建一个串口监听类
     *
     * @author zhong
     */
    private class SerialListener implements SerialPortEventListener {

        /**
         * 处理监控到的串口事件
         */
        @Override
        public void serialEvent(SerialPortEvent serialPortEvent) {

            switch (serialPortEvent.getEventType()) {

                case SerialPortEvent.BI: // 10 通讯中断
                    JOptionPane.showMessageDialog(null, "与串口设备通讯中断", "错误", JOptionPane.INFORMATION_MESSAGE);
                    break;

                case SerialPortEvent.OE: // 7 溢位（溢出）错误

                case SerialPortEvent.FE: // 9 帧错误

                case SerialPortEvent.PE: // 8 奇偶校验错误

                case SerialPortEvent.CD: // 6 载波检测

                case SerialPortEvent.CTS: // 3 清除待发送数据

                case SerialPortEvent.DSR: // 4 待发送数据准备好了

                case SerialPortEvent.RI: // 5 振铃指示

                case SerialPortEvent.OUTPUT_BUFFER_EMPTY: // 2 输出缓冲区已清空
                    break;

                case SerialPortEvent.DATA_AVAILABLE: // 1 串口存在可用数据

                    byte[] data = null;

                    try {
                        if (serialPort == null) {
                            JOptionPane.showMessageDialog(null, "串口对象为空！监听失败！", "错误", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            data = SerialTool.readFromPort(serialPort);    //读取数据，存入字节数组
                            System.out.println("read data=" + data.toString());
                            //自定义解析过程
                            Channel channel = NettyClient.channel;
                            if (null != channel) {
                                channel.writeAndFlush(Unpooled.copiedBuffer(data));
                            }
                        }

                    } catch (ReadDataFromSerialPortFailure e) {
                        JOptionPane.showMessageDialog(null, e, "错误", JOptionPane.INFORMATION_MESSAGE);
                        System.exit(0);    //发生读取错误时显示错误信息后退出系统
                    } catch (SerialPortInputStreamCloseFailure serialPortInputStreamCloseFailure) {
                        serialPortInputStreamCloseFailure.printStackTrace();
                    }

                    break;
            }

        }

    }

}
