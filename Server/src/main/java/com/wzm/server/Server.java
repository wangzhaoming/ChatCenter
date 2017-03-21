package com.wzm.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.sleepycat.je.DatabaseException;
import com.wzm.db.MyDbEnv;
import com.wzm.net.MySocket;
import com.wzm.service.UserService;

public class Server {
	private ServerSocket server = null;
	private static final int PORT = 10614;
	private List<MySocket> sockets = new ArrayList<MySocket>();
	private ExecutorService pool = null;
	private static final String SERVER_LOGOUT_HEADER = "LOGOUTCOMMAND_VERSION_2.0";
	private static final String SERVER_USER_LIST_HEADER = "USER_LIST";
	private static final String SERVER_USER_MESSAGE_HEADER = "MESSAGE";
	private static final String CLIENT_LOGOUT_COMMAND = "LOGOUT";
	private static final String CLIENT_MASSAGE_HEADER = "MESSAGE";
	private static final String CLIENT_USER_LIST_REQUEST = "USER_LIST";
	private static final String SERVER_P2P_CONNECT_HEADER = "P2P_CONNECT";
	private static final String CLIENT_P2P_REQUEST = "P2P_REQUEST";
	private boolean isServerRunning = true;// 鏄惁鏀跺埌鍏抽棴鍛戒护
	private MyDbEnv env;
	private RegisterServer registerServer;
	private TCPNatServer tcpNatServer;

	public Server() {
		env = new MyDbEnv();
		try {
			env.setup(new File("Database"));
		} catch (DatabaseException e) {
			e.printStackTrace();
		}

		registerServer = new RegisterServer(env);
		registerServer.start();

		tcpNatServer = new TCPNatServer();
		tcpNatServer.start();

		try {
			server = new ServerSocket(PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}

		pool = Executors.newCachedThreadPool();
		MySocket client = null;

		new Thread(new consoleInputThread()).start();

		while (isServerRunning) {
			try {
				client = new MySocket(server.accept());
				BufferedReader reader = validate(client);
				if (reader != null) {
					sockets.add(client);
					pool.execute(new ChatService(client, reader));
				}
			} catch (IOException e) {
				if (isServerRunning) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) {
		System.out.println("server3.2");
		new Server();
	}

	class ChatService implements Runnable {

		private MySocket socket = null;
		private BufferedReader in = null;
		private String msg = "";

		public ChatService(MySocket socket, BufferedReader in) {
			this.socket = socket;
			this.in = in;
		}

		@Override
		public void run() {
			String content = "";
			String header = null;
			while (isServerRunning) {
				try {
					if ((content = in.readLine()) != null) {
						header = content.split(":", 2)[0];
						content = content.split(":", 2)[1];

						if (header.equals(CLIENT_LOGOUT_COMMAND))// 鎺ュ彈鍒扮櫥鍑烘秷鎭�渓ogout鈥�
						{
							msg = "Client:"
									+ socket.getSocket().getInetAddress()
									+ " logout,Online:";
							sendMsg(SERVER_LOGOUT_HEADER + ":LOGOUT",
									socket.getSocket());// 鍚戝鎴风鍙戦�佺櫥鍑哄懡浠�
							sockets.remove(socket);
							in.close();
							socket.getSocket().close();
							System.out.println(msg + sockets.size());
							break;
						} else if (header.equals(CLIENT_MASSAGE_HEADER))// 鍚戞墍鏈夌敤鎴峰彂閫佹秷鎭�
						{
							msg = SERVER_USER_MESSAGE_HEADER + ":"
									+ socket.getUsername() + "(Online:"
									+ sockets.size() + "):";
							System.out.println("Client:"
									+ socket.getSocket().getInetAddress() + " "
									+ msg);
							System.out.println(content);
							this.sendMsgToAllClient(msg);
							this.sendMsgToAllClient(SERVER_USER_MESSAGE_HEADER
									+ ": " + content);// 娑堟伅鍐呭
						} else if (header.equals(CLIENT_USER_LIST_REQUEST))// 瀹㈡埛绔姹傚湪绾跨敤鎴峰垪琛�
						{
							StringBuffer listString = new StringBuffer();
							for (MySocket mSocket : sockets) {
								listString.append(mSocket.getUsername())
										.append(";");
							}
							sendMsg(SERVER_USER_LIST_HEADER + ":"
									+ listString.toString(), socket.getSocket());
						} else if (header.equals(CLIENT_P2P_REQUEST)) {// 瀹㈡埛绔姹傜偣瀵圭偣杩炴帴
							for (MySocket mSocket : sockets) {
								if (mSocket.getUsername().equals(content)) {
									System.out.println(socket.getSocket()
											.getInetAddress()
											+ ":"
											+ socket.getSocket().getPort()
											+ " "
											+ socket.getUsername()
											+ " request connect"
											+ mSocket.getSocket()
													.getInetAddress()
											+ ":"
											+ mSocket.getSocket().getPort()
											+ ":" + mSocket.getUsername());
									sendMsg(SERVER_P2P_CONNECT_HEADER + ":"
											+ socket.getUsername(),
											mSocket.getSocket());
									break;
								}
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		private void sendMsgToAllClient(String msg) {
			for (MySocket mySocket : sockets) {
				sendMsg(msg, mySocket.getSocket());
			}
		}
	}

	private void sendMsg(String msg, Socket client) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
					client.getOutputStream())), true);
			writer.println(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private BufferedReader validate(MySocket client) {
		UserService userService = new UserService(env.getDb());
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(client.getSocket()
					.getInputStream()));
			String[] str = in.readLine().split(" ");
			if (userService.validateUser(str[0], str[1])) {
				System.out.println("Client:"
						+ client.getSocket().getInetAddress() + " login,name:"
						+ str[0] + ",Online:" + (sockets.size() + 1));
				sendMsg(str[2], client.getSocket());
				client.setUsername(str[0]);
				return in;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void shutdownAndAwaitTermination() {
		registerServer.stopServer();
		tcpNatServer.stopServer();
		isServerRunning = false;
		pool.shutdown(); // Disable new tasks from being submitted
		try {
			// Wait a while for existing tasks to terminate
			if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
				pool.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!pool.awaitTermination(60, TimeUnit.SECONDS))
					System.err.println("Pool did not terminate");
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			pool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
		try {
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		env.close();// 鍏抽棴鏁版嵁搴�
	}

	class consoleInputThread implements Runnable {
		private Scanner scanner = new Scanner(System.in);

		@Override
		public void run() {
			while (true) {
				if (scanner.next().equals("stop")) {
					shutdownAndAwaitTermination();
					break;
				}
			}
		}
	}
}
