package com.wzm.db;

import com.sleepycat.je.DatabaseException;

public interface DataAccess {
	public boolean put(String key, String data) throws DatabaseException;

	public String get(String key) throws DatabaseException;

	// public boolean delete(String key);
}
