package com.wzm.chat.frame;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import com.wzm.card.frames.MainFrame;
import com.wzm.chat.net.ChatClient;
import com.wzm.err.LoginFailedException;
import com.wzm.mine.frame.MineFrame;

public class ChatFrame extends JFrame {
	private ChatPanel chatPanel;
	private JMenuBar mb;
	private JMenu gameMenu;
	private JMenuItem mineItem;
	private JMenuItem cardItem;
	private JMenu stateMenu;
	private JMenuItem onlineStateItem;
	private JMenuItem offlineStateItem;
	private JMenu helpMenu;
	private JMenuItem aboutMenuItem;
	private String username;
	private String password;
	private JList userList;
	public static HashSet<PrivateMessageFrame> p2pFramesSet = new HashSet<PrivateMessageFrame>();

	public static MainFrame cardGameFrame;
	public static MineFrame mineGameFrame;

	public ChatFrame(String username, String password)
			throws LoginFailedException {
		this.username = username;
		this.password = password;

		init();
		addListeners();

		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);

		doConnectionTest();
	}

	private void doConnectionTest()// 启动线程向服务器发送检测信息，判断服务器状态
	{
		new Thread(new Runnable() {
			public void run() {
				while (getClient().getOnlineState()) {
					getClient().connectionTest();
					getClient().sendMessage(
							ChatClient.CLIENT_USER_LIST_REQUEST + ":getlist");// 获取用户列表
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				stateMenu.setText("Offline");
				stateMenu.setForeground(Color.red);
			}
		}).start();
	}

	private void init() throws LoginFailedException {
		gameMenu = new JMenu("Game");
		mineItem = new JMenuItem("扫雷");
		cardItem = new JMenuItem("斗地主");
		gameMenu.add(mineItem);
		gameMenu.add(cardItem);

		stateMenu = new JMenu("Online");
		onlineStateItem = new JMenuItem("Online");
		onlineStateItem.setForeground(Color.green);
		offlineStateItem = new JMenuItem("Offline");
		offlineStateItem.setForeground(Color.red);
		stateMenu.add(onlineStateItem);
		stateMenu.add(offlineStateItem);

		helpMenu = new JMenu("Help");
		aboutMenuItem = new JMenuItem("About");
		helpMenu.add(aboutMenuItem);

		mb = new JMenuBar();
		mb.add(gameMenu);
		mb.add(stateMenu);
		mb.add(helpMenu);

		setJMenuBar(mb);

		setLayout(null);
		setSize(400, 400);
		userList = new JList();
		userList.setBounds(260, 0, 120, 280);
		JScrollPane scrollPane = new JScrollPane(userList);
		scrollPane.setBounds(userList.getBounds());

		chatPanel = new ChatPanel(username + " " + password + " ", userList);
		chatPanel.setBounds(0, 0, 250, 280);

		add(scrollPane);
		add(chatPanel);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setStateMenu();
	}

	private void setStateMenu() {
		if (getClient().getOnlineState()) {
			stateMenu.setText("Online");
			stateMenu.setForeground(Color.green);
		} else {
			stateMenu.setText("Offline");
			stateMenu.setForeground(Color.red);
		}

	}

	private void addListeners() {
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				chatPanel.closeClient();
			}
		});

		mineItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (mineGameFrame == null) {
					mineGameFrame = new MineFrame();
					mineGameFrame.addWindowListener(new WindowAdapter() {
						@Override
						public void windowClosing(WindowEvent e) {
							mineGameFrame = null;
						}
					});
				} else {
					mineGameFrame.setExtendedState(NORMAL);
				}
			}
		});

		cardItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (cardGameFrame == null) {
					cardGameFrame = new MainFrame();
					cardGameFrame.addWindowListener(new WindowAdapter() {
						@Override
						public void windowClosing(WindowEvent e) {
							cardGameFrame = null;
						}
					});
				} else {
					cardGameFrame.setExtendedState(NORMAL);
				}
			}
		});

		onlineStateItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (!getClient().getOnlineState()) {
					if (getClient().connect(ChatClient.IP)) {
						stateMenu.setText("Online");
						stateMenu.setForeground(Color.green);
						doConnectionTest();
					}
				}
			}
		});

		offlineStateItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (getClient().getOnlineState()) {
					getClient().sendMessage(
							ChatClient.CLIENT_LOGOUT_COMMAND + ":" + "logout");
				}
				stateMenu.setText("Offline");
				stateMenu.setForeground(Color.red);
			}
		});

		aboutMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(ChatFrame.this,
						"ChatRoom ver4.1\n" + "powered by wzm.", "About",
						JOptionPane.INFORMATION_MESSAGE);
			}
		});

		userList.addMouseListener(new MouseAdapter()// 双击用户列表发起点对点连接
		{
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1
						&& e.getClickCount() == 2) {
					String name = ((JList) e.getComponent()).getSelectedValue()
							.toString();
					if (!name.equals(username)) {
						boolean flag = false;// 没有打开相应窗口
						for (PrivateMessageFrame f : p2pFramesSet) {
							if (f.getTitle().equals(name)) {
								f.setExtendedState(NORMAL);
								flag = true;
								break;
							}
						}
						if (!flag) {
							p2pFramesSet.add(new PrivateMessageFrame(name,
									getClient(), p2pFramesSet));
						}
					}
				}
			}
		});
	}

	private ChatClient getClient() {
		return chatPanel.getClient();
	}
}
