package com.expensemanager.util;

public class EnvUtil {
	public static String get(String key, String def) {
		String v = System.getenv(key);
		return v != null ? v : def;
	}
}