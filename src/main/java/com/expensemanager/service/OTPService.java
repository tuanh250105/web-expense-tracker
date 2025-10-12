package com.expensemanager.service;

public class OTPService {
	public static String generateSixDigits() {
		int code = 100000 + (int)(Math.random() * 900000);
		return String.valueOf(code);
	}
}