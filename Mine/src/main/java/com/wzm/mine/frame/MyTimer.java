package com.wzm.mine.frame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.Timer;

public class MyTimer extends JLabel {
	private long time;
	private Timer timer;
	private boolean isStarted;

	public MyTimer() {
		isStarted = false;
		time = -28800000;
		timer = new Timer(1000, new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				time+=1000;
				MyTimer.this.setText(parseTime(time));
			}
		});
		setText("00:00:00");
	}
	
	public static String parseTime(long time){
		return new SimpleDateFormat("HH:mm:ss").format(new Date(time));
	}

	public void start() {
		timer.start();
		isStarted = true;
	}

	public void stop() {
		timer.stop();
		isStarted = false;
	}

	public void reset() {
		setText("00:00:00");
		time = -28800000;
	}

	public boolean isStarted() {
		return isStarted;
	}
	
	public long getTime()
	{
		return time;
	}
}
