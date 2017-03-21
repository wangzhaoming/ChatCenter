package com.wzm.db.test;

import java.io.File;

import com.sleepycat.je.DatabaseException;
import com.wzm.db.DataAccess;
import com.wzm.db.DataAccessFactory;
import com.wzm.db.MyDbEnv;

public class TestDb {
	public static void main(String[] args) throws DatabaseException {
		MyDbEnv env = new MyDbEnv();
		env.setup(new File("Database"));
		DataAccess access = DataAccessFactory.getNoOverwriteDataAccess(env
				.getDb());

		access.put("zl", "123456");

		// System.out.println(access.get("test"));
	}
}
