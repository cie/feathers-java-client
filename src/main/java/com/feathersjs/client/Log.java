package com.feathersjs.client;

public class Log {

	public static void e(String header, String message, Throwable t) {
		System.err.println(header + message);
		t.printStackTrace();
	}

	public static void d(String header, String message) {
		System.err.println(header + message);
	}

	public static void e(String header, String message) {
		System.err.println(header + message);
	}

}
