package com.wzm.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import com.sleepycat.je.DatabaseException;
import com.wzm.db.DataAccess;
import com.wzm.db.DataAccessFactory;
import com.wzm.db.MyDbEnv;

public class RegisterServer extends Thread {
	private ServerSocket server = null;
	private static final int PORT = 10613;
	private MyDbEnv env;
	private boolean isServerRunning = true;// 是否收到关闭命令

	public RegisterServer(MyDbEnv env) {
		this.env = env;
		try {
			env.setup(new File("Database"));
		} catch (DatabaseException e) {
			e.printStackTrace();
		}

		try {
			server = new ServerSocket(PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		Socket client = null;

		while (isServerRunning) {
			try {
				client = server.accept();
				putNewUser(client);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					client.close();
					client = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void putNewUser(Socket client) {
		BufferedReader reader = null;
		BufferedWriter writer = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
					client.getInputStream()));
			writer = new BufferedWriter(new OutputStreamWriter(
					client.getOutputStream()));
			String[] str = reader.readLine().split(" ");

			DataAccess dataAccess = DataAccessFactory
					.getNoOverwriteDataAccess(env.getDb());

			if (dataAccess.put(str[0], str[1])) {
				writer.write("ok\r\n");
			} else {
				writer.write("err\r\n");
			}
			writer.flush();
		} catch (IOException | DatabaseException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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
