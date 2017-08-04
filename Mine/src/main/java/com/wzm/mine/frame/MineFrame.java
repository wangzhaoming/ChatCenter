package com.wzm.mine.frame;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import com.wzm.mine.logic.Mines;
import com.wzm.mine.logic.Scores;

public class MineFrame extends JFrame {
	private int[][] mines;
	public static final int SIZE = 25;
	private static final int OFFSET_X = 12;
	private static final int OFFSET_Y = 75;
	
	private int column;
	private int row;
	private int mineNum;
	private MyTimer timer;
	private Scores scores;

	private static final int NORMAL_COLUMN = 8;
	private static final int NORMAL_ROW = 8;
	private static final int NORMAL_MINE_NUMBER = 10;
	private static final int NIGHTMARE_COLUMN = 16;
	private static final int NIGHTMARE_ROW = 16;
	private static final int NIGHTMARE_MINE_NUMBER = 40;
	private static final int HELL_COLUMN = 30;
	private static final int HELL_ROW = 16;
	private static final int HELL_MINE_NUMBER = 99;

	private JButton[][] btns;
	private JMenuBar mb;

	private void start() {
		setSize(SIZE * column + OFFSET_X, SIZE * row + OFFSET_Y);
		mines = (new Mines(column, row, mineNum)).getMines();
		btns = new JButton[column][row];
		for (int i = 0; i < column; i++) {
			for (int j = 0; j < row; j++) {
				btns[i][j] = new JButton();
				btns[i][j].setBounds(i * SIZE, j * SIZE, SIZE, SIZE);
				btns[i][j].addMouseListener(new EventHandler());
				btns[i][j].setFont(new Font("", Font.BOLD, 12));
				btns[i][j].setMargin(new Insets(0, 0, 0, 0));
				add(btns[i][j]);
			}
		}

		timer.setBounds(0,SIZE * row, 60, 20);
		timer.stop();
		timer.reset();
		repaint();
	}

	private void removeButtons() {
		if (btns != null) {
			for (int i = 0; i < column; i++) {
				for (int j = 0; j < row; j++) {
					MineFrame.this.remove(btns[i][j]);
				}
			}
		}
	}

	private void init() {
		column = NORMAL_COLUMN;
		row = NORMAL_ROW;
		mineNum = NORMAL_MINE_NUMBER;
		setLayout(null);

		timer=new MyTimer();
		add(timer);
		
		scores=new Scores();
		
		start();

		JMenu gameMenu = new JMenu("Game");
		JMenuItem restartItem = new JMenuItem("Restart");
		JMenuItem highscoreItem = new JMenuItem("HighScore");
		gameMenu.add(restartItem);
		gameMenu.add(highscoreItem);
		
		restartItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeButtons();
				start();
			}
		});
		
		highscoreItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				JOptionPane.showMessageDialog(MineFrame.this, scores.getHighScores());
			}
		});

		JMenu difficultyMenu = new JMenu("Level");
		JMenuItem normal = new JMenuItem("Normal");
		JMenuItem nightmare = new JMenuItem("Nightmare");
		JMenuItem hell = new JMenuItem("Hell");
		difficultyMenu.add(normal);
		difficultyMenu.add(nightmare);
		difficultyMenu.add(hell);

		normal.addActionListener(new LevelSeletedHandler());
		nightmare.addActionListener(new LevelSeletedHandler());
		hell.addActionListener(new LevelSeletedHandler());

		mb = new JMenuBar();
		mb.add(gameMenu);
		mb.add(difficultyMenu);
		setJMenuBar(mb);
		setResizable(false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);
	}

	private void showSpace(int x, int y) {
		if (x >= 0 && x < column && y >= 0 && y < row && btns[x][y].isEnabled()) {
			int num = mines[x][y];
			if (num == 0) {
				btns[x][y].setEnabled(false);
				btns[x][y].setText("");

				for (int[] locate : getSurround(x, y)) {
					showSpace(locate[0], locate[1]);
				}
			} else if (num == -1) {
				for (int i = 0; i < column; i++) {
					for (int j = 0; j < row; j++) {
						if (mines[i][j] == -1) {
							btns[i][j].setText("*");
							btns[i][j].setEnabled(false);
						}
					}
				}
				btns[x][y].setEnabled(false);
				btns[x][y].setBackground(Color.red);
				removeListeners();
				if (timer.isStarted()) {
					timer.stop();
				}
				JOptionPane.showMessageDialog(MineFrame.this, "GG");
			} else {
				btns[x][y].setEnabled(false);
				btns[x][y].setText(String.valueOf(num));
			}
			removeListener(btns[x][y]);
			btns[x][y].addMouseListener(new LeftRightClickHandler());
		}
	}

	class EventHandler extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			JButton btn = (JButton) e.getSource();
			if (btn.isEnabled()) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					if (!timer.isStarted()) {
						timer.start();
					}
					if (!btn.getText().equals("@")) {
						int x = btn.getX() / SIZE;
						int y = btn.getY() / SIZE;
						showSpace(x, y);
					}
				} else if (e.getButton() == MouseEvent.BUTTON3) {
					if (!btn.getText().equals("@")) {
						btn.setText("@");
					} else {
						btn.setText("");
					}
				}
				judge();
			}
		}
	}

	class LeftRightClickHandler extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			JButton btn = (JButton) e.getSource();
			if (!btn.isEnabled()) {
				if (e.getModifiersEx() == InputEvent.BUTTON1_DOWN_MASK
						|| e.getModifiersEx() == InputEvent.BUTTON3_DOWN_MASK) {
					int x = btn.getX() / SIZE;
					int y = btn.getY() / SIZE;
					int num = 0;
					for (int[] locate : getSurround(x, y)) {
						if (locate[0] >= 0 && locate[0] < column
								&& locate[1] >= 0 && locate[1] < row
								&& btns[locate[0]][locate[1]].getText() == "@") {
							num++;
						}
					}
					if (String.valueOf(num).equals(btn.getText())) {
						for (int[] locate : getSurround(x, y)) {
							if (locate[0] >= 0
									&& locate[0] < column
									&& locate[1] >= 0
									&& locate[1] < row
									&& btns[locate[0]][locate[1]].isEnabled()
									&& btns[locate[0]][locate[1]].getText() != "@") {
								showSpace(locate[0], locate[1]);
							}
						}
					}
				}
				judge();
			}
		}
	}

	class LevelSeletedHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			removeButtons();
			if (e.getActionCommand() == "Normal") {
				column = NORMAL_COLUMN;
				row = NORMAL_ROW;
				mineNum = NORMAL_MINE_NUMBER;
			} else if (e.getActionCommand() == "Nightmare") {
				column = NIGHTMARE_COLUMN;
				row = NIGHTMARE_ROW;
				mineNum = NIGHTMARE_MINE_NUMBER;
			} else if (e.getActionCommand() == "Hell") {
				column = HELL_COLUMN;
				row = HELL_ROW;
				mineNum = HELL_MINE_NUMBER;
			}
			start();
		}
	}

	private List<int[]> getSurround(int x, int y) {
		List<int[]> list = new ArrayList<int[]>();

		list.add(new int[] { x - 1, y - 1 });
		list.add(new int[] { x - 1, y });
		list.add(new int[] { x - 1, y + 1 });
		list.add(new int[] { x, y - 1 });
		list.add(new int[] { x, y + 1 });
		list.add(new int[] { x + 1, y - 1 });
		list.add(new int[] { x + 1, y });
		list.add(new int[] { x + 1, y + 1 });

		return list;
	}
	
	
	private void removeListeners(){
		for (int i = 0; i < column; i++) {
			for (int j = 0; j < row; j++) {
				removeListener(btns[i][j]);
			}
		}
	}
	
	private void removeListener(JButton button){
		MouseListener[] listeners=button.getMouseListeners();
		for(MouseListener listener:listeners){
			button.removeMouseListener(listener);
		}
	}

	private void judge() {
		int count = 0;
		int minesLeft=0;
		for (int i = 0; i < column; i++) {
			for (int j = 0; j < row; j++) {
				if (btns[i][j].isEnabled()) {
					count++;
					if (mines[i][j]==-1) {
						minesLeft++;
					}
				}
			}
		}
		if (count == minesLeft) {
			if (timer.isStarted()) {
				timer.stop();
			}
			removeListeners();
			scores.setScore(timer.getTime(), mineNum==NORMAL_MINE_NUMBER?0:(mineNum==NIGHTMARE_MINE_NUMBER?3:6));
			JOptionPane.showMessageDialog(null, "You Win!");
		}
	}

	public MineFrame() {
		init();
	}

	public int getColumn()
	{
		return column;
	}

	public int getRow()
	{
		return row;
	}
	
	public static void main(String[] args) {
		new MineFrame();
	}
}
