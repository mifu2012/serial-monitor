package org.mif.serial.monitor.frame;

import org.mif.serial.monitor.serialport.DataView;
import org.mif.serial.monitor.serialport.HttpClientUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * @description: 登录
 * @author: mif
 * @date: 2019/5/15 22:21
 */
public class LoginFrame extends JFrame {

    /**
     * 程序界面宽度
     */
    public static final int WIDTH = 800;

    /**
     * 程序界面高度
     */
    public static final int HEIGHT = 620;

    /**
     * 程序界面出现位置（横坐标）
     */
    public static final int LOC_X = 200;

    /**
     * 程序界面出现位置（纵坐标）
     */
    public static final int LOC_Y = 70;

    private static JButton loginBtn;//登陆按钮
    private static JButton cancelBtn;//忘记密码按钮
    private static JLabel loginLabel;//登录的版面
    private static JFrame loginFrame;//登陆的框架
    private static JTextField textField;//用户名
    private static JPasswordField passwordField;//密码
    private static JLabel nameLabel;
    private static JLabel passwordLabel;

    public LoginFrame() {//初始化登陆界面
        Font font = new Font("黑体", Font.PLAIN, 20);//设置字体
        loginFrame = new JFrame("PC端辅助软件");
        loginFrame.setBounds(200, 70, 450, 400);
        //给登陆界面添加背景图片
        ImageIcon bgim = new ImageIcon("computer.png");//背景图案
        bgim.setImage(bgim.getImage().
                getScaledInstance(bgim.getIconWidth(),
                        bgim.getIconHeight(),
                        Image.SCALE_DEFAULT));
        loginLabel = new JLabel();
        loginLabel.setIcon(bgim);

        nameLabel = new JLabel("用户名");
        nameLabel.setBounds(20, 50, 60, 50);
        nameLabel.setFont(font);

        passwordLabel = new JLabel("密码");
        passwordLabel.setBounds(20, 120, 60, 50);
        passwordLabel.setFont(font);

        loginBtn = new JButton("登陆");         //更改成loginButton
        loginBtn.setBounds(90, 250, 100, 50);
        loginBtn.setFont(font);

        cancelBtn = new JButton("退出");
        cancelBtn.setBounds(250, 250, 100, 50);
        cancelBtn.setFont(font);

        //加入文本框
        textField = new JTextField("admin");
        textField.setBounds(150, 50, 250, 50);
        textField.setFont(font);

        passwordField = new JPasswordField("123456");//密码输入框
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

        //初始化登陆界面
        LoginFrame hl = new LoginFrame();
        /**
         * 处理点击事件
         * 1.登陆按钮点击事件，判断账号密码是否正确，若正确，弹出监测信息界面
         * 否则，无响应（暂时无响应）
         * ：后可在登陆界面添加一个logLabel提示用户是否用户密码正确
         * 2.退出按钮，直接退出程序
         */
        //登陆点击事件
        ActionListener bt1_ls = arg0 -> {
            // TODO Auto-generated method stub
            String admin = textField.getText();
            char[] password = passwordField.getPassword();
            String str = String.valueOf(password); //将char数组转化为string类型

            HttpClientUtils utils = HttpClientUtils.getInstance();
            String result = utils.login(admin, str);

            if (result.equals("\"success\"")) {

                DataView dataView = new DataView();
//                    dataView.setVisible(true);
                dataView.dataFrame();

                hl.loginFrame.dispose();//销毁当前界面
            } else {
                JOptionPane.showMessageDialog(null, "帐号或密码错误", "错误", JOptionPane.INFORMATION_MESSAGE);
            }

        };
        loginBtn.addActionListener(bt1_ls);

        //退出事件的处理
        ActionListener bt2_ls = e -> {
            System.exit(0);//终止当前程序
        };
        cancelBtn.addActionListener(bt2_ls);

    }


}
