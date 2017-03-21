package com.wzm.db;

import com.sleepycat.je.Database;

public class DataAccessFactory {
	public static DataAccess getNoOverwriteDataAccess(Database db) {
		return new NoOverwriteDataAccess(db);
	}
}
