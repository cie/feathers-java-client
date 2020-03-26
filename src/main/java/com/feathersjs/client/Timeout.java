package com.feathersjs.client;

public class Timeout extends FeathersException {
	private static final long serialVersionUID = 1L;

	public Timeout(String message) {
		super(message);
	}

}
