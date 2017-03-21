package com.wzm.chat.net;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

import com.wzm.chat.frame.ChatEditorPane;
import com.wzm.chat.frame.ChatFrame;
import com.wzm.chat.frame.PrivateMessageFrame;
import com.wzm.util.ConfigUtil;

public class ChatClient {
	private Socket client = null;
	private BufferedReader in = null;
	private PrintWriter out = null;
	private ChatEditorPane area;
	private JList onlineUsers;
	public static String IP;
	private static final int PORT = 10614;
	private static final String SERVER_LOGOUT_HEADER = "LOGOUTCOMMAND_VERSION_2.0";
	private static final String SERVER_USER_LIST_HEADER = "USER_LIST";
	private static final String SERVER_USER_MESSAGE_HEADER = "MESSAGE";
	private static final String SERVER_P2P_CONNECT_HEADER = "P2P_CONNECT";
	public static final String CLIENT_LOGOUT_COMMAND = "LOGOUT";
	public static final String CLIENT_MASSAGE_HEADER = "MESSAGE";
	public static final String CLIENT_USER_LIST_REQUEST = "USER_LIST";
	public static final String CLIENT_P2P_REQUEST = "P2P_REQUEST";
	private boolean isConnected = false;
	private String loginString;

	public ChatClient(ChatEditorPane area, JList userList, String loginString) {
		this.area = area;
		this.onlineUsers = userList;
		this.loginString = loginString;
		IP = ConfigUtil.getIP();
	}

	public boolean connect(String IP) {
		return connect(IP, PORT);
	}

	public boolean connect(String IP, int Port) {
		if (client == null) {
			try {
				client = new Socket(IP, Port);
				// client.setReuseAddress(true);//重用本地端口，点对点连接时使用
			} catch (UnknownHostException e) {
				client = null;
				area.append("Connect server failed,Unknown Host!<br />");
				e.printStackTrace();
			} catch (IOException e) {
				client = null;
				area.append("Connect server failed,server maybe Down!<br />");
				e.printStackTrace();
			}
		}

		if (client != null) {
			try {
				in = new BufferedReader(new InputStreamReader(
						client.getInputStream()));
				out = new PrintWriter(new BufferedWriter(
						new OutputStreamWriter(client.getOutputStream())), true);

				long sessionCode = new Random().nextLong();
				out.println(loginString + sessionCode);
				if (in.readLine().equals(String.valueOf(sessionCode))) {
					area.setText("Login success.<br />");
					isConnected = true;
					new Thread(new GetMsgThread(area)).start();
				} else {
					closeClient();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return isConnected;
	}

	public boolean getOnlineState() {
		return isConnected;
	}

	public void sendMessage(String msg) {
		if (client != null) {
			if (client.isConnected()) {
				if (!client.isOutputShutdown()) {
					out.println(msg);
				}
			}
		}
	}

	public int getLocalPort() {
		if (client != null) {
			return client.getLocalPort();
		}
		return -1;
	}

	public void closeClient() {
		try {
			if (out != null) {
				out.close();
			}
			if (in != null) {
				in.close();
			}
			if (client != null) {
				client.close();
				client = null;
			}
			isConnected = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean connectionTest() {
		if (client != null) {
			if (client.isConnected()) {
				if (!client.isOutputShutdown()) {
					try {
						client.sendUrgentData(0xFF);
						client.sendUrgentData(0xFF);
						return true;
					} catch (IOException e) {
						area.append("Connection lost,please reconnect.<br />");
						onlineUsers.setModel(new DefaultListModel());
						closeClient();
						e.printStackTrace();
					}
				}
			}
		}
		return false;
	}

	class GetMsgThread implements Runnable {
		private ChatEditorPane area;

		public GetMsgThread(ChatEditorPane area) {
			this.area = area;
		}

		public void run() {
			while (isConnected) {
				String msg = null;
				String header = null;
				try {
					if ((msg = in.readLine()) != null) {
						header = msg.split(":", 2)[0];
						msg = msg.split(":", 2)[1];
						if (header.equals(SERVER_LOGOUT_HEADER))// 登出命令
						{
							closeClient();
							area.append("Logout success.<br />");
							onlineUsers.setModel(new DefaultListModel());
							break;
						} else if (header.equals(SERVER_USER_LIST_HEADER))// 服务器发来的用户列表
						{
							String[] users = msg.split(";");
							onlineUsers.setListData(users);
						} else if (header.equals(SERVER_USER_MESSAGE_HEADER))// 用户消息
						{
							area.append(msg);
							area.append("<br />");
//							JScrollPane p=(JScrollPane)area.getParent().getParent();
//							p.getViewport().setViewPosition(new Point(0,area.));
//							
//							
//							
						} else if (header.equals(SERVER_P2P_CONNECT_HEADER)) {
							boolean flag = false;// 没有打开相应窗口
							for (PrivateMessageFrame f : ChatFrame.p2pFramesSet) {
								if (f.getTitle().equals(msg)) {
									f.setExtendedState(JFrame.NORMAL);
									f.reconnectTo(loginString.split(" ")[0],
											ChatFrame.p2pFramesSet);
									flag = true;
									break;
								}
							}
							if (!flag) {
								ChatFrame.p2pFramesSet
										.add(new PrivateMessageFrame(msg,
												loginString.split(" ")[0],
												ChatClient.this,
												ChatFrame.p2pFramesSet));
							}
						}
					}
				} catch (IOException e) {
					// e.printStackTrace();
				}
			}
		}
	}
}
