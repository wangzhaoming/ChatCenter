package com.wzm.chat.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JFrame;

import com.wzm.chat.frame.DataTransmissionProgressBar;

public class FileSender implements TransmissionInfo {
	private File file;
	private OutputStream out;
	private static final int bufferSize = 1024;
	private byte[] buffer = new byte[bufferSize];
	private long bytesSended=0;
	private JFrame parent;

	public FileSender(JFrame frame,File file, OutputStream out) {
		this.file = file;
		this.out = out;
		this.parent=frame;
	}

	public FileSender(JFrame frame,String path, OutputStream out) {
		this.file = new File(path);
		this.out = out;
		this.parent=frame;
	}

	public void send() {
		new Thread(new DataTransmissionProgressBar(parent, this)).start();
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;

		// ServerSocket server = null;
		// Socket socket = null;
		// try {
		// server = new ServerSocket(10610);
		// socket = server.accept();
		try {
			bis = new BufferedInputStream(new FileInputStream(file));
			bos = new BufferedOutputStream(out);

			int num = bis.read(buffer);
			while (num != -1) {
				bos.write(buffer, 0, num);
				bytesSended+=num;
				num = bis.read(buffer);
			}
			bos.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			// if (bos != null) {
			// try {
			// bos.close();
			// } catch (IOException e) {
			// e.printStackTrace();
			// }
			// }
		}

		// } catch (IOException e1) {
		// e1.printStackTrace();
		// } finally {
		// try {
		// if (socket != null) {
		// socket.close();
		// }
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// try {
		// if (server != null) {
		// server.close();
		// }
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// }
	}

	@Override
	public long getFileLengh() {
		return file.length();
	}

	@Override
	public long getBytesTransferred() {
		return bytesSended;
	}
}
