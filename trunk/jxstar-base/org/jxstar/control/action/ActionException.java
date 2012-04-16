/*
 * ActionException.java 2010-11-16
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.control.action;

import java.text.MessageFormat;

/**
 * Action执行异常。
 *
 * @author TonyTan
 * @version 1.0, 2010-11-16
 */
public class ActionException extends Exception {
	private static final long serialVersionUID = 7137119381351052540L;

	/**
	 * @param message
	 */
	public ActionException(String message) {
		super(message);
	}
	
	public ActionException(String message, Object ... params) {
		super(MessageFormat.format(message, params));
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ActionException(String message, Throwable cause) {
		super(message, cause);
	}
}
