package com.wzm.err;

public class LoginFailedException extends Exception {
	public LoginFailedException() {
		super();
	}

	public LoginFailedException(String message) {
		super(message);
	}
}
