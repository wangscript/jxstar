/*
 * PageParserUtil.java 2010-10-15
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.fun.design.parser;

import java.util.List;
import java.util.Map;

import org.jxstar.dao.BaseDao;
import org.jxstar.dao.DaoParam;
import org.jxstar.service.define.DefineName;
import org.jxstar.util.factory.FactoryUtil;

/**
 * 页面解析工具类。
 *
 * @author TonyTan
 * @version 1.0, 2010-10-15
 */
public class PageParserUtil {
	private static BaseDao _dao = BaseDao.getInstance();
	
	/**
	 * 取字段长度，原来采用DatabaseMetaData.getColumns取值，
	 * 但字段长度不准确，后改为直接从系统表中取值。
	 * @param tableName -- 表名
	 * @return
	 */
	public static Map<String,String> fieldLength(String tableName) {
		Map<String,String> mpData = FactoryUtil.newMap();
		String sql = "select field_name, data_size from v_column_info where table_name = ?";

		DaoParam param = _dao.createParam(sql);
		param.setDsName(DefineName.DESIGN_NAME);
		param.addStringValue(tableName);
		
		List<Map<String,String>> lsData = _dao.query(param);
		if (lsData.isEmpty()) return mpData;
		
		for (int i = 0, n = lsData.size(); i < n; i++) {
			Map<String,String> mp = lsData.get(i);
			mpData.put(mp.get("field_name"), mp.get("data_size"));
		}
		
		return mpData;
	}
	
	/**
	 * 取选择控件定义信息
	 * @param ctlcode
	 * @return
	 */
	public static Map<String,String> selectWinCtl(String ctlcode) {
		String sql = "select fun_id, layout_page from funall_control where control_code = ?";
		DaoParam paramCtl = _dao.createParam(sql);
		paramCtl.setDsName(DefineName.DESIGN_NAME);
		paramCtl.addStringValue(ctlcode);
		
		return _dao.queryMap(paramCtl);
	}
}
