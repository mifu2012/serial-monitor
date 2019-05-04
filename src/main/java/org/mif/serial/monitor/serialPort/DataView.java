package org.mif.serial.monitor.serialPort;

import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.mif.serial.monitor.serialException.ExceptionWriter;
import org.mif.serial.monitor.serialException.ReadDataFromSerialPortFailure;
import org.mif.serial.monitor.serialException.SerialPortInputStreamCloseFailure;
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
@Slf4j
public class DataView extends Frame {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    Client client = null;

    private List<EquipmentVO> pclList; //PLC列表

    private List<String> commList;    //保存可用端口号
    private SerialPort serialPort = null;    //保存串口对象


    private Choice plcChoice = new Choice();    //PLC编码（下拉框）

    private Font font = new Font("微软雅黑", Font.BOLD, 25);

    private Label samplingPeriod = new Label("暂无数据", Label.CENTER);    //采样周期
    private Label baudRate = new Label("暂无数据", Label.CENTER);    //波特率
    private Label equipmentLength = new Label("暂无数据", Label.CENTER);    //长度
    private Label parityBit = new Label("暂无数据", Label.CENTER);    //检验位
    private Label stopBit = new Label("暂无数据", Label.CENTER);    //停止位
    private Label linkedMethod = new Label("暂无数据", Label.CENTER);    //链接方式

    private Choice commChoice = new Choice();    //串口选择（下拉框）
    private Choice bpsChoice = new Choice();    //波特率选择

    private Button openSerialButton = new Button("打开串口");

    Image offScreen = null;    //重画时的画布

    //设置window的icon
    Toolkit toolKit = getToolkit();
    Image icon = toolKit.getImage("computer.png");

    /**
     * 类的构造方法
     *
     * @param client
     */
    public DataView(Client client) {
        this.client = client;
        commList = SerialTool.findPort();    //程序初始化时就扫描一次有效串口
        HttpClientUtils httpClientUtils = HttpClientUtils.getInstance();
        pclList = httpClientUtils.getPlcList();    //程序初始化时就扫描一次有效串口
    }

    /**
     * 主菜单窗口显示；
     * 添加Label、按钮、下拉条及相关事件监听；
     */
    public void dataFrame() {
        this.setBounds(client.LOC_X, client.LOC_Y, client.WIDTH, client.HEIGHT);
        this.setTitle("PC辅助软件");
        this.setIconImage(icon);
        this.setBackground(Color.white);
        this.setLayout(null);

        this.addWindowListener(new WindowAdapter() {
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
                    samplingPeriod.setText(equipmentVO.getSamplingPeriod());
                    baudRate.setText(equipmentVO.getBaudRate());
                    equipmentLength.setText(equipmentVO.getEquipmentLength());
                    parityBit.setText(equipmentVO.getParityBit());
                    stopBit.setText(equipmentVO.getStopBit());
                    linkedMethod.setText(equipmentVO.getLinkedMethod());
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
            samplingPeriod.setText(vo.getSamplingPeriod());
            baudRate.setText(vo.getBaudRate());
            equipmentLength.setText(vo.getEquipmentLength());
            parityBit.setText(vo.getParityBit());
            stopBit.setText(vo.getStopBit());
            linkedMethod.setText(vo.getLinkedMethod());
        });


        samplingPeriod.setBounds(140, 103, 225, 50);
        samplingPeriod.setFont(font);
        samplingPeriod.setForeground(Color.black);
        add(samplingPeriod);

        baudRate.setBounds(520, 103, 225, 50);
        baudRate.setFont(font);
        baudRate.setForeground(Color.black);
        add(baudRate);

        equipmentLength.setBounds(140, 193, 225, 50);
        equipmentLength.setFont(font);
        equipmentLength.setForeground(Color.black);
        add(equipmentLength);

        parityBit.setBounds(520, 193, 225, 50);
        parityBit.setFont(font);
        parityBit.setForeground(Color.black);
        add(parityBit);

        stopBit.setBounds(140, 283, 225, 50);
        stopBit.setFont(font);
        stopBit.setForeground(Color.black);
        add(stopBit);

        linkedMethod.setBounds(520, 283, 225, 50);
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

        //添加打开串口按钮
        openSerialButton.setBounds(250, 490, 300, 50);
        openSerialButton.setBackground(Color.lightGray);
        openSerialButton.setFont(new Font("微软雅黑", Font.BOLD, 20));
        openSerialButton.setForeground(Color.darkGray);
        add(openSerialButton);
        //添加打开串口按钮的事件监听
        openSerialButton.addActionListener(new ActionListener() {

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
                    String bpsStr = baudRate.getText();
                    if (StringUtils.isEmpty(bpsStr)) {
                        JOptionPane.showMessageDialog(null, "请先完善PLC资料！", "错误", JOptionPane.INFORMATION_MESSAGE);
                    }
                    int bps = Integer.parseInt(bpsStr);
                    try {

                        //获取指定端口名及波特率的串口对象
                        serialPort = SerialTool.openPort(commName, bps);
                        //在该串口对象上添加监听器
                        SerialTool.addListener(serialPort, new SerialListener());
                        //监听成功进行提示
                        JOptionPane.showMessageDialog(null, "监听成功，稍后将显示监测数据！", "提示", JOptionPane.INFORMATION_MESSAGE);

                    } catch (Exception e1) {
                        //发生错误时使用一个Dialog提示具体的错误信息
                        JOptionPane.showMessageDialog(null, e1, "错误", JOptionPane.INFORMATION_MESSAGE);
                    }
//                    }
                }

            }
        });


        this.setResizable(false);

        new Thread(new RepaintThread()).start();    //启动重画线程

    }

    /**
     * 画出主界面组件元素
     */
    public void paint(Graphics g) {
        Color c = g.getColor();

        g.setColor(Color.orange);
        g.setFont(new Font("微软雅黑", Font.BOLD, 20));
        g.drawString(" PLC编号： ", 50, 80);

        g.setColor(Color.black);
        g.setFont(new Font("微软雅黑", Font.BOLD, 25));
        g.drawString(" 采样周期： ", 50, 130);

        g.setColor(Color.black);
        g.setFont(new Font("微软雅黑", Font.BOLD, 25));
        g.drawString(" 波特率： ", 425, 130);

        g.setColor(Color.black);
        g.setFont(new Font("微软雅黑", Font.BOLD, 25));
        g.drawString(" 长度： ", 50, 220);

        g.setColor(Color.black);
        g.setFont(new Font("微软雅黑", Font.BOLD, 25));
        g.drawString(" 检验位： ", 425, 220);

        g.setColor(Color.black);
        g.setFont(new Font("微软雅黑", Font.BOLD, 25));
        g.drawString(" 停止位： ", 50, 310);

        g.setColor(Color.black);
        g.setFont(new Font("微软雅黑", Font.BOLD, 25));
        g.drawString(" 链接方式： ", 425, 310);

        g.setColor(Color.gray);
        g.setFont(new Font("微软雅黑", Font.BOLD, 20));
        g.drawString(" 串口选择： ", 50, 410);

    }

    /**
     * 双缓冲方式重画界面各元素组件
     */
    public void update(Graphics g) {
        if (offScreen == null) offScreen = this.createImage(Client.WIDTH, Client.HEIGHT);
        Graphics gOffScreen = offScreen.getGraphics();
        Color c = gOffScreen.getColor();
        gOffScreen.setColor(Color.white);
        gOffScreen.fillRect(0, 0, Client.WIDTH, Client.HEIGHT);    //重画背景画布
        this.paint(gOffScreen);    //重画界面元素
        gOffScreen.setColor(c);
        g.drawImage(offScreen, 0, 0, null);    //将新画好的画布“贴”在原画布上
    }

    /*
     * 重画线程（每隔30毫秒重画一次）
     */
    private class RepaintThread implements Runnable {
        public void run() {
            while (true) {
                //调用重画方法
                repaint();


                //扫描可用串口
                commList = SerialTool.findPort();
                if (commList != null && commList.size() > 0) {

                    //添加新扫描到的可用串口
                    for (String s : commList) {

                        //该串口名是否已存在，初始默认为不存在（在commList里存在但在commChoice里不存在，则新添加）
                        boolean commExist = false;

                        for (int i = 0; i < commChoice.getItemCount(); i++) {
                            if (s.equals(commChoice.getItem(i))) {
                                //当前扫描到的串口名已经在初始扫描时存在
                                commExist = true;
                                break;
                            }
                        }

                        if (commExist) {
                            //当前扫描到的串口名已经在初始扫描时存在，直接进入下一次循环
                            continue;
                        } else {
                            //若不存在则添加新串口名至可用串口下拉列表
                            commChoice.add(s);
                        }
                    }

                    //移除已经不可用的串口
                    for (int i = 0; i < commChoice.getItemCount(); i++) {

                        //该串口是否已失效，初始默认为已经失效（在commChoice里存在但在commList里不存在，则已经失效）
                        boolean commNotExist = true;

                        for (String s : commList) {
                            if (s.equals(commChoice.getItem(i))) {
                                commNotExist = false;
                                break;
                            }
                        }

                        if (commNotExist) {
                            //System.out.println("remove" + commChoice.getItem(i));
                            commChoice.remove(i);
                        } else {
                            continue;
                        }
                    }

                } else {
                    //如果扫描到的commList为空，则移除所有已有串口
                    commChoice.removeAll();
                }

                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    String err = ExceptionWriter.getErrorInfoFromException(e);
                    JOptionPane.showMessageDialog(null, err, "错误", JOptionPane.INFORMATION_MESSAGE);
                    System.exit(0);
                }
            }
        }

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

                    log.info("found data");
                    byte[] data = null;

                    try {
                        if (serialPort == null) {
                            JOptionPane.showMessageDialog(null, "串口对象为空！监听失败！", "错误", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            data = SerialTool.readFromPort(serialPort);    //读取数据，存入字节数组
                            String dataOriginal = new String(data);
                            log.info("read data =【{}】", dataOriginal);

                            //自定义解析过程
                            HttpClientUtils utils = HttpClientUtils.getInstance();
                            String equipNo = plcChoice.getSelectedItem();
                            utils.sendTransData(equipNo, dataOriginal);

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
