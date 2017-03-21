package com.wzm.service;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseException;
import com.wzm.db.DataAccess;
import com.wzm.db.DataAccessFactory;

public class UserService {
	private DataAccess access;

	public UserService(Database db) {
		access = DataAccessFactory.getNoOverwriteDataAccess(db);
	}

	public boolean validateUser(String name, String pass) {
		String retPass = null;
		try {
			retPass = access.get(name);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}

		if (retPass != null) {
			if (pass.equals(retPass)) {
				return true;
			}
		}

		return false;
	}
}
