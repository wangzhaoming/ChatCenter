package com.wzm.card.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.wzm.card.frames.MainFrame;
import com.wzm.card.logic.Cards;

public class Server extends Playable {
	private ServerSocket server = null;
	private static final int PORT = 10615;
	private Socket[] sockets = new Socket[2];
	private ExecutorService pool = null;

	public Server(MainFrame frame) {
		this.frame = frame;
		try {
			server = new ServerSocket(PORT, 2);
		} catch (IOException e) {
			e.printStackTrace();
		}

		pool = Executors.newCachedThreadPool();

		for (int i = 0; i < 2; i++) {
			try {
				sockets[i] = server.accept();
				pool.execute(new ChatService(sockets[i]));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (server != null) {
			try {
				server.close();
				server = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		init();
	}

	class ChatService implements Runnable {

		private BufferedReader in = null;

		public ChatService(Socket socket) {
			try {
				this.in = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			String msg = "";
			while (running) {
				try {
					if ((msg = in.readLine()) != null) {
						sendMsg(msg);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (in != null) {
				try {
					in.close();
					in = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void sendMsgToAllClient(String msg) {
		for (Socket socket : sockets) {
			sendMsg(msg, socket);
		}
	}

	@Override
	public void sendMsg(String msg) {
		sendMsgToAllClient(msg);
		if (msg.startsWith("init")) {
			init();
		} else {
			dealMsg(msg);
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

	@Override
	public void shutdownAndAwaitTermination() {
		running = false;
		for (int i = 0; i < 2; i++) {
			if (sockets[i] != null) {
				try {
					sockets[i].close();
					sockets[i] = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

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
		
		if (server != null) {
			try {
				server.close();
				server = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void init() {
		lastPlay = -1;
		Cards cards = new Cards();

		this.cards = cards.getCards(0);
		hiddenCards = cards.getHiddenCards();

		frame.init(this);

		sendMsg("0", sockets[0]);
		sendMsg(cards.getCards(1).toString(), sockets[0]);
		sendMsg(cards.getHiddenCards().toString(), sockets[0]);

		sendMsg("2", sockets[1]);
		sendMsg(cards.getCards(2).toString(), sockets[1]);
		sendMsg(cards.getHiddenCards().toString(), sockets[1]);

		sendMsg("start:" + (int) (Math.random() * 3));
		// sendMsg("start:1");
	}
}
