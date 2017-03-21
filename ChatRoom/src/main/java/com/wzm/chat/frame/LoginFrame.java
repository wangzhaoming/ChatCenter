package com.wzm.chat.frame;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.wzm.err.LoginFailedException;
import com.wzm.util.ConfigUtil;

public class LoginFrame extends JFrame {
	private JTextField username;
	private JPasswordField password;
	private JPasswordField repassword;
	private JButton loginBtn;
	private JButton regBtn;
	private JLabel repasslbl;
	private JLabel registerlbl;
	private String ip;

	public LoginFrame() {
		ip = ConfigUtil.getIP();
		init();
		addListener();

		setLayout(null);
		setSize(260, 190);
		setVisible(true);
		setResizable(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	private void init() {
		username = new JTextField();
		password = new JPasswordField();
		repassword = new JPasswordField();
		registerlbl = new JLabel("<html><u>没有帐号？点此注册</u></html>");

		JLabel namelbl = new JLabel("Username");
		JLabel passlbl = new JLabel("Password");
		repasslbl = new JLabel("Repassword");

		namelbl.setBounds(20, 23, 100, 15);
		passlbl.setBounds(20, 58, 100, 15);
		repasslbl.setBounds(10, 93, 100, 15);
		username.setBounds(100, 20, 120, 25);
		password.setBounds(100, 55, 120, 25);
		repassword.setBounds(100, 90, 120, 25);
		registerlbl.setBounds(65, 120, 120, 25);

		add(namelbl);
		add(passlbl);
		add(username);
		add(password);
		add(registerlbl);

		loginBtn = new JButton("Login");
		loginBtn.setBounds(80, 90, 80, 30);

		regBtn = new JButton("Register");
		regBtn.setBounds(60, 120, 120, 30);

		add(loginBtn);
	}

	private void addListener() {
		loginBtn.addActionListener(new LoginActionHandler());
		username.addActionListener(new LoginActionHandler());
		password.addActionListener(new LoginActionHandler());

		regBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String name = username.getText().trim();
				String pass = new String(password.getPassword()).trim();
				String repass = new String(repassword.getPassword()).trim();

				if (name.equals("") || pass.equals("") || repass.equals("")) {
					JOptionPane.showMessageDialog(LoginFrame.this, "用户名密码不能为空");
				} else if (!pass.equals(repass)) {
					JOptionPane.showMessageDialog(LoginFrame.this, "两次输入的密码不同");
				} else {
					BufferedReader in = null;
					BufferedWriter out = null;
					Socket socket = null;
					try {
						socket = new Socket(ip, 10613);
						in = new BufferedReader(new InputStreamReader(socket
								.getInputStream()));
						out = new BufferedWriter(new OutputStreamWriter(socket
								.getOutputStream()));

						out.write(name + " " + pass + "\r\n");
						out.flush();

						if (in.readLine().trim().equals("ok")) {
							JOptionPane.showMessageDialog(LoginFrame.this,
									"注册成功");
							LoginFrame.this.dispose();
							new LoginFrame();
						} else {
							JOptionPane.showMessageDialog(LoginFrame.this,
									"该用户已被注册");
						}

					} catch (UnknownHostException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						if (in != null) {
							try {
								in.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						if (out != null) {
							try {
								out.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						if (socket != null) {
							try {
								socket.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		});

		registerlbl.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent arg0) {
				registerlbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
			}

			@Override
			public void mouseClicked(MouseEvent arg0) {
				username.removeActionListener(username.getActionListeners()[0]);
				password.removeActionListener(password.getActionListeners()[0]);

				LoginFrame.this.add(repassword);
				LoginFrame.this.add(repasslbl);
				LoginFrame.this.add(regBtn);
				loginBtn.setVisible(false);
				registerlbl.setVisible(false);

				LoginFrame.this.repaint();
			}
		});
	}

	class LoginActionHandler implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			try {
				new ChatFrame(username.getText().trim(), new String(
						password.getPassword()));
				LoginFrame.this.dispose();
			} catch (LoginFailedException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		new LoginFrame();
	}
}
