/*
 * AjaxController.java 2009-5-28
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.control;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.jxstar.control.action.Action;
import org.jxstar.util.factory.SystemFactory;
import org.jxstar.util.resource.JsMessage;

/**
 * 所有前端请求的控制器。
 *
 * @author TonyTan
 * @version 1.0, 2009-5-28
 */
public class AjaxController extends HttpServlet {
	private static final long serialVersionUID = 3080747755569542886L;
	
	//private static final Log _log = Log.getInstance();	
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException {
		processRequest(request, response);
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException {
		processRequest(request, response);
	}	
	
	/**
	 * 处理HTTP请求。
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException  {
		//取actionName，取URL中最后一个/与.do之间的字符串
		String requestUrl = request.getRequestURI();
		String actionName = parseActionName(requestUrl);
		
		//创建action的实例
		Action action = (Action) SystemFactory.createSystemObject(actionName);
		if (action == null) {
			response.sendError(401, JsMessage.getValue("controlservlet.actionisnull", actionName));
			return;
		}

		//执行action
		action.execute(request, response);
	}
	
	/**
	 * 根据请求路径返回action名称
	 * 
	 * @param reqUrl - 请求路径
	 * @return String
	 */
	private String parseActionName(String requestUrl) {
		if (requestUrl == null || requestUrl.length() == 0) {
			return "";
		}
		
		requestUrl = requestUrl.replaceAll("\\\\", "/");
		int last = requestUrl.lastIndexOf("/");
		if (last >= 0) {
			requestUrl = requestUrl.substring(last + 1);
		}

		return requestUrl.split("\\.")[0];
	}
}
