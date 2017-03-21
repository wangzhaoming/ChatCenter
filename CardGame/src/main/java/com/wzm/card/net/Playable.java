package com.wzm.card.net;

import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;

import com.wzm.card.frames.MainFrame;
import com.wzm.card.logic.Card;

public abstract class Playable {
	protected int id = 1;
	protected List<Card> cards;
	protected List<Card> hiddenCards;
	protected MainFrame frame;
	protected int lastPlay = -1;
	protected int restartNum = 0;
	protected boolean running = true;

	public abstract void sendMsg(String msg);

	public abstract void init();
	
	public abstract void shutdownAndAwaitTermination();

	public List<Card> getInitialCards() {
		return cards;
	}

	public List<Card> getHiddenCards() {
		return hiddenCards;
	}

	public int getId() {
		return id;
	}

	public int getLeftId() {
		return (id + 3 - 1) % 3;
	}

	public int getRightId() {
		return (id + 3 + 1) % 3;
	}

	public void changePlayer() {
		sendMsg("enable:" + (id + 1) % 3);
	}

	protected void dealMsg(String msg) {
		if (msg.startsWith("enable")) {
			frame.drawArrow(Integer.parseInt(msg.split(":")[1]));
			if (msg.split(":")[1].equals("" + id)) {
				frame.setPlayEnabled(true);
			}
		} else if (msg.startsWith("start")) {
			if (msg.split(":")[1].equals("" + id)) {
				if (lastPlay != id) {
					if (JOptionPane.showConfirmDialog(frame, "抢地主", "",
							JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						cards.addAll(hiddenCards);
						Collections.sort(cards);
						sendMsg("add:" + id + cards.toString());
						frame.setPlayEnabled(true);
						return;
					}
					sendMsg("start:" + (id + 1) % 3);
					lastPlay = id;
				} else {
					cards.addAll(hiddenCards);
					Collections.sort(cards);
					sendMsg("add:" + id + cards.toString());
					frame.setPlayEnabled(true);
				}
			}
		} else if (msg.startsWith("add")) {
			lastPlay = Integer.parseInt(msg.split(":")[1].substring(0, 1));
			frame.addHiddenCards(msg.split(":")[1]);
		} else if (msg.equals("restart")) {
			if (JOptionPane.showConfirmDialog(frame, "重新开始？", "",
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				sendMsg("restart:OK");
			}
		} else if (msg.equals("restart:OK")) {
			restartNum++;
			if (restartNum == 3) {
				if (id == 1) {
					sendMsg("init");
				}
				restartNum = 0;
			}
		} else {
			frame.updatePane(msg);
		}
	}

	public int getLastPlay() {
		return lastPlay;
	}

	public void setLastPlay(int lastPlay) {
		this.lastPlay = lastPlay;
	}
}
