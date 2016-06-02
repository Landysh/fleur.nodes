package io.landysh.inflor.java.core;

public class UnexpectecCaseException extends Exception{
	/**
	 * An exception used in cases where Aaron didn't expect a problem but there was one.
	 */
	public UnexpectecCaseException(String message) {
		this.printStackTrace();
		System.out.print(message);
	}

	private static final long serialVersionUID = 1L;

}
