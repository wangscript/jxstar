/*
 * ServiceControllerTest.java 2009-10-6
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.control;

import java.util.Map;


import org.jxstar.control.action.RequestContext;
import org.jxstar.service.control.ServiceControllerImp;
import org.jxstar.test.AbstractTest;
import org.jxstar.util.factory.FactoryUtil;
import org.jxstar.util.resource.JsParam;

/**
 * 控制器测试
 *
 * @author TonyTan
 * @version 1.0, 2009-10-6
 */
public class ServiceControllerTest extends AbstractTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub	
		ServiceControllerTest test = new ServiceControllerTest();
		ServiceControllerImp ctl = new ServiceControllerImp();
		
		RequestContext context = test.loginContext();
		ctl.execute(context);
		
		System.out.println("getReturnData=" + context.getReturnData());
	}

	private RequestContext loginContext() {
		Map<String,Object> mp = FactoryUtil.newMap();
		String funid = "login";
		String code = "login";
		String type = "login";
		
		Map<String,String> user = FactoryUtil.newMap();
		user.put("user_code", "admin");
		user.put("user_pass", "888");

		mp.put("user_code", "admin");
		mp.put("user_pass", "888");
		mp.put(JsParam.EVENTCODE, "login");
		mp.put(JsParam.PAGETYPE, "login");
		mp.put(JsParam.FUNID, "login");
		
		mp.put(JsParam.REALPATH, "d:/Tomcat6/webapps/jxstar");
		
		RequestContext context = new RequestContext(mp);
		context.setFunID(funid);
		context.setEventCode(code);
		context.setPageType(type);
		context.setUserInfo(user);
		
		return context;
	}

	public RequestContext createContext() {
		Map<String,Object> mp = FactoryUtil.newMap();
		String funid = "query_data";
		String code = "sys_control";
		String type = "grid";
		
		Map<String,String> user = FactoryUtil.newMap();
		user.put("user_code", "admin");
		user.put("user_id", "administor");

		/*mp.put(JsParam.KEYID, "tm6703025");
		mp.put("copynum", "5");
		mp.put(JsParam.EVENTCODE, code);
		mp.put(JsParam.PAGETYPE, type);
		mp.put(JsParam.FUNID, funid);*/
		
		mp.put("tree_level", "1");
		mp.put("tree_funid", "sys_module");
		
		RequestContext context = new RequestContext(mp);
		context.setFunID(funid);
		context.setEventCode(code);
		context.setPageType(type);
		context.setUserInfo(user);
		
		return context;
	}
	
	public RequestContext queryContext() {
		Map<String,Object> mp = FactoryUtil.newMap();
		String funid = "queryevent";
		String code = "grid_query";
		String type = "";
		
		Map<String,String> user = FactoryUtil.newMap();
		user.put("user_code", "admin");
		user.put("user_id", "administor");

		mp.put("limit", "50");
		mp.put("start", "0");
		mp.put(JsParam.EVENTCODE, code);
		mp.put(JsParam.PAGETYPE, type);
		mp.put(JsParam.FUNID, funid);
		
		mp.put("query_funid", "sys_fun_base");
		mp.put("selpagetype", "editgrid");
		
		RequestContext context = new RequestContext(mp);
		context.setFunID(funid);
		context.setEventCode(code);
		context.setPageType(type);
		context.setUserInfo(user);
		
		return context;
	}
}
