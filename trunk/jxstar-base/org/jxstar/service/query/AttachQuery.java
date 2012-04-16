/*
 * GridQuery.java 2011-10-26
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.service.query;

import java.util.List;
import java.util.Map;

import org.jxstar.dao.DaoParam;
import org.jxstar.service.BusinessObject;
import org.jxstar.util.factory.FactoryUtil;

/**
 * 查询当前表格中显示记录的相关附件信息。
 *
 * @author TonyTan
 * @version 1.0, 2011-10-26
 */
public class AttachQuery extends BusinessObject {
	private static final long serialVersionUID = 1L;
	
	/**
	 * 取某个表是附件标志
	 * @param tableName -- 当前表
	 * @param keyIds -- 当前显示的记录ID，格式如：key1,key2,key3...
	 * @return
	 */
	public String query(String tableName, String keyIds) {
		//_log.showDebug("------------query attach, tablename=" + tableName + ";keyids=" + keyIds);
		
		if (tableName == null || tableName.length() == 0) return _returnSuccess;
		if (keyIds == null || keyIds.length() == 0) return _returnSuccess;
		
		//取附件记录
		String sql = "select data_id, attach_id, attach_name, content_type, fun_id from sys_attach " +
				"where table_name = ? and data_id in " + keyIns(keyIds) + " order by data_id";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(tableName);
		List<Map<String,String>> lsData = _dao.query(param);
		if (lsData.isEmpty()) return _returnSuccess;
		
		Map<String,String> keyIndex = getMap(keyIds);
		StringBuilder sbJson = new StringBuilder();
		for (int i = 0, n = lsData.size(); i < n; i++) {
			Map<String,String> mpData = lsData.get(i);
			
			String dataid = mpData.get("data_id");
			String json = "{row_num:"+ keyIndex.get(dataid) +", " +
					"data_id:'"+ dataid +"', " +
					"attach_id:'"+ mpData.get("attach_id") +"', " +
					"attach_name:'"+ mpData.get("attach_name") +"', " +
					"fun_id:'"+ mpData.get("fun_id") +"', " +
					"content_type:'"+ mpData.get("content_type") +"'},";
			
			sbJson.append(json);
		}
		String jsdata = "[" + sbJson.substring(0, sbJson.length()-1) + "]";
		//_log.showDebug("query attach json=" + jsdata);
		
		//返回查询数据
		setReturnData(jsdata);
		
		return _returnSuccess;
	}
	
	//保存主键值的序号
	private Map<String,String> getMap(String keyIds) {
		Map<String,String> mpkeys = FactoryUtil.newMap();
		String[] keys = keyIds.split(",");
		for (int i = 0; i < keys.length; i++) {
			mpkeys.put(keys[i], Integer.toString(i));
		}
		return mpkeys;
	}
	
	private String keyIns(String keyIds) {
		String keyIns = keyIds.replaceAll(",", "','");
		
		return "('" + keyIns + "')";
	}
}
