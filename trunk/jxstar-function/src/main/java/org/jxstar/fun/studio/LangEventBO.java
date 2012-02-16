/*
 * LangEventBO.java 2011-3-31
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */

package org.jxstar.fun.studio;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jxstar.dao.DaoParam;
import org.jxstar.service.BusinessObject;
import org.jxstar.service.define.DefineName;
import org.jxstar.util.FileUtil;
import org.jxstar.util.factory.FactoryUtil;
import org.jxstar.util.resource.JsMessage;

/**
 * 生成功能事件的语言文件。
 *
 * @author TonyTan
 * @version 1.0, 2011-3-31
 */
public class LangEventBO extends BusinessObject {
	private static final long serialVersionUID = 1L;

	/**
	 * 先构建所有系统事件的文字，然后检索每个功能的自定义事件，
	 * 如果在系统事件中存在事件代号与名称相同的事件，则不构建新的文字，否则要添加。
	 * @param realPath -- 系统路径
	 * @return
	 */
	public String createJson(String realPath) {
		//系统事件信息
		Map<String,String> mpSysEvent = getSysEvent();
		
		//功能自定义事件信息
		Map<String,Map<String,String>> mpFunEvent = getFunEvent(mpSysEvent);
		
		//事件描述文件
		StringBuilder sbJson = new StringBuilder("jeLang = {\r");
		
		//添加系统事件信息
		sbJson.append(getJson("sysevent", mpSysEvent));
		
		//添加功能自定义事件信息
		Iterator<String> itr = mpFunEvent.keySet().iterator();
		while(itr.hasNext()) {
			String funId = itr.next();
			Map<String,String> mpEvent = mpFunEvent.get(funId);
			
			sbJson.append(getJson(funId, mpEvent));
		}
		String json = sbJson.substring(0, sbJson.length()-2) + "\r};";
		
		//_log.showDebug("-------json=" + json);
		
		//生成文件
		String fileName = realPath + "/public/locale/event-lang-zh.js";
		if (!FileUtil.saveFileUtf8(fileName, json)) {
			setMessage(JsMessage.getValue("combodata.fcerror"));
			return _returnFaild;
		}
		_log.showDebug("-------create success! " + fileName);
		
		return _returnSuccess;
	}
	
	/**
	 * 取一个功能的事件信息
	 * @param funId -- 功能ID
	 * @param mpEvent -- 事件信息
	 * @return
	 */
	private String getJson(String funId, Map<String,String> mpEvent) {
		StringBuilder sbItem = new StringBuilder();
		sbItem.append("\t'").append(funId).append("':{\r");
		
		Iterator<String> itr = mpEvent.keySet().iterator();
		while(itr.hasNext()) {
			String code = itr.next();
			String name = mpEvent.get(code);
			
			sbItem.append("\t\t'").append(code).append("':'").append(name).append("',\r");
		}
		
		String item = sbItem.substring(0, sbItem.length()-2);
		
		return item + "\r\t},\r";
	}
	
	/**
	 * 取功能事件信息
	 * @return
	 */
	private Map<String,Map<String,String>> getFunEvent(Map<String,String> mpSysEvent) {
		Map<String,Map<String,String>> mpFunEvent = FactoryUtil.newMap();
		
		String where = "fun_id <> 'sysevent' and is_hide = '0' and is_domain = '0' order by fun_id, event_code";
		List<Map<String,String>> lsEvent = queryEvent(where);
		
		String curFunId = "";
		Map<String,String> mpEvent = FactoryUtil.newMap();
		for (int i = 0, n = lsEvent.size(); i < n; i++) {
			Map<String,String> mpData = lsEvent.get(i);
			
			String funid = mpData.get("fun_id");
			String code = mpData.get("event_code");
			String name = mpData.get("event_name");
			
			//一个新的功能
			if (!funid.equals(curFunId)) {
				if (i > 0 && !mpEvent.isEmpty()) {
					mpFunEvent.put(curFunId, mpEvent);
					//构建新功能的事件信息
					mpEvent = FactoryUtil.newMap();
				}
				curFunId = funid;
			}
			
			//在系统事件中没有的事件才添加
			String sysName = mpSysEvent.get(code);
			if (sysName == null || (sysName != null && !sysName.equals(name))) {
				mpEvent.put(code, name);
			}
		}
		//最后一个功能
		if (curFunId.length() > 0 && !mpEvent.isEmpty()) {
			mpFunEvent.put(curFunId, mpEvent);
		}
		
		return mpFunEvent;
	}
	
	/**
	 * 取系统事件信息
	 * @return
	 */
	private Map<String,String> getSysEvent() {
		Map<String,String> mpEvent = FactoryUtil.newMap();
		
		String where = "fun_id = 'sysevent' and is_hide = '0' order by event_code";
		List<Map<String,String>> lsEvent = queryEvent(where);
		
		for (int i = 0, n = lsEvent.size(); i < n; i++) {
			Map<String,String> mpData = lsEvent.get(i);
			
			String code = mpData.get("event_code");
			String name = mpData.get("event_name");
			
			mpEvent.put(code, name);
		}
		
		return mpEvent;
	}
	
	/**
	 * 取事件代码与名称
	 * @param where -- 查询语句
	 * @return
	 */
	private List<Map<String,String>> queryEvent(String where) {
		String sql = "select event_name, event_code, fun_id from fun_event where " + where;
		
		DaoParam param = _dao.createParam(sql);
		param.setDsName(DefineName.DESIGN_NAME);
		return _dao.query(param);
	}
}
