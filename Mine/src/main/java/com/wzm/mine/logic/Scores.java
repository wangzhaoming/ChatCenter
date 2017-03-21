package com.wzm.mine.logic;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.wzm.mine.frame.MyTimer;

public class Scores {
	private long[] times;
	private byte[] md5;
	private String path;
	private static final char SEPARATOR = '\n';

	public Scores() {
		path = System.getProperty("user.dir") + File.separator + "inf"
				+ File.separator + "HighScore";

		File file = new File(path);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		times = new long[9];

		load();
	}

	private byte[] getMd5(byte[] data) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("md5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		md.update(data);
		return md.digest();
	}

	private boolean validate() {
		if (Arrays.equals(md5, getMd5(getTimeData()))) {
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @param time
	 *            新的时间
	 * @param level
	 *            0 normal,3 nightmare,6 hell
	 */
	public void setScore(long time, int level) {
		if (time < times[level]) {
			times[level] = time;
		} else if (time < times[level + 1]) {
			times[level + 1] = time;
		} else if (time < times[level + 2]) {
			times[level + 2] = time;
		}
		dump();
	}

	private void dump() {
		BufferedOutputStream os = null;
		try {
			os = new BufferedOutputStream(new FileOutputStream(new File(path)));
			os.write(getMd5(getTimeData()));
			os.write('\n');
			os.write(getTimeData());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (os != null) {
					os.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private byte[] getTimeData() {
		StringBuffer buffer = new StringBuffer();
		for (long time : times) {
			buffer.append(time);
			buffer.append(SEPARATOR);
		}
		buffer.deleteCharAt(buffer.length() - 1);
		return buffer.toString().getBytes();
	}

	private String readLine(InputStream is) {
		int b = -1;
		StringBuffer buffer = new StringBuffer();
		try {
			b = is.read();
			while (b != -1 && b != SEPARATOR) {
				buffer.append((char) b);
				b = is.read();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return buffer.toString();
	}

	private byte[] readMD5(InputStream is) {
		List<Byte> list = new ArrayList<Byte>();
		int b = -1;
		try {
			b = is.read();
			while (b != -1 && b != SEPARATOR) {
				list.add((byte) b);
				b = is.read();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		byte[] bytes = new byte[list.size()];
		for (int i = 0; i < list.size(); i++) {
			bytes[i] = list.get(i);
		}

		return bytes;
	}

	private void load() {
		BufferedInputStream is = null;
		try {
			is = new BufferedInputStream(new FileInputStream(new File(path)));

			md5 = readMD5(is);

			if (md5.length > 0) {
				for (int i = 0; i < times.length; i++) {
					String str = readLine(is);
					if (!"".equals(str)) {
						times[i] = Long.parseLong(str);
					} else {
						times[i] = 0;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (md5.length < 1 || !validate()) {
			for (int i = 0; i < 9; i++) {
				times[i] = Long.MAX_VALUE;
			}
			dump();
		}
	}

	public String getHighScores() {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < 9; i++) {
			switch (i) {
			case 0:
				buffer.append("Normal:" + SEPARATOR);
				break;
			case 3:
				buffer.append("Nightmare:" + SEPARATOR);
				break;
			case 6:
				buffer.append("hell:" + SEPARATOR);
				break;
			}

			buffer.append("  ").append(i % 3 + 1).append(":");
			buffer.append(times[i] == Long.MAX_VALUE ? "None" : MyTimer
					.parseTime(times[i]));
			buffer.append(SEPARATOR);
		}

		buffer.deleteCharAt(buffer.length() - 1);
		return buffer.toString();
	}
}
