/*
 * FieldQueryBO.java 2011-2-21
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.fun.design;

import java.util.List;
import java.util.Map;

import org.jxstar.service.BusinessObject;
import org.jxstar.service.define.FunDefineDao;
import org.jxstar.util.resource.JsMessage;

/**
 * 功能字段信息查询类，用于表单同步属性方法。
 *
 * @author TonyTan
 * @version 1.0, 2011-2-21
 */
public class FieldQueryBO extends BusinessObject {
	private static final long serialVersionUID = -6235384390496534332L;

	/**
	 * 查询设计信息中需要的字段列表信息：
	 * 返回的JSON对象格式为：{'colcode': {xtype:'', title:'', visible:''}, ...}
	 * @param funId -- 功能ID
	 * @return
	 */
	public String queryField(String funId) {
		//取字段定义列表信息
		List<Map<String,String>> lsColumn = FunDefineDao.queryCol(funId);
		if (lsColumn.isEmpty()) {//"没有找到【{0}】功能的字段信息！"
			setMessage(JsMessage.getValue("formdisignbo.nofield"), funId);
			return _returnFaild;
		}
		
		StringBuilder sbjson = new StringBuilder();
		//取字段信息构建JSON对象
		for (int i = 0, n = lsColumn.size(); i < n; i++) {
			Map<String,String> mpColumn = lsColumn.get(i);
			
			String fieldJson = createFieldJson(mpColumn);
			sbjson.append(fieldJson).append(",");
		}
		
		//返回字段信息到前台
		String json = sbjson.substring(0, sbjson.length()-1);
		_log.showDebug("-------queryfield json=" + json);
		setReturnData("{" + json + "}");
		
		return _returnSuccess;
	}
	
	/**
	 * 取一个字段的JSON，格式为：colcode:{xtype:'', title:'', visible:''}
	 * @param mpField -- 字段信息
	 * @return
	 */
	private String createFieldJson(Map<String, String> mpField) {
		StringBuilder sbfield = new StringBuilder();
		
		String col_code = mpField.get("col_code");
		String col_name = mpField.get("col_name");
		String col_control = mpField.get("col_control");
		String col_index = mpField.get("col_index");
		//序号大于等于10000的控件缺省不显示
		String visible = Integer.parseInt(col_index)<10000 ? "true":"false";
		
		sbfield.append("'" + col_code).append("':{xtype:'").append(col_control)
				.append("', title:'").append(col_name).append("', visible:'")
				.append(visible).append("'}");
		
		return sbfield.toString();
	}
}
