package org.jxstar.service.studio;

import java.util.Map;


import org.jxstar.control.action.RequestContext;
import org.jxstar.service.control.ServiceControllerImp;
import org.jxstar.test.AbstractTest;
import org.jxstar.util.factory.FactoryUtil;
import org.jxstar.util.resource.JsParam;

public class QueryDataTest extends AbstractTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub	
		QueryDataTest test = new QueryDataTest();
		ServiceControllerImp ctl = new ServiceControllerImp();
		
		RequestContext context = test.queryContext();
		ctl.execute(context);
		
		System.out.println(context.getReturnData());
	}
	
	private RequestContext queryContext() {
		Map<String,Object> mp = FactoryUtil.newMap();
		String funid = "queryevent";
		String code = "cond_query";
		String type = "";
		
		Map<String,String> user = FactoryUtil.newMap();
		user.put("user_code", "DongHong");
		user.put("user_id", "jxstar10102209192201");

		mp.put(JsParam.EVENTCODE, code);
		mp.put(JsParam.PAGETYPE, type);
		mp.put(JsParam.FUNID, funid);
		
		mp.put("selfunid", "run_malrecord");
		
		RequestContext context = new RequestContext(mp);
		context.setFunID(funid);
		context.setEventCode(code);
		context.setPageType(type);
		context.setUserInfo(user);
		
		return context;
	}

}
