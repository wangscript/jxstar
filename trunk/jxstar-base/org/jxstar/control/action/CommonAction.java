/*
 * CommonAction.java 2009-5-28
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.control.action;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jxstar.service.control.ServiceController;
import org.jxstar.util.MapUtil;
import org.jxstar.util.factory.SystemFactory;
import org.jxstar.util.resource.JsMessage;
import org.jxstar.util.resource.JsParam;

/**
 * 公共事件请求的命令控制器.
 *
 * @author TonyTan
 * @version 1.0, 2008-4-13
 */
public class CommonAction extends Action {
	
	public void execute(HttpServletRequest request,
			HttpServletResponse response) {
		ResponseContext responseContext = null;
		try {//捕获所有异常，提高服务的稳定性
			responseContext = processAction(request, response);
		} catch (Exception e) {
			_log.showError(e);
			return;
		}
		

		if (responseContext != null) {
			//响应信息格式
			String dataType = request.getParameter("dataType");
			//响应信息
			String reponseText = "";
			if (dataType != null && dataType.equals("xml")) {
				reponseText = responseContext.reponseXml();
				response.setContentType("text/xml");
			} else {
				reponseText = responseContext.reponseText();
				response.setContentType("text/html");
			}
			//System.out.println("reponseText=\n" + reponseText);
			
			//反馈响应信息
			try {
				response.getWriter().write(reponseText);
			} catch (Exception e) {
				_log.showError(e);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private ResponseContext processAction(HttpServletRequest request,
			HttpServletResponse response) {
		//创建返回对象
		ResponseContext responseContext = new ResponseContext(false);
		//创建RequestContext
		RequestContext requestContext;
		try {
			requestContext = ActionHelper.getRequestContext(request);
		} catch (ActionException e) {
			_log.showError(e);
			responseContext.setMessage(e.getMessage());
			return responseContext;
		}
		
		//当前功能ID
		String funId = requestContext.getFunID();
		//当前事件代码
		String eventCode = requestContext.getEventCode();
		_log.showDebug("command action funid = " + funId + " eventcode = " + eventCode);
		if (funId == null || funId.length() == 0) {
			responseContext.setMessage(JsMessage.getValue("commonaction.funidnull"));
			return responseContext;
		}
		if (eventCode == null || eventCode.length() == 0) {
			responseContext.setMessage(JsMessage.getValue("commonaction.eventcodenull"));
			return responseContext;
		}
		
		//如果是登陆事件,则把session中的用户信息删除
		if (funId.equals("login") && eventCode.equals("login")) {
			request.getSession().removeAttribute(JsParam.CURRUSER);
		} else {
			Map<String,String> mpUser = (Map<String,String>) request.getSession().
								getAttribute(JsParam.CURRUSER);
			if (mpUser == null || mpUser.isEmpty()) {
				//当会话失效时，直接退出
				if (eventCode.equals("logout")) {
					_log.showDebug(".......session is invalidate, logout event end!!");
					responseContext.setSuccessed(true);
					return responseContext;
				}
				
				responseContext.setMessage(JsMessage.getValue("commonaction.nologin"));
				return responseContext;
			} else {
				//判断当前用户是否有效
				String reqUserId = requestContext.getRequestValue("user_id");
				String userId = mpUser.get("user_id");
				String reqUserCode = requestContext.getRequestValue("user_code");
				String userCode = mpUser.get("user_code");
				if (!reqUserId.equals(userId) && !reqUserCode.equals(userCode)) {
					responseContext.setMessage(JsMessage.getValue("commonaction.nouser"));
					return responseContext;
				}

				//如果请求参数中没有用户ID，则可以从会话中取用户ID
				if (reqUserId.length() == 0) {
					reqUserId = MapUtil.getValue(mpUser, "user_id");
					requestContext.setRequestValue("user_id", reqUserId);
				}
				
				//把当前用户信息存到上下文中
				requestContext.setUserInfo(mpUser);
			}
		}
		
		//创建服务控制器对象
		ServiceController serviceController = (ServiceController) SystemFactory
										.createSystemObject("ServiceController");
		if (serviceController == null) {
			responseContext.setMessage(JsMessage.getValue("commonaction.createenginefaild"));
			return responseContext;
		}
		
		//执行失败时，把内部的消息抛给前台
		if (!serviceController.execute(requestContext)) {
			String message = requestContext.getMessage();
			if (message == null || message.length() == 0) {
				message = JsMessage.getValue("commonaction.faild");
			}
			responseContext.setMessage(message);
			responseContext.setResponseData(requestContext.getReturnData());
			
			return responseContext;
		}
		
		//如果是登陆则保存用户信息到会话中
		if (funId.equals("login")) {
			//退出系统则会话实现
			if (eventCode.equals("logout")) {
				request.getSession().invalidate();
			} else {
				Map<String,String> mpUser = requestContext.getUserInfo();
				request.getSession().setAttribute(
									JsParam.CURRUSER, mpUser);
			}
		}
		
		//把请求对象中的响应结果反馈到响应对象中
		ActionHelper.contextToResponse(requestContext, responseContext);

		responseContext.setSuccessed(true);
		return responseContext;
	}

}
