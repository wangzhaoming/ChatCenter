package com.wzm.card.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import com.wzm.card.frames.MainFrame;
import com.wzm.card.util.Utils;

public class Client extends Playable {
	private Socket client = null;
	private BufferedReader in = null;
	private PrintWriter out = null;
	private static final int PORT = 10615;

	public Client(MainFrame frame) {
		this.frame = frame;
	}

	public boolean connect(String IP) {
		if (client == null) {
			try {
				client = new Socket(IP, PORT);
			} catch (UnknownHostException e) {
				client = null;
				e.printStackTrace();
			} catch (IOException e) {
				client = null;
				e.printStackTrace();
			}
		}

		if (client != null) {
			try {
				in = new BufferedReader(new InputStreamReader(
						client.getInputStream()));
				out = new PrintWriter(new BufferedWriter(
						new OutputStreamWriter(client.getOutputStream())), true);

				init();

				new Thread(new GetMsgThread()).start();
				
				return true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return false;
	}

	@Override
	public void init() {
		lastPlay = -1;
		try {
			id = Integer.parseInt(in.readLine());
			cards = Utils.retriveCardListFromString(in.readLine());
			hiddenCards = Utils.retriveCardListFromString(in.readLine());
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		frame.init(this);
	}

	@Override
	public void sendMsg(String msg) {
		if (client != null) {
			if (client.isConnected()) {
				if (!client.isOutputShutdown()) {
					out.println(msg);
				}
			}
		}
	}

	@Override
	public void shutdownAndAwaitTermination() {
		running = false;
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
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	class GetMsgThread implements Runnable {
		public void run() {
			while (running) {
				String msg = null;
				try {
					if ((msg = in.readLine()) != null) {
						if (msg.startsWith("init")) {
							init();
						} else {
							dealMsg(msg);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
