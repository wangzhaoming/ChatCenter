package com.wzm.chat.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JFrame;

import com.wzm.chat.frame.DataTransmissionProgressBar;

public class FileReceiver implements TransmissionInfo{
	private File file;
	private InputStream in;
	private static final int bufferSize = 1024;
	private byte[] buffer = new byte[bufferSize];
	private long length;
	private long byteReceived=0;
	private JFrame parent;

	public FileReceiver(JFrame frame,File file, InputStream in,long length) {
		this.parent=frame;
		this.file = file;
		this.in = in;
		this.length=length;
	}

	public FileReceiver(JFrame frame,String path, InputStream in,long length) {
		this.parent=frame;
		this.file = new File(path);
		this.in = in;
		this.length=length;
	}

	public void receive() {
		new Thread(new DataTransmissionProgressBar(parent,this)).start();
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
//		Socket socket=null;
//		
//		socket=new Socket();
		
		try {
			bis = new BufferedInputStream(in);
			bos = new BufferedOutputStream(new FileOutputStream(file));
			int num = bis.read(buffer);
			while (num != -1) {
				bos.write(buffer, 0, num);
				byteReceived+=num;
				if (byteReceived==length) {
					break;
				}
				num = bis.read(buffer);
			}
			bos.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public long getFileLengh() {
		return length;
	}

	@Override
	public long getBytesTransferred() {
		return byteReceived;
	}
}
