/*
 * ToolbarQuery.java 2009-10-13
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.service.query;

import java.util.List;
import java.util.Map;


import org.jxstar.service.BusinessObject;
import org.jxstar.service.define.DefineDataManger;
import org.jxstar.service.define.FunDefineDao;
import org.jxstar.service.util.SysUserUtil;
import org.jxstar.util.factory.FactoryUtil;
import org.jxstar.util.resource.JsMessage;

/**
 * 生成工具栏的JSON，根据当前用户的操作权限，没有权限的按钮将不显示，如果是子功能，则取父功能的操作权限。
 * 如果功能注册类型是：选择功能、其它功能，则不做功能权限控制。
 *
 * @author TonyTan
 * @version 1.0, 2009-10-13
 */
public class ToolbarQuery extends BusinessObject {
	private static final long serialVersionUID = 1L;

	/**
	 * 创建工具的Json对象
	 * @param userId -- 用户ID
	 * @param funId -- 功能ID
	 * @param pageType -- 页面类型
	 * @param parentFunId -- 父功能ID
	 * @return
	 */
	public String createJson(String userId, String funId, String pageType, String parentFunId) {
		if (funId == null || funId.length() == 0) {
			setMessage(JsMessage.getValue("pagedesign.funidisnull"));
			return _returnFaild;
		}
		if (pageType == null || pageType.length() == 0) {
			setMessage(JsMessage.getValue("pagedesign.pagetypeisnull"));
			return _returnFaild;
		}
		String json = "{buttons:[], right:null}";
		
		//判断用户是否为系统管理员
		boolean isAdmin = SysUserUtil.isAdmin(userId);
		//判断当前功能是否受操作权限控制
		boolean hasRight= hasRight(funId);
		
		List<Map<String, String>> lsEvent = FunDefineDao.queryEvent(funId, pageType);
		if (lsEvent == null || lsEvent.isEmpty()) {
			//setMessage(JsMessage.getValue("toolbar.eventempty"));
			setReturnData(json);
			return _returnSuccess;
		}
		
		//保存操作权限的JSON对象
		String rightJson = "null";
		
		//取当前功能的操作权限
		Map<String,String> mpRight = FactoryUtil.newMap();
		if (!isAdmin && hasRight) {
			if (parentFunId != null && parentFunId.length() > 0) {
				mpRight = SysUserUtil.getFunRight(userId, parentFunId);
			} else {
				mpRight = SysUserUtil.getFunRight(userId, funId);
			}
			rightJson = getRightJson(mpRight);
		}
		
		//审批页面类型不做处理
		boolean isCheck = (pageType.indexOf("chk") >= 0);
		
		//构建按钮组
		StringBuilder sbItems = new StringBuilder();
		for (int i = 0, n = lsEvent.size(); i < n; i++) {
			Map<String, String> mpEvent = lsEvent.get(i);
			
			//判断是否该按钮的操作权限
			if (!isAdmin && !isCheck && hasRight) {
				String rightType = mpEvent.get("right_type");
				String rightValue = mpRight.get(rightType);
				if (!rightValue.equals("1")) continue;
			}
			
			String item = createItem(mpEvent);
			sbItems.append(item).append(",\r");
		}
		String buttonJson = "[]";
		if (sbItems.length() > 0) {
			buttonJson = "[" + sbItems.substring(0, sbItems.length()-2) + "]";
		}
		
		json = "{buttons:"+ buttonJson +", right:"+ rightJson +"}";
		
		//_log.showDebug(json);
		setReturnData(json);
		return _returnSuccess;
	}
	
	//构建一个按钮信息
	private String createItem(Map<String, String> mpEvent) {
		String pageType = mpEvent.get("page_type");//.replace(",", "\\,");
		String clientMethod = mpEvent.get("client_method");
		String method = "", args = "";
		if (clientMethod != null && clientMethod.length() > 0) {
			if (clientMethod.indexOf("(") >= 0) {
				//取方法名
				method = clientMethod.split("\\(")[0].trim();
				//取方法的参数
				args = clientMethod.substring(clientMethod.indexOf("(")+1, clientMethod.indexOf(")"));
				//args = args.replace("'", "\\'");				
			} else {
				//无参数
				method = clientMethod;
			}
		}
		
		StringBuilder sbItem = new StringBuilder();
		sbItem.append("{text:'"+ mpEvent.get("event_name") +"', ");
		sbItem.append("method:'"+ method +"', ");
		sbItem.append("args:["+ args +"], ");
		sbItem.append("showType:'"+ mpEvent.get("show_type") +"', ");
		sbItem.append("eventCode:'"+ mpEvent.get("event_code") +"', ");
		sbItem.append("eventIndex:'"+ mpEvent.get("event_index") +"', ");
		sbItem.append("iconCls:'eb_"+ mpEvent.get("event_code") +"', ");
		sbItem.append("pageType:'"+ pageType +"', ");
		sbItem.append("rightType:'"+ mpEvent.get("right_type") +"', ");
		sbItem.append("accKey:'"+ mpEvent.get("access_key") +"'}");
		
		return sbItem.toString();
	}
	
	/**
	 * 是否受操作权限控制，主功能受操作权限控制，子功能不受操作权限控制。
	 * @param funId -- 功能ID
	 * @return
	 */
	private boolean hasRight(String funId) {
		Map<String,String> mpFun = DefineDataManger.getInstance().getFunData(funId);
		String regType = mpFun.get("reg_type");
		
		return (regType.equals("main") || regType.equals("treemain"));
	}
	
	/**
	 * 取功能权限构建的JSON
	 * @param mpRight
	 * @return
	 */
	private String getRightJson(Map<String,String> mpRight) {
		StringBuilder sbJson = new StringBuilder();
		
		sbJson.append("{edit:'").append(mpRight.get("edit")).append("',");
		sbJson.append("print:'").append(mpRight.get("print")).append("',");
		sbJson.append("audit:'").append(mpRight.get("audit")).append("',");
		sbJson.append("other:'").append(mpRight.get("other")).append("'}");
		
		return sbJson.toString();
	}
}
