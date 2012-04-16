/*
 * DmConfig.java 2010-12-17
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.dm.util;

import java.util.List;
import java.util.Map;

import org.jxstar.dao.BaseDao;
import org.jxstar.dao.DaoParam;
import org.jxstar.dm.DmException;
import org.jxstar.util.MapUtil;
import org.jxstar.util.resource.JsMessage;

/**
 * 取数据库配置信息，为数据建模工具服务。
 *
 * @author TonyTan
 * @version 1.0, 2010-12-17
 */
public class DmConfig {
	//dao对象
	private static BaseDao _dao = BaseDao.getInstance();
	//字段表的字段串
	private static String _field_sql = "field_id, field_name, field_title, data_type, data_size, data_scale, default_value, nullable, like_field, field_memo, field_type, field_index, table_id, state";
	//数据表的字段串
	private static String _table_sql = "table_id, table_name, table_title, table_memo, table_space, key_field, state, ds_name, table_type";
	//索引表的字段串
	private static String _index_sql = "index_id, index_name, isunique, index_field, index_memo, table_id, state";
	
	/**
	 * 检查是否存在未完成的记录
	 * @param tableName -- 数据库表名
	 */
	public static boolean hasNotComplete(String tableName) {
		String sql = "select count(*) as cnt from "+ tableName +" where state <> '6'";
		DaoParam param = _dao.createParam(sql);
		Map<String,String> mpCnt = _dao.queryMap(param);
		
		return MapUtil.hasRecord(mpCnt);
	}
	
	/**
	 * 检查该字段是否有建设索引
	 * @param tableId -- 字段配置ID
	 * @param fieldName -- 字段名
	 * @return
	 */
	public static boolean isIndexField(String tableId, String fieldName) {
		String sql = "select count(*) as cnt from dm_index where table_id = ? and index_field like ?";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(tableId);
		param.addStringValue("%"+fieldName+"%");
		
		Map<String,String> mpCnt = _dao.queryMap(param);
		return MapUtil.hasRecord(mpCnt);
	}
	
	/**
	 * 删除配置信息公共方法
	 * @param tableName -- 数据库中的配置表表名
	 * @param tableId -- 配置表ID
	 * @return
	 */
	public static boolean deleteCfg(String tableName, String tableId) throws DmException {
		return deleteCfg(tableName, "table_id", tableId);
	}
	
	/**
	 * 删除配置信息公共方法
	 * @param tableName -- 数据库中的配置表表名
	 * @param tableName -- 配置表的主键
	 * @param tableId -- 配置表ID
	 * @return
	 */
	public static boolean deleteCfg(String tableName, String keyField, String tableId) throws DmException {
		String sql = "delete from "+ tableName +" where "+ keyField +" = ?";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(tableId);
		
		boolean ret = _dao.update(param);
		if (!ret) {//"删除【{0}】表中的记录【{1}】出错！"
			throw new DmException(JsMessage.getValue("dmconfig.delerror"), tableName, tableId);
		}
		return true;
	}
	
	/**
	 * 更新索引配置的状态
	 * @param indexId -- 索引配置ID
	 * @param state -- 状态
	 * @return
	 */
	public static boolean updateIndexState(String indexId, String state) throws DmException {
		String sql = "update dm_indexcfg set state = ? where index_id = ?";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(state);
		param.addStringValue(indexId);
		
		boolean ret = _dao.update(param);
		if (!ret) {//"修改【{0}】索引配置的状态为【{1}】出错！"
			throw new DmException(JsMessage.getValue("dmconfig.indexerror"), indexId, state);
		}
		return true;
	}
	
	/**
	 * 更新字段配置的状态
	 * @param fieldId -- 字段配置ID
	 * @param state -- 状态
	 * @return
	 */
	public static boolean updateFieldState(String fieldId, String state) throws DmException {
		String sql = "update dm_fieldcfg set state = ? where field_id = ?";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(state);
		param.addStringValue(fieldId);
		
		boolean ret = _dao.update(param);
		if (!ret) {//"修改【{0}】字段配置的状态为【{1}】出错！"
			throw new DmException(JsMessage.getValue("dmconfig.fielderror"), fieldId, state);
		}
		return true;
	}
	
	/**
	 * 更新配置的状态
	 * @param tableName -- 数据库表名
	 * @param tableId -- 表配置ID
	 * @param state -- 状态
	 * @return
	 */
	public static boolean updateState(String tableName, String tableId, String state) throws DmException {
		String sql = "update "+ tableName +" set state = ? where table_id = ?";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(state);
		param.addStringValue(tableId);
		
		boolean ret = _dao.update(param);
		if (!ret) {//"修改【{0}】表的状态为【{1}】出错！"
			throw new DmException(JsMessage.getValue("dmconfig.stateerror"), tableName, state);
		}
		return true;
	}
	
	/**
	 * 更新表配置的状态
	 * @param tableId -- 表配置ID
	 * @param state -- 状态
	 * @return
	 */
	public static boolean updateTableState(String tableId, String state) {
		String sql = "update dm_tablecfg set state = ? where table_id = ?";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(state);
		param.addStringValue(tableId);
		
		return _dao.update(param);
	}
	
	/**
	 * 取表配置状态
	 * @param tableId -- 表配置ID
	 * @return
	 */
	public static String getTableCfgState(String tableId) {
		String sql = "select state from dm_tablecfg where table_id = ?";
		
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(tableId);
		
		Map<String,String> mpTable = _dao.queryMap(param);
		if (mpTable.isEmpty()) return "";
		
		return mpTable.get("state");
	}

	/**
	 * 根据表ID取表配置信息
	 * @param tableId -- 表配置ID
	 * @return
	 */
	public static Map<String,String> getTableCfg(String tableId) {
		String sql = "select "+ _table_sql +" from dm_tablecfg where table_id = ?";
		
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(tableId);
		
		return _dao.queryMap(param);
	}
	
	/**
	 * 根据表名取表配置信息
	 * @param tableName -- 表名
	 * @return
	 */
	public static Map<String,String> getTableCfgByName(String tableName) {
		String sql = "select "+ _table_sql +" from dm_tablecfg where table_name = ?";
		
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(tableName);
		
		return _dao.queryMap(param);
	}
	
	/**
	 * 取字段配置信息
	 * @param fieldId -- 字段配置ID
	 * @return
	 */
	public static Map<String,String> getMapField(String fieldId) {
		String sql = "select "+ _field_sql +" from dm_fieldcfg where field_id = ?";
		
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(fieldId);
		
		return _dao.queryMap(param);
	}
	
	/**
	 * 取索引配置信息
	 * @param indexId -- 索引配置ID
	 * @return
	 */
	public static Map<String,String> getMapIndex(String indexId) {
		String sql = "select "+ _index_sql +" from dm_indexcfg where index_id = ?";
		
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(indexId);
		
		return _dao.queryMap(param);
	}
	/**
	 * 根据表ID取字段配置信息
	 * @param tableId -- 表配置ID
	 * @return
	 */
	public static List<Map<String,String>> getFieldCfg(String tableId) {
		return getFieldCfg(tableId, null);
	}
	
	/**
	 * 根据表ID取字段配置信息
	 * @param tableId -- 表配置ID
	 * @param stateWhere -- 状态查询条件
	 * @return
	 */
	public static List<Map<String,String>> getFieldCfg(String tableId, String stateWhere) {
		StringBuilder sql = new StringBuilder();
		sql.append("select "+ _field_sql +" from dm_fieldcfg where table_id = ?");
		if (stateWhere != null && stateWhere.length() > 0) {
			sql.append(" and " + stateWhere);
		}
		sql.append(" order by field_index");
		
		DaoParam param = _dao.createParam(sql.toString());
		param.addStringValue(tableId);
		
		return _dao.query(param);
	}
	
	/**
	 * 根据表ID取索引配置信息
	 * @param tableId -- 表配置ID
	 * @return
	 */
	public static List<Map<String,String>> getIndexCfg(String tableId) {
		return getIndexCfg(tableId, null);
	}
	
	/**
	 * 根据表ID取索引配置信息
	 * @param tableId -- 表配置ID
	 * @param stateWhere -- 状态查询条件
	 * @return
	 */
	public static List<Map<String,String>> getIndexCfg(String tableId, String stateWhere) {
		StringBuilder sql = new StringBuilder();
		sql.append("select "+ _index_sql +" from dm_indexcfg where table_id = ?");
		if (stateWhere != null && stateWhere.length() > 0) {
			sql.append(" and " + stateWhere);
		}
		
		DaoParam param = _dao.createParam(sql.toString());
		param.addStringValue(tableId);
		
		return _dao.query(param);
	}
	
	/**
	 * 根据表ID取表原配置信息
	 * @param tableId -- 表ID
	 * @return
	 */
	public static Map<String,String> getTableOldCfg(String tableId) {
		String sql = "select "+ _table_sql +" from dm_table where table_id = ?";
		
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(tableId);
		
		return _dao.queryMap(param);
	}
	
	/**
	 * 根据表ID取字段原配置信息
	 * @param tableId -- 表ID
	 * @return
	 */
	public static List<Map<String,String>> getFieldOldCfg(String tableId) {
		String sql = "select "+ _field_sql +" from dm_field where table_id = ? order by field_index";
		
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(tableId);
		
		return _dao.query(param);
	}
	
	/**
	 * 根据表ID取索引原配置信息
	 * @param tableId -- 表ID
	 * @return
	 */
	public static List<Map<String,String>> getIndexOldCfg(String tableId) {
		String sql = "select "+ _index_sql +" from dm_index where table_id = ?";
		
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(tableId);
		
		return _dao.query(param);
	}
}
