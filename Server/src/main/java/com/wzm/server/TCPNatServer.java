package com.wzm.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class TCPNatServer extends Thread {
	private static final String REQUEST = "REQUEST";
	private static final String RESPONSE = "RESPONSE";
	private ServerSocket server = null;
	private Socket client = null;
	private static final int PORT = 10612;
	private boolean isServerRunning = true;// 是否收到关闭命令
	private HashMap<String, String> info;// key：用户名 value：ip:port
	private PrintWriter tmpOut = null;
	private BufferedReader tmpIn = null;

	public TCPNatServer() {
		info = new HashMap<String, String>();
		try {
			server = new ServerSocket(PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (isServerRunning) {
			try {
				client = server.accept();

				tmpIn = new BufferedReader(new InputStreamReader(
						client.getInputStream()));
				tmpOut = new PrintWriter(new BufferedWriter(
						new OutputStreamWriter(client.getOutputStream())), true);

				String[] str = tmpIn.readLine().split(":", 2);
				String header = str[0];
				String msg = str[1];

				if (header.equals(REQUEST)) {
					System.out.println(client.getInetAddress().toString() + ":"
							+ client.getPort() + " is listening " + msg
							+ " to connect");
					info.put(msg, client.getInetAddress().toString() + ":"
							+ client.getPort());
				} else if (header.equals(RESPONSE)) {
					System.out.println(client.getInetAddress().toString() + ":"
							+ client.getPort() + " is connected to "
							+ info.get(msg));
					tmpOut.println(info.get(msg));
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
					client.close();
					client = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) {
		new TCPNatServer().start();
	}

	public void stopServer() {
		isServerRunning = false;
		try {
			if (server != null) {
				server.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
