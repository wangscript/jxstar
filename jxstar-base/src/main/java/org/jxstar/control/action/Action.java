/*
 * Action.java 2009-5-28
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.control.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jxstar.util.log.Log;

/**
 * 控制器的基类，请求处理。
 *
 * @author TonyTan
 * @version 1.0, 2009-5-28
 */
public abstract class Action {
	protected static Log _log = Log.getInstance();
	
	/**
	 * 执行请求处理的接口
	 * @param request
	 * @param response
	 * @return
	 */
	public abstract void execute(HttpServletRequest request, 
			HttpServletResponse response);
}
