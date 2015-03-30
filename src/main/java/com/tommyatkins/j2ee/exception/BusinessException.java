package com.tommyatkins.j2ee.exception;

public class BusinessException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -570746335673328075L;

	private String detailMessage;

	public BusinessException(String message) {
		this.detailMessage = message;
	}

	@Override
	public String getMessage() {
		return this.detailMessage;
	}

}
