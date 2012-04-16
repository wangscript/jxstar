/*
 * GridSaveControllerTest.java 2009-10-6
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
public class GridSaveControllerTest extends AbstractTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub	
		GridSaveControllerTest test = new GridSaveControllerTest();
		ServiceControllerImp ctl = new ServiceControllerImp();
		
		RequestContext context = test.createContext();
		ctl.execute(context);
		
		System.out.println(context.getReturnData());
	}

	private RequestContext createContext() {
		Map<String,Object> mp = FactoryUtil.newMap();
		String funid = "event_param";
		String code = "save_eg";
		String type = "editgrid";
		
		Map<String,String> user = FactoryUtil.newMap();
		user.put("user_code", "admin");
		user.put("user_id", "administor");
		
		//请求头部
		mp.put(JsParam.KEYID, "tm6703025");
		mp.put("fkValue", "tm6078704");
		mp.put(JsParam.EVENTCODE, code);
		mp.put(JsParam.PAGETYPE, type);
		mp.put(JsParam.FUNID, funid);
		mp.put(JsParam.REALPATH, "d:/Tomcat6/webapps/jxstar");
		
		//请求参数值
		mp.put("fun_event_param.param_type", "parameter");
		mp.put("fun_event_param.param_id", "tmaa1");
		mp.put("fun_event_param.invoke_id", "tm6078704");
		mp.put("fun_event_param.param_name", JsParam.FUNID);
		mp.put("fun_event_param.param_index", "1");
		mp.put("fun_event_param.param_value", "");
		mp.put("fun_event_param.param_memo", "AA");
		
		RequestContext context = new RequestContext(mp);
		context.setFunID(funid);
		context.setEventCode(code);
		context.setPageType(type);
		context.setUserInfo(user);
		
		return context;
	}

}
