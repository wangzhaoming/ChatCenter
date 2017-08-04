package com.wzm.card.frames;

import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JTextField;

import com.wzm.card.logic.Card;
import com.wzm.card.logic.pattern.BasePattern;
import com.wzm.card.logic.pattern.PatternFactory;
import com.wzm.card.net.Client;
import com.wzm.card.net.Playable;
import com.wzm.card.net.Server;
import com.wzm.card.util.Utils;

public class MainFrame extends JFrame {
	private JLayeredPane cardPaneLeft = new JLayeredPane();
	private JLayeredPane cardPaneBot = new JLayeredPane();
	private JLayeredPane cardPaneRight = new JLayeredPane();
	private JLayeredPane cardPaneMid = new JLayeredPane();
	private HashMap<JLabel, Card> selectedCards = new HashMap<JLabel, Card>();
	private HashMap<JLabel, Card> allMyCards = new HashMap<JLabel, Card>();
	private Playable client;
	private BasePattern lastPlayedCards;

	private JLabel[] hiddenCardsLbl = new JLabel[3];
	private JLabel diZhuLbl = new JLabel("地主");
	private JLabel arrowLbl = new JLabel();
	private JButton sendBtn = new JButton("出牌");
	private JButton passBtn = new JButton("PASS");

	private JButton createGameBtn = new JButton("创建游戏");
	private JButton joinGameBtn = new JButton("加入游戏");

	private JLabel ipLabel = new JLabel();
	private JTextField ipInputTxt = new JTextField();

	public void connect(String ip) {
		ipInputTxt.setText(ip);
		joinGameBtn.doClick();
	}

	public MainFrame(String ip) {
		this();
		ipInputTxt.setText(ip);
		joinGameBtn.doClick();
	}

	public MainFrame() {
		setLayout(null);
		Insets insets = new Insets(0, 0, 0, 0);

		JLabel versionLbl = new JLabel("斗地主 ver 1.2, powered by wzm.");
		versionLbl.setBounds(0, 0, 300, 20);
		add(versionLbl);

		diZhuLbl.setSize(60, 40);
		arrowLbl.setSize(40, 40);
		add(arrowLbl);
		ipLabel.setBounds(420, 20, 100, 20);
		ipLabel.setText(Utils.getLocalIP());
		add(ipLabel);

		ipInputTxt.setBounds(420, 50, 90, 20);
		add(ipInputTxt);

		cardPaneBot.setBounds(50, 400, 500, 150);
		add(cardPaneBot);

		cardPaneLeft.setBounds(50, 100, 66, 500);
		add(cardPaneLeft);

		cardPaneRight.setBounds(484, 100, 66, 500);
		add(cardPaneRight);

		cardPaneMid.setBounds(180, 200, 240, 150);
		add(cardPaneMid);

		sendBtn.setBounds(320, 530, 80, 20);
		passBtn.setBounds(200, 530, 80, 20);

		passBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sendBtn.setVisible(false);
				passBtn.setVisible(false);
				client.changePlayer();
			}
		});

		sendBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectedCards.size() != 0) {
					BasePattern pattern = PatternFactory.parsePattern(Utils
							.retriveCardListFromMap(selectedCards));
					if (pattern == null) {
						return;
					}

					if (client.getLastPlay() != client.getId()) {
						if (!lastPlayedCards.validate(pattern)) {
							return;
						}
					}

					for (JLabel lbl : selectedCards.keySet()) {
						allMyCards.remove(lbl);
					}

					client.sendMsg(""
							+ client.getId()
							+ Utils.retriveCardListFromMap(selectedCards)
									.toString());
					selectedCards.clear();
					showCards(cardPaneBot,
							Utils.retriveLabelListFromMap(allMyCards));

					sendBtn.setVisible(false);
					passBtn.setVisible(false);
					if (allMyCards.size() == 0) {
						client.sendMsg("restart");
					} else {
						client.changePlayer();
					}
				}
			}
		});
		add(sendBtn);
		add(passBtn);
		sendBtn.setVisible(false);
		passBtn.setVisible(false);

		createGameBtn.setBounds(520, 20, 60, 20);
		createGameBtn.setMargin(insets);
		add(createGameBtn);
		createGameBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
				Transferable t = new StringSelection("start://CardGame:"
						+ ipLabel.getText());
				cb.setContents(t, null);

				new Thread(new Runnable() {
					@Override
					public void run() {
						new Server(MainFrame.this);
					}
				}).start();

				createGameBtn.setVisible(false);
				joinGameBtn.setVisible(false);
				ipLabel.setVisible(false);
				ipInputTxt.setVisible(false);
			}
		});

		joinGameBtn.setBounds(520, 50, 60, 20);
		joinGameBtn.setMargin(insets);
		add(joinGameBtn);
		joinGameBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						Client client = new Client(MainFrame.this);
						if (client.connect(ipInputTxt.getText())) {
							init(client);
						} else {
							createGameBtn.setVisible(true);
							joinGameBtn.setVisible(true);
							ipLabel.setVisible(true);
							ipInputTxt.setVisible(true);
						}
					}
				}).start();

				createGameBtn.setVisible(false);
				joinGameBtn.setVisible(false);
				ipLabel.setVisible(false);
				ipInputTxt.setVisible(false);
			}
		});

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (client != null) {
					client.shutdownAndAwaitTermination();
				}
			}
		});

		setVisible(true);
		setSize(600, 600);
	}

	public void init(Playable playable) {
		client = playable;

		for (int i = 0; i < 3; i++) {
			if (hiddenCardsLbl[i] != null) {
				remove(hiddenCardsLbl[i]);
			}
		}
		cardPaneMid.removeAll();

		initialCards(client.getInitialCards());
		showCards(cardPaneBot, Utils.retriveLabelListFromMap(allMyCards));
		showCards(cardPaneLeft, 17);
		showCards(cardPaneRight, 17);

		repaint();
	}

	private ImageIcon getIcon(Card card) {
		ImageIcon icon = null;
		if (card.getDigit() == 16) {
			icon = new ImageIcon(Utils.getResourceURL("/imgs/BlackJoker.jpg"));
		} else if (card.getDigit() == 17) {
			icon = new ImageIcon(Utils.getResourceURL("/imgs/RedJoker.jpg"));
		} else {
			icon = new ImageIcon(Utils.getResourceURL("/imgs/" + card.getSuit()
					+ "_" + card.getDigit() + ".jpg"));
		}
		return icon;
	}

	private void initialCards(List<Card> list) {
		allMyCards.clear();
		for (int i = 0; i < list.size(); i++) {
			JLabel lbl = new JLabel(getIcon(list.get(i)));
			lbl.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					JLabel lbl = ((JLabel) e.getSource());
					changeState(lbl);
				}

				@Override
				public void mouseEntered(MouseEvent e) {
					JLabel lbl = ((JLabel) e.getSource());

					if (e.getModifiersEx() == InputEvent.BUTTON1_DOWN_MASK) {
						changeState(lbl);
					}
				}
				
				private void changeState(JLabel lbl){
					if (selectedCards.containsKey(lbl)) {
						selectedCards.remove(lbl);
						lbl.setLocation((int) lbl.getLocation().getX(),
								(int) lbl.getLocation().getY() + 15);
					} else {
						selectedCards.put(lbl, allMyCards.get(lbl));
						lbl.setLocation((int) lbl.getLocation().getX(),
								(int) lbl.getLocation().getY() - 15);
					}
				}
			});
			allMyCards.put(lbl, list.get(i));
		}
	}

	private void showCards(JLayeredPane pane, int num) {
		pane.removeAll();

		ImageIcon icon = new ImageIcon(Utils.getResourceURL("/imgs/back.jpg"));
		for (int i = 0; i < num; i++) {
			JLabel lbl = new JLabel(icon);
			lbl.setBounds(0, i * 10, 66, 88);
			pane.add(lbl, new Integer(i));
		}
		repaint();
	}

	private void showCards(JLayeredPane pane, List<JLabel> lbls) {
		pane.removeAll();

		int width = 66 + (15 * lbls.size() - 1);
		int offset = (pane.getWidth() - width) / 2;

		for (int i = 0; i < lbls.size(); i++) {
			lbls.get(i).setBounds(i * 15 + offset, 20, 66, 88);
			pane.add(lbls.get(i), new Integer(i));
		}

		repaint();
	}

	public void addHiddenCards(String str) {
		int id = Integer.parseInt(str.substring(0, 1));
		str = str.substring(1);

		List<Card> cards = Utils.retriveCardListFromString(str);

		for (int i = 0; i < 3; i++) {
			hiddenCardsLbl[i] = new JLabel(getIcon(client.getHiddenCards().get(
					i)));
			hiddenCardsLbl[i].setBounds(187 + 80 * i, 20, 66, 88);
			add(hiddenCardsLbl[i]);
		}

		drawArrow(id);

		selectedCards.clear();
		showCards(cardPaneBot, Utils.retriveLabelListFromMap(allMyCards));

		if (id == client.getLeftId()) {
			showCards(cardPaneLeft, cards.size());
			diZhuLbl.setLocation(50, 60);
			add(diZhuLbl);
			repaint();
			return;
		}

		if (id == client.getRightId()) {
			showCards(cardPaneRight, cards.size());
			diZhuLbl.setLocation(520, 60);
			add(diZhuLbl);
			repaint();
			return;
		}

		initialCards(cards);
		showCards(cardPaneBot, Utils.retriveLabelListFromMap(allMyCards));
		diZhuLbl.setLocation(150, 360);
		add(diZhuLbl);
		repaint();
	}

	public void updatePane(String str) {
		int id = Integer.parseInt(str.substring(0, 1));
		str = str.substring(1);

		List<JLabel> lbls = new ArrayList<JLabel>();
		List<Card> cards = Utils.retriveCardListFromString(str);

		if (cards.size() == 0) {
			return;
		}

		client.setLastPlay(id);
		lastPlayedCards = PatternFactory.parsePattern(cards);

		for (Card card : cards) {
			lbls.add(new JLabel(getIcon(card)));
		}

		if (id == client.getLeftId()) {
			showCards(cardPaneLeft,
					cardPaneLeft.getComponentCount() - cards.size());

			int width = 66 + (15 * lbls.size() - 1);
			int offset = (cardPaneMid.getWidth() - width) / 2;
			cardPaneMid.setLocation(150 - offset, 200);
		} else if (id == client.getRightId()) {
			showCards(cardPaneRight,
					cardPaneRight.getComponentCount() - cards.size());

			int width = 66 + (15 * lbls.size() - 1);
			int offset = (cardPaneMid.getWidth() - width) / 2;
			cardPaneMid.setLocation(220 + offset, 200);
		} else {
			cardPaneMid.setLocation(180, 200 + 80);
		}

		showCards(cardPaneMid, lbls);
	}

	public void setPlayEnabled(boolean enabled) {
		if (enabled) {
			sendBtn.setVisible(true);
			if (client.getLastPlay() != client.getId()) {
				passBtn.setVisible(true);
			}
		} else {
			sendBtn.setVisible(false);
			passBtn.setVisible(false);
		}
	}

	public void drawArrow(int id) {
		if (id == client.getLeftId()) {
			arrowLbl.setIcon(new ImageIcon(Utils
					.getResourceURL("/imgs/left_arrow.png")));
			arrowLbl.setLocation(150, 280);
			return;
		}

		if (id == client.getRightId()) {
			arrowLbl.setIcon(new ImageIcon(Utils
					.getResourceURL("/imgs/right_arrow.png")));
			arrowLbl.setLocation(430, 280);
			return;
		}
		arrowLbl.setIcon(new ImageIcon(Utils
				.getResourceURL("/imgs/down_arrow.png")));
		arrowLbl.setLocation(280, 360);
	}
	
	public static void main(String[] args) {
		new MainFrame();
	}
}
