/*
 * Copyright(c) 2012 Donghong Inc.
 */
package org.jxstar.service.query;

import java.util.Map;

import org.jxstar.control.action.RequestContext;
import org.jxstar.test.AbstractTest;
import org.jxstar.util.factory.FactoryUtil;
import org.jxstar.util.resource.JsParam;

/**
 * 
 *
 * @author TonyTan
 * @version 1.0, 2012-3-1
 */
public class TreeQueryExtTest extends AbstractTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TreeQuery query = new TreeQuery();
		
		query.queryTree(getRequest());
	}
	
	private static RequestContext getRequest() {
		Map<String,Object> mp = FactoryUtil.newMap();
		String funid = "queryevent";
		String code = "query_tree";
		String type = "query";
		
		Map<String,String> user = FactoryUtil.newMap();
		user.put("user_id", "administrator");

		mp.put("user_id", "administrator");
		mp.put("node", "100100010001");
		mp.put("tree_no", "1");
		mp.put("tree_funid", "tech_model");
		
		mp.put(JsParam.EVENTCODE, code);
		mp.put(JsParam.PAGETYPE, type);
		mp.put(JsParam.FUNID, funid);
		
		RequestContext context = new RequestContext(mp);
		context.setFunID(funid);
		context.setEventCode(code);
		context.setPageType(type);
		context.setUserInfo(user);
		
		return context;
	}
}
