/*
 * ColumnMd.java 2009-9-28
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.service.define;

import java.util.List;
import java.util.Map;

import org.jxstar.util.factory.FactoryUtil;
import org.jxstar.util.log.Log;
import org.jxstar.util.resource.JsMessage;

/**
 * 字段明细定义信息对象。
 *
 * @author TonyTan
 * @version 1.0, 2009-9-28
 */
public class ColumnDefine {
	private Log _log = Log.getInstance();
	//字段名与列表中的序号
	private Map<String,Integer> _colIndex = null;
	//字段信息列表
	private List<Map<String,String>> _colList = null;
	
	public ColumnDefine(String funId) {
		_colList = FunDefineDao.queryCol(funId);
		
		setColumnList(_colList);
	}
	
	public ColumnDefine(List<Map<String, String>> colList) {
		setColumnList(colList);
	}
	
	/**
	 * 根据字段名取得字段定义信息
	 * @param colCode -- 字段名
	 * @return
	 */
	public Map<String, String> getColumnData(String colCode) {
		Map<String, String> ret = FactoryUtil.newMap();
		
		if (_colIndex == null) {
			//"列数据对象没有初始化！"
			_log.showWarn(JsMessage.getValue("columndefine.colisnull"));
			return ret;
		}
			
		Integer index = _colIndex.get(colCode);
		if (index == null) {
			_log.showWarn(JsMessage.getValue("columndefine.colisnull"));
			return ret;
		}
		
		return _colList.get(index.intValue());
	}
	
	/**
	 * 取字段值列表
	 * @return
	 */
	public List<Map<String, String>> getColumnList() {
		return _colList;
	}
	
	/**
	 * 保存每个字段在列表中的序号
	 * @param colList
	 */
	private void setColumnList(List<Map<String, String>> colList) {
		_colList = colList;
		_colIndex = FactoryUtil.newMap();
		
		for (int i = 0, n = colList.size(); i < n; i++) {
			Map<String, String> mpcol = colList.get(i);
			_colIndex.put(mpcol.get("col_code"), i);
		}
	}
}
