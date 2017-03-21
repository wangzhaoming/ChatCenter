package com.wzm.db;

import java.io.UnsupportedEncodingException;

import com.sleepycat.je.DatabaseEntry;

public class DataEntryUtil {
	public static DatabaseEntry stringToEntry(String data) {
		try {
			return new DatabaseEntry(data.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String entryToString(DatabaseEntry entry) {
		try {
			return new String(entry.getData(), "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
}
