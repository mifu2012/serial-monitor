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
 * ���������ʾ��
 *
 * @author Zhong
 */
public class DataView extends Frame {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private List<EquipmentVO> pclList; //PLC�б�

    private List<String> commList;    //������ö˿ں�
    public static SerialPort serialPort = null;    //���洮�ڶ���


    private Choice plcChoice = new Choice();    //PLC���루������

    private Font font = new Font("΢���ź�", Font.BOLD, 25);

    private TextField baudRate = new TextField("��������", Label.CENTER);    //������
    private JComboBox equipmentLength = new JComboBox(new String[]{"0x0000","0x1000"});    //����
    private JComboBox parityBit = new JComboBox(new String[]{"0x0000","0x0400","0x0500"});    //����λ
    private JComboBox stopBit = new JComboBox(new String[]{"0x0000","0x1000","0x2000","0x3000"});    //ֹͣλ
    private JComboBox linkedMethod = new JComboBox(new String[]{"0x00","0x01"});    //���ӷ�ʽ

    private Choice commChoice = new Choice();    //����ѡ��������

    private Button openSerialButton = new Button("�򿪴���");
    private Button updateButton = new Button("�޸�");

    private static String plcChannelId;


    Image offScreen = null;    //�ػ�ʱ�Ļ���

    //����window��icon
    Toolkit toolKit = getToolkit();
    Image icon = toolKit.getImage("computer.png");

    /**
     * ��Ĺ��췽��
     */
    public DataView() {
        this.setBounds(200, 70, 800, 620);    //�趨������������ֵ�λ��
        this.setTitle("CDIO������Ŀ");    //���ó������
        this.setBackground(Color.white);    //���ñ���ɫ

        this.addWindowListener(new WindowAdapter() {
            //��ӶԴ���״̬�ļ���
            public void windowClosing(WindowEvent arg0) {
                //�����ڹر�ʱ
                System.exit(0);    //�˳�����
            }

        });

        this.setResizable(false);    //���ڴ�С���ɸ���
        this.setVisible(true);    //��ʾ����

        //�����ʼ��ʱ��ɨ��һ����Ч����
        commList = SerialTool.findPort();
        HttpClientUtils httpClientUtils = HttpClientUtils.getInstance();
        //�����ʼ��ʱ��ɨ��һ����Ч����
        pclList = httpClientUtils.getPlcList();
    }

    /**
     * ���˵�������ʾ��
     * ���Label����ť��������������¼�������
     */
    public void dataFrame() {
        this.setBounds(200, 70, 800, 620);
        this.setTitle("PC�������");
        this.setIconImage(icon);
        this.setBackground(Color.white);
        this.setLayout(null);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent arg0) {
                if (serialPort != null) {
                    //�����˳�ʱ�رմ����ͷ���Դ
                    SerialTool.closePort(serialPort);
                }
                System.exit(0);
            }

        });

        //���plcѡ��ѡ��
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
            plcChoice.add("--����PLC--");
        }
        add(plcChoice);

        //�����¼�����
        plcChoice.addItemListener(e -> {
            String equipNO = (String) e.getItem();
            HttpClientUtils httpClientUtils = HttpClientUtils.getInstance();
            EquipmentVO vo = httpClientUtils.getPlcDetail(equipNO);
            if (null == vo) {
                JOptionPane.showMessageDialog(null, "û�л�ȡ��PLC���ݣ�", "����", JOptionPane.INFORMATION_MESSAGE);
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

        //��Ӵ���ѡ��ѡ��
        commChoice.setBounds(160, 397, 200, 200);
        //����Ƿ��п��ô��ڣ��������ѡ����
        if (commList == null || commList.size() < 1) {
            JOptionPane.showMessageDialog(null, "û����������Ч���ڣ�", "����", JOptionPane.INFORMATION_MESSAGE);
        } else {
            for (String s : commList) {
                commChoice.add(s);
            }
        }
        add(commChoice);


        //�޸İ�ť
        updateButton.setBounds(150, 490, 100, 50);
        updateButton.setBackground(Color.orange);
        updateButton.setFont(new Font("΢���ź�", Font.BOLD, 20));
        updateButton.setForeground(Color.darkGray);
        add(updateButton);

        //��Ӵ򿪴��ڰ�ť
        openSerialButton.setBounds(420, 490, 100, 50);
        openSerialButton.setBackground(Color.orange);
        openSerialButton.setFont(new Font("΢���ź�", Font.BOLD, 20));
        openSerialButton.setForeground(Color.darkGray);
        add(openSerialButton);

        //��Ӵ򿪴��ڰ�ť���¼�����
        openSerialButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                //��ȡ��������
                String commName = commChoice.getSelectedItem();

                //��鴮�������Ƿ��ȡ��ȷ
                if (commName == null || commName.equals("")) {
                    JOptionPane.showMessageDialog(null, "û����������Ч���ڣ�", "����", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    //��鲨�����Ƿ��ȡ��ȷ
//                    if (bpsStr == null || bpsStr.equals("")) {
//                        JOptionPane.showMessageDialog(null, "�����ʻ�ȡ����", "����", JOptionPane.INFORMATION_MESSAGE);
//                    } else {
                    //�������������ʾ���ȡ��ȷʱ
//                    String bpsStr = baudRate.getText(); TODO
                    String bpsStr = null;
                    if (StringUtils.isEmpty(bpsStr) || bpsStr.equals("��������")) {
                        JOptionPane.showMessageDialog(null, "��������PLC���ϣ�", "����", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    int bps = Integer.parseInt(bpsStr);
                    try {

                        //��ȡָ���˿����������ʵĴ��ڶ���
                        serialPort = SerialTool.openPort(commName, bps);
                        //�ڸô��ڶ�������Ӽ�����
                        SerialTool.addListener(serialPort, new SerialListener());
                        //�����ɹ�������ʾ
                        JOptionPane.showMessageDialog(null, "�����ɹ����Ժ���ʾ������ݣ�", "��ʾ", JOptionPane.INFORMATION_MESSAGE);

                        // tcp ע��
                        Channel localhost = new NettyClient(Constants.REMOTE_ADDR, Constants.REMOTE_N_PORT).run();

                        //Http
                        localhost.writeAndFlush(Unpooled.copiedBuffer(("BIND_" + plcChannelId).getBytes()));


                    } catch (Exception e1) {
                        //��������ʱʹ��һ��Dialog��ʾ����Ĵ�����Ϣ
                        JOptionPane.showMessageDialog(null, e1, "����", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
//                    }
                }

            }
        });


        this.setResizable(false);

    }

    /**
     * �������������Ԫ��
     */
    @Override
    public void paint(Graphics g) {
        Color c = g.getColor();

        g.setColor(Color.orange);
        g.setFont(new Font("΢���ź�", Font.BOLD, 20));
        g.drawString(" PLC��ţ� ", 50, 80);

        g.setColor(Color.black);
        g.setFont(new Font("΢���ź�", Font.BOLD, 20));
        g.drawString(" �����ʣ� ", 50, 150);

        g.setColor(Color.black);
        g.setFont(new Font("΢���ź�", Font.BOLD, 20));
        g.drawString(" ���ȣ� ", 425, 150);

        g.setColor(Color.black);
        g.setFont(new Font("΢���ź�", Font.BOLD, 20));
        g.drawString(" ����λ�� ", 50, 240);

        g.setColor(Color.black);
        g.setFont(new Font("΢���ź�", Font.BOLD, 20));
        g.drawString(" ֹͣλ�� ", 425, 240);

        g.setColor(Color.black);
        g.setFont(new Font("΢���ź�", Font.BOLD, 20));
        g.drawString(" ���ӷ�ʽ�� ", 50, 320);

        g.setColor(Color.orange);
        g.setFont(new Font("΢���ź�", Font.BOLD, 20));
        g.drawString(" ����ѡ�� ", 50, 410);

    }

    /**
     * ���ڲ�����ʽ����һ�����ڼ�����
     *
     * @author zhong
     */
    private class SerialListener implements SerialPortEventListener {

        /**
         * �����ص��Ĵ����¼�
         */
        @Override
        public void serialEvent(SerialPortEvent serialPortEvent) {

            switch (serialPortEvent.getEventType()) {

                case SerialPortEvent.BI: // 10 ͨѶ�ж�
                    JOptionPane.showMessageDialog(null, "�봮���豸ͨѶ�ж�", "����", JOptionPane.INFORMATION_MESSAGE);
                    break;

                case SerialPortEvent.OE: // 7 ��λ�����������

                case SerialPortEvent.FE: // 9 ֡����

                case SerialPortEvent.PE: // 8 ��żУ�����

                case SerialPortEvent.CD: // 6 �ز����

                case SerialPortEvent.CTS: // 3 �������������

                case SerialPortEvent.DSR: // 4 ����������׼������

                case SerialPortEvent.RI: // 5 ����ָʾ

                case SerialPortEvent.OUTPUT_BUFFER_EMPTY: // 2 ��������������
                    break;

                case SerialPortEvent.DATA_AVAILABLE: // 1 ���ڴ��ڿ�������

                    byte[] data = null;

                    try {
                        if (serialPort == null) {
                            JOptionPane.showMessageDialog(null, "���ڶ���Ϊ�գ�����ʧ�ܣ�", "����", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            data = SerialTool.readFromPort(serialPort);    //��ȡ���ݣ������ֽ�����
                            System.out.println("read data=" + data.toString());
                            //�Զ����������
                            Channel channel = NettyClient.channel;
                            if (null != channel) {
                                channel.writeAndFlush(Unpooled.copiedBuffer(data));
                            }
                        }

                    } catch (ReadDataFromSerialPortFailure e) {
                        JOptionPane.showMessageDialog(null, e, "����", JOptionPane.INFORMATION_MESSAGE);
                        System.exit(0);    //������ȡ����ʱ��ʾ������Ϣ���˳�ϵͳ
                    } catch (SerialPortInputStreamCloseFailure serialPortInputStreamCloseFailure) {
                        serialPortInputStreamCloseFailure.printStackTrace();
                    }

                    break;
            }

        }

    }

}
