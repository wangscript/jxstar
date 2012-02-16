/*
 * SqlRuleControllerTest.java 2009-10-6
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
public class SqlRuleControllerTest extends AbstractTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub	
		SqlRuleControllerTest test = new SqlRuleControllerTest();
		ServiceControllerImp ctl = new ServiceControllerImp();
		
		RequestContext context = test.createContext();
		ctl.execute(context);
		
		System.out.println(context.getReturnData());
	}

	private RequestContext createContext() {
		Map<String,Object> mp = FactoryUtil.newMap();
		String funid = "sys_event";
		String code = "import";
		String type = "import";
		
		Map<String,String> user = FactoryUtil.newMap();
		user.put("user_code", "admin");
		user.put("user_id", "administor");

		//请求头部
		String[] astr = new String[]{"sys0061","sys0062"};
		mp.put(JsParam.KEYID, astr);
		//mp.put("fkValue", "tm6078704");
		mp.put(JsParam.EVENTCODE, code);
		mp.put(JsParam.PAGETYPE, type);
		mp.put(JsParam.FUNID, funid);
		mp.put("destfunid", "fun_event");
		mp.put(JsParam.REALPATH, "d:/Tomcat6/webapps/jxstar");
		
		RequestContext context = new RequestContext(mp);
		context.setFunID(funid);
		context.setEventCode(code);
		context.setPageType(type);
		context.setUserInfo(user);
		
		return context;
	}

}
