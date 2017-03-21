package com.wzm.chat.frame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.HashSet;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.wzm.chat.net.ChatClient;
import com.wzm.chat.net.FileReceiver;
import com.wzm.chat.net.FileSender;
import com.wzm.util.ConfigUtil;

public class PrivateMessageFrame extends JFrame {
	private String name;// 对方的用户名
	private String myName;
	private JTextArea area;
	private JTextField txtField;
	private JScrollPane scrollPane;
	private JButton fileSendBtn;
	private BufferedReader in = null;
	private PrintWriter out = null;
	private int port;
	private ServerSocket server = null;
	private Socket socket = null;
	private Socket tmpSocket = null;
	private PrintWriter tmpOut = null;
	private BufferedReader tmpIn = null;
	private HashSet<PrivateMessageFrame> p2pFramesSet;
	public static final int SERVER = 0;
	public static final int CLIENT = 1;
	private static final String REQUEST = "REQUEST";
	private static final String RESPONSE = "RESPONSE";
	private static final String DISCONNECT_COMMAND = "!!!DISCONNECT!!!";
	private static final String FILE_SEND_REQUEST = "!!!FILE_SEND_PLEASE_RECV!!!";
	private static final String FILE_SEND_RESPONSE = "!!!FILE_RECV_OK!!!";
	private int serverOrClient;
	private boolean isConnected = false;
	private ChatClient client;
	private File file;

	public PrivateMessageFrame(String name, ChatClient client,
			HashSet<PrivateMessageFrame> connectedSet) {
		this.serverOrClient = SERVER;
		this.client = client;
		this.setTitle(name);
		this.name = name;
		this.p2pFramesSet = connectedSet;

		init();
	}

	public PrivateMessageFrame(String name, String myName, ChatClient client,
			HashSet<PrivateMessageFrame> connectedSet) {
		this(name, client, connectedSet);

		this.serverOrClient = CLIENT;
		this.myName = myName;

		connect();
	}

	public void reconnectTo(String myName,
			HashSet<PrivateMessageFrame> connectedSet) {
		disconnect();

		this.serverOrClient = CLIENT;
		this.myName = myName;
		this.p2pFramesSet = connectedSet;

		connect();
	}

	private void sendSocketInfo(String header, String info) {
		try {
			tmpSocket = new Socket(InetAddress.getByName(ConfigUtil.getIP()),
					10612);
			tmpSocket.setReuseAddress(true);

			tmpIn = new BufferedReader(new InputStreamReader(
					tmpSocket.getInputStream()));
			tmpOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
					tmpSocket.getOutputStream())), true);
			tmpOut.println(header + ":" + info);

			port = tmpSocket.getLocalPort();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void connect() {
		try {
			if (serverOrClient == SERVER)// 发起方为服务器
			{
				client.sendMessage(ChatClient.CLIENT_P2P_REQUEST + ":" + name);
				sendSocketInfo(REQUEST, name);
				server = new ServerSocket(port);
				server.setSoTimeout(5000);
				socket = server.accept();
			} else if (serverOrClient == CLIENT)// 请求响应方
			{
				sendSocketInfo(RESPONSE, myName);

				String[] str = tmpIn.readLine().split(":");

				socket = new Socket(InetAddress.getByName(str[0].substring(1)),
						Integer.parseInt(str[1]));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			tmpOut.close();
			tmpOut = null;
			try {
				tmpIn.close();
				tmpIn = null;
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				tmpSocket.close();
				tmpSocket = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (socket != null) {
			try {
				in = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
				out = new PrintWriter(new BufferedWriter(
						new OutputStreamWriter(socket.getOutputStream())), true);

				new Thread(new getMsgThread()).start();
				isConnected = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private void disconnect() {
		try {
			if (out != null) {
				out.close();
			}
			if (in != null) {
				in.close();
			}
			if (socket != null) {
				socket.close();
				socket = null;
			}
			if (server != null) {
				server.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		isConnected = false;
	}

	private void init() {
		setLayout(null);

		area = new JTextArea();
		area.setBounds(0, 0, 300, 300);
		area.setEditable(false);
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		scrollPane = new JScrollPane(area);
		scrollPane.setBounds(0, 0, 300, 300);

		txtField = new JTextField();
		txtField.setBounds(0, 310, 300, 20);

		add(scrollPane);
		add(txtField);

		fileSendBtn = new JButton("发送文件");
		fileSendBtn.setBounds(10, 340, 100, 25);
		add(fileSendBtn);

		fileSendBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser chooser = new JFileChooser();
				// FileNameExtensionFilter filter = new FileNameExtensionFilter(
				// "JPG & GIF Images", "jpg", "gif");
				// chooser.setFileFilter(filter);
				int returnVal = chooser
						.showOpenDialog(PrivateMessageFrame.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					tryConnect();
//					System.out.println("You chose to open this file: "
//							+ chooser.getSelectedFile().getName());
					file = chooser.getSelectedFile();
					sendMessage(FILE_SEND_REQUEST + ":"
							+ chooser.getSelectedFile().getName()+":"+chooser.getSelectedFile().length());
				}
			}
		});

		setSize(315, 400);
		setVisible(true);
		setResizable(false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				sendMessage(DISCONNECT_COMMAND);
				disconnect();
				p2pFramesSet.remove(PrivateMessageFrame.this);
			}
		});

		txtField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				tryConnect();
				if (sendMessage(txtField.getText())) {
					area.append("我:\r\n");
					area.append(txtField.getText());
					area.append("\r\n");
				}
				txtField.setText("");
			}
		});
	}

	public JTextField getTxtField() {
		return txtField;
	}

	private void tryConnect() {
		if (!isConnected) {
			serverOrClient = SERVER;
			connect();
		}
	}

	public boolean sendMessage(String msg) {
		if (socket != null) {
			if (socket.isConnected()) {
				if (!socket.isOutputShutdown()) {
					out.println(msg);
					return true;
				}
			}
		}
		return false;
	}

	class getMsgThread implements Runnable {
		public void run() {
			while (true) {
				String msg = null;
				try {
					if ((msg = in.readLine()) != null) {
						if (msg.equals(DISCONNECT_COMMAND)) {
							disconnect();
						} else if (msg.startsWith(FILE_SEND_REQUEST)) {
							if (JOptionPane.showConfirmDialog(
									PrivateMessageFrame.this,
									"对方传送文件:" + msg.split(":", 3)[1]
											+ ",是否接收文件？", "文件接收确认",
									JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
								tryConnect();
								String path = Thread.currentThread()
										.getContextClassLoader()
										.getResource("").toURI().getPath()
										+ msg.split(":", 3)[1];
								long length=Long.parseLong(msg.split(":", 3)[2]);
//								String path="/home/soft01/receivedfiles/"+msg.split(":", 2)[1];
								sendMessage(FILE_SEND_RESPONSE);
								// 接受文件

								new FileReceiver(PrivateMessageFrame.this,path, socket.getInputStream(),length).receive();

							}
						} else if (msg.startsWith(FILE_SEND_RESPONSE)) {// 获得对方同意，开始发送
							new FileSender(PrivateMessageFrame.this,file, socket.getOutputStream())
									.send();
						} else {
							area.append(name + ":\r\n");
							area.append(msg);
							area.append("\r\n");
							area.setCaretPosition(area.getText().length());
						}
					}
				} catch (IOException e) {
					break;
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
