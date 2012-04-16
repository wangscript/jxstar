/*
 * QueryControllerTest.java 2009-10-6
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
public class QueryControllerTest extends AbstractTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub	
		QueryControllerTest test = new QueryControllerTest();
		ServiceControllerImp ctl = new ServiceControllerImp();
		
		RequestContext context = test.queryContext();
		ctl.execute(context);
		
		System.out.println(context.getReturnData());
	}
	
	private RequestContext queryContext() {
		Map<String,Object> mp = FactoryUtil.newMap();
		String funid = "queryevent";
		String code = "query_data";
		String type = "";
		
		Map<String,String> user = FactoryUtil.newMap();
		user.put("user_code", "admin");
		user.put("user_id", "administor");

		//mp.put("limit", "50");
		//mp.put("start", "0");
		mp.put(JsParam.EVENTCODE, code);
		mp.put(JsParam.PAGETYPE, type);
		mp.put(JsParam.FUNID, funid);
		mp.put("sys.realpath", "d:/Tomcat6/webapps/jxstar");
		mp.put("is_page", "0");
		mp.put("user_id", "administor");
		mp.put("where_sql", "wf_nodeattr.node_id = ? and wf_nodeattr.process_id = ?");
		mp.put("where_value", "3;jxstar17");
		mp.put("where_type", "string;string");
		
		mp.put("query_funid", "wf_nodeattr");
		//mp.put("selpagetype", "grid");
		mp.put("dataType", "json");
		
		RequestContext context = new RequestContext(mp);
		context.setFunID(funid);
		context.setEventCode(code);
		context.setPageType(type);
		context.setUserInfo(user);
		
		return context;
	}
}
