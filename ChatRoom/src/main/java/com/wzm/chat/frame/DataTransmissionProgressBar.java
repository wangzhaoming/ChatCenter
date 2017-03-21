package com.wzm.chat.frame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;

import com.wzm.chat.net.TransmissionInfo;

public class DataTransmissionProgressBar extends JPanel implements Runnable{
	private JProgressBar progressBar;
	private TransmissionInfo info;
	private Timer timer;
	private JFrame parent;
	
	public DataTransmissionProgressBar(JFrame frame,TransmissionInfo minfo) {
		this.info=minfo;
		this.parent=frame;
		progressBar=new JProgressBar(0,100);
		progressBar.setBounds(0,0,150,10);
		setLayout(null);
		add(progressBar);
		setBounds(135, 345, 150, 10);
		parent.add(this);
		
		timer=new Timer(500, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				progressBar.setValue((int) (info.getBytesTransferred()*1.0/info.getFileLengh()*100));
				if (progressBar.getValue()==progressBar.getMaximum()) {
					parent.remove(DataTransmissionProgressBar.this);
					parent.repaint();
					setParentEnabled(true);
					timer.stop();
				}
			}
		});
	}

	private void setParentEnabled(boolean enable){
		PrivateMessageFrame frame=(PrivateMessageFrame) parent;
		frame.getTxtField().setEnabled(enable);
		
		if (!enable) {
			frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		}
		else {
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		}
	}
	
	
	@Override
	public void run() {
		setParentEnabled(false);
		timer.start();
	}
}
