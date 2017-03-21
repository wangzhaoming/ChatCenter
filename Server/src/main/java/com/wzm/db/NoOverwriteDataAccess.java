package com.wzm.db;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class NoOverwriteDataAccess implements DataAccess {
	private Database db;

	public NoOverwriteDataAccess(Database db) {
		this.db = db;
	}

	public boolean put(String key, String data) throws DatabaseException {
		if (db.putNoOverwrite(null, DataEntryUtil.stringToEntry(key),
				DataEntryUtil.stringToEntry(data)) == OperationStatus.SUCCESS) {
			return true;
		}
		return false;
	}

	public String get(String key) throws DatabaseException {
		DatabaseEntry dataEntry = new DatabaseEntry();
		if (db.get(null, DataEntryUtil.stringToEntry(key), dataEntry,
				LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			return DataEntryUtil.entryToString(dataEntry);
		}
		return null;
	}
}
