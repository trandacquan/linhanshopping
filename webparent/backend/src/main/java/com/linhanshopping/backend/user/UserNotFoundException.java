package com.linhanshopping.backend.user;

public class UserNotFoundException extends Exception {// Đây là 1 exception do mình tự tạo

	private static final long serialVersionUID = 1L;

	public UserNotFoundException(String message) {
		super(message);
	}
}
