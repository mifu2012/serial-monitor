package org.mif.serial.monitor.frame;

import org.mif.serial.monitor.serialport.DataView;
import org.mif.serial.monitor.serialport.HttpClientUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * @description: ��¼
 * @author: mif
 * @date: 2019/5/15 22:21
 */
public class LoginFrame extends JFrame {

    /**
     * ���������
     */
    public static final int WIDTH = 800;

    /**
     * �������߶�
     */
    public static final int HEIGHT = 620;

    /**
     * ����������λ�ã������꣩
     */
    public static final int LOC_X = 200;

    /**
     * ����������λ�ã������꣩
     */
    public static final int LOC_Y = 70;

    private static JButton loginBtn;//��½��ť
    private static JButton cancelBtn;//�������밴ť
    private static JLabel loginLabel;//��¼�İ���
    private static JFrame loginFrame;//��½�Ŀ��
    private static JTextField textField;//�û���
    private static JPasswordField passwordField;//����
    private static JLabel nameLabel;
    private static JLabel passwordLabel;

    public LoginFrame() {//��ʼ����½����
        Font font = new Font("����", Font.PLAIN, 20);//��������
        loginFrame = new JFrame("PC�˸������");
        loginFrame.setBounds(200, 70, 450, 400);
        //����½������ӱ���ͼƬ
        ImageIcon bgim = new ImageIcon("computer.png");//����ͼ��
        bgim.setImage(bgim.getImage().
                getScaledInstance(bgim.getIconWidth(),
                        bgim.getIconHeight(),
                        Image.SCALE_DEFAULT));
        loginLabel = new JLabel();
        loginLabel.setIcon(bgim);

        nameLabel = new JLabel("�û���");
        nameLabel.setBounds(20, 50, 60, 50);
        nameLabel.setFont(font);

        passwordLabel = new JLabel("����");
        passwordLabel.setBounds(20, 120, 60, 50);
        passwordLabel.setFont(font);

        loginBtn = new JButton("��½");         //���ĳ�loginButton
        loginBtn.setBounds(90, 250, 100, 50);
        loginBtn.setFont(font);

        cancelBtn = new JButton("�˳�");
        cancelBtn.setBounds(250, 250, 100, 50);
        cancelBtn.setFont(font);

        //�����ı���
        textField = new JTextField("admin");
        textField.setBounds(150, 50, 250, 50);
        textField.setFont(font);

        passwordField = new JPasswordField("123456");//���������
        passwordField.setBounds(150, 120, 250, 50);
        passwordField.setFont(font);

        loginLabel.add(textField);
        loginLabel.add(passwordField);

        loginLabel.add(nameLabel);
        loginLabel.add(passwordLabel);
        loginLabel.add(loginBtn);
        loginLabel.add(cancelBtn);

        loginFrame.add(loginLabel);
        loginFrame.setVisible(true);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        loginFrame.setLocation(300, 400);
    }

    public static void main(String[] args) {

        //��ʼ����½����
        LoginFrame hl = new LoginFrame();
        /**
         * �������¼�
         * 1.��½��ť����¼����ж��˺������Ƿ���ȷ������ȷ�����������Ϣ����
         * ��������Ӧ����ʱ����Ӧ��
         * ������ڵ�½�������һ��logLabel��ʾ�û��Ƿ��û�������ȷ
         * 2.�˳���ť��ֱ���˳�����
         */
        //��½����¼�
        ActionListener bt1_ls = arg0 -> {
            // TODO Auto-generated method stub
            String admin = textField.getText();
            char[] password = passwordField.getPassword();
            String str = String.valueOf(password); //��char����ת��Ϊstring����

            HttpClientUtils utils = HttpClientUtils.getInstance();
            String result = utils.login(admin, str);

            if (result.equals("\"success\"")) {

                DataView dataView = new DataView();
//                    dataView.setVisible(true);
                dataView.dataFrame();

                hl.loginFrame.dispose();//���ٵ�ǰ����
            } else {
                JOptionPane.showMessageDialog(null, "�ʺŻ��������", "����", JOptionPane.INFORMATION_MESSAGE);
            }

        };
        loginBtn.addActionListener(bt1_ls);

        //�˳��¼��Ĵ���
        ActionListener bt2_ls = e -> {
            System.exit(0);//��ֹ��ǰ����
        };
        cancelBtn.addActionListener(bt2_ls);

    }


}
