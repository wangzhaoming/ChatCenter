package com.wzm.db;

import java.io.File;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

public class MyDbEnv {

	private Environment myEnv;

	private Database userinfoDb;

	// Our constructor does nothing
	public MyDbEnv() {
	}

	// The setup() method opens all our databases and the environment
	// for us.
	public void setup(File envHome) throws DatabaseException {

		EnvironmentConfig myEnvConfig = new EnvironmentConfig();
		DatabaseConfig myDbConfig = new DatabaseConfig();

		// If the environment is opened for write, then we want to be
		// able to create the environment and databases if
		// they do not exist.
		myEnvConfig.setAllowCreate(true);
		myDbConfig.setAllowCreate(true);

		// Allow transactions if we are writing to the database
		myEnvConfig.setTransactional(true);
		myDbConfig.setTransactional(true);

		// Open the environment
		myEnv = new Environment(envHome, myEnvConfig);

		userinfoDb = myEnv.openDatabase(null, "userinfoDb", myDbConfig);
	}

	// getter methods

	// Needed for things like beginning transactions
	public Environment getEnv() {
		return myEnv;
	}

	public Database getDb() {
		return userinfoDb;
	}

	// Close the environment
	public void close() {
		if (myEnv != null) {
			try {
				// Close the secondary before closing the primaries
				userinfoDb.close();

				// Finally, close the environment.
				myEnv.close();
			} catch (DatabaseException dbe) {
				System.err.println("Error closing MyDbEnv: " + dbe.toString());
			}
		}
	}
}
