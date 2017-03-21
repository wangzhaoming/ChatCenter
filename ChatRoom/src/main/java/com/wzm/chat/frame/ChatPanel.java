package com.wzm.chat.frame;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JEditorPane;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import com.wzm.card.frames.MainFrame;
import com.wzm.chat.net.ChatClient;
import com.wzm.err.LoginFailedException;

public class ChatPanel extends JPanel {
	private ChatEditorPane txtArea;
	private JTextField txtField;
	private JScrollPane scrollPane;
	private ChatClient client;

	public ChatPanel(String loginString, JList users)
			throws LoginFailedException {
		txtArea = new ChatEditorPane();
		txtField = new JTextField();
		this.setLayout(null);

		txtArea.setEditable(false);
		// txtArea.setLineWrap(true);
		// txtArea.setWrapStyleWord(true);
		// txtArea.setAutoscrolls(true);
		scrollPane = new JScrollPane(txtArea);
		
		this.add(scrollPane);
		this.add(txtField);

		client = new ChatClient(txtArea, users, loginString);

		if (!client.connect(ChatClient.IP)) {
			throw new LoginFailedException("login failed");
		}

		addListeners();

		txtArea.setSize(250, 250);
		scrollPane.setBounds(0, 0, 250, 250);
		txtField.setBounds(0, 250, 200, 20);
	}

	private void addListeners() {
		scrollPane.getViewport().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JScrollBar bar=scrollPane.getVerticalScrollBar();
				bar.setValue(bar.getMaximum());
			}
		});
		
		txtField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (client != null) {
					client.sendMessage(ChatClient.CLIENT_MASSAGE_HEADER + ":"
							+ txtField.getText());
				}
				txtField.setText("");
			}
		});

		txtArea.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					String str = e.getDescription();
					if (str.startsWith("start")) {
						int index = str.lastIndexOf(":");
						String url = str.substring(index + 1);
						String game = str.substring(8, index);

						if (game.equals("CardGame")) {
							if (ChatFrame.cardGameFrame == null) {
								ChatFrame.cardGameFrame = new MainFrame(url);
								ChatFrame.cardGameFrame
										.addWindowListener(new WindowAdapter() {
											@Override
											public void windowClosing(
													WindowEvent e) {
												ChatFrame.cardGameFrame = null;
											}
										});
							} else {
								ChatFrame.cardGameFrame
										.setExtendedState(Frame.NORMAL);
								ChatFrame.cardGameFrame.connect(url);
							}

						}
					}
				}
			}
		});
	}

	public void closeClient() {
		client.sendMessage(ChatClient.CLIENT_LOGOUT_COMMAND + ":" + "logout");
	}

	public ChatClient getClient() {
		return client;
	}

	public JEditorPane getTxtArea() {
		return txtArea;
	}
}
