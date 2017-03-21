package com.wzm.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConfigUtil {
	public static String getIP() {
		BufferedReader reader = null;
		String ip = null;
		String path = null;
		path = System.getProperty("user.dir") +File.separator+ "inf" + File.separator
				+ "config";
		try {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(path)));
			ip = reader.readLine();
			if (ip == null) {
				ip = "127.0.0.1";
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return ip;
	}
}
