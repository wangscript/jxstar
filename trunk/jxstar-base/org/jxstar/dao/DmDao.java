/*
 * DmDao.java 2011-1-26
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.dao;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jxstar.util.factory.FactoryUtil;
import org.jxstar.util.key.KeyCreator;
import org.jxstar.util.log.Log;

/**
 * 为简化数据库操作，体现以数据表为中心的面向对象的思想，
 * 基于数据模型信息设计的数据库操作对象，主要用于：
 * 数据查询、修改、新增、复制、删除等操作；
 * 
 * 数据表的字段信息没有缓存，表格结果集查询是采用select * from table的方式，如果只取少量的字段信息，
 * 建议采用_dao的方法直接操作SQL；
 * 
 * 本对象的操作不单独处理add_date, add_userid, modify_date, modify_userid字段。
 *
 * @author TonyTan
 * @version 1.0, 2011-1-26
 */
public class DmDao {
	private static BaseDao _dao = BaseDao.getInstance();
	private static Log _log = Log.getInstance();
	
	/**
	 * 根据主键值查询指定表的记录值：
	 * 构建的查询语句为：select * from tableName where keyField = ?
	 * @param tableName -- 表名
	 * @param keyId -- 主键值
	 * @return
	 */
	public static Map<String,String> queryMap(String tableName, String keyId) {
		Map<String,String> mpRet = FactoryUtil.newMap();
		if (tableName == null || tableName.length() == 0 || 
				keyId == null || keyId.length() == 0) {
			_log.showError("queryMap param tableName or keyId is null! ");
			return mpRet;
		}
		
		String keyField = DmDaoUtil.getKeyField(tableName);
		if (keyField == null || keyField.length() == 0) {
			_log.showError("queryMap keyField is null! ");
			return mpRet;
		}
		
		String sql = "select * from "+ tableName +" where "+ keyField +" = ?";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(keyId);
		
		return _dao.queryMap(param);
	}
	
	/**
	 * 根据过滤语句查询指定表的记录值。
	 * @param tableName -- 表名
	 * @param where -- 过滤语句，不含where关键字
	 * @return
	 */
	public static Map<String,String> queryMapByWhere(String tableName, String where) {
		Map<String,String> mpRet = FactoryUtil.newMap();
		if (tableName == null || tableName.length() == 0 || 
				where == null || where.length() == 0) {
			_log.showError("queryMapByWhere param tableName or where is null! ");
			return mpRet;
		}
		
		String sql = "select * from "+ tableName +" where "+ where;
		DaoParam param = _dao.createParam(sql);
		
		return _dao.queryMap(param);
	}
	
	/**
	 * 根据过滤语句查询指定表的数据集。
	 * @param tableName -- 表名
	 * @param where -- 过滤语句，不含where关键字
	 * @return
	 */
	public static List<Map<String,String>> query(String tableName, String where) {
		List<Map<String,String>> lsRet = FactoryUtil.newList();
		if (tableName == null || tableName.length() == 0 || 
				where == null || where.length() == 0) {
			_log.showError("query param tableName or where is null! ");
			return lsRet;
		}
		
		String sql = "select * from "+ tableName +" where "+ where;
		DaoParam param = _dao.createParam(sql);
		
		return _dao.query(param);
	}
	
	/**
	 * 更新指定记录的数据值：
	 * 更新的字段将从mpData的参数名中提取，数据类型从数据模型中取，主键字段名从数据模型中取。
	 * @param tableName -- 表名
	 * @param keyId -- 主键值
	 * @param mpData -- 需要更新的数据对象
	 * @return
	 */
	public static boolean update(String tableName, String keyId, Map<String,String> mpData) {
		if (tableName == null || tableName.length() == 0 || 
				keyId == null || keyId.length() == 0) {
			_log.showError("update param tableName or keyId is null! ");
			return false;
		}
		
		if (mpData == null || mpData.isEmpty()) {
			_log.showError("update param mpData is null! ");
			return false;
		}
		
		//取表的字段名与数据类型集
		Map<String,String> mpParamType = DmDaoUtil.getParamType(tableName);
		if (mpParamType == null || mpParamType.isEmpty()) {
			_log.showError("update mpParamType is null! ");
			return false;
		}
		
		//创建参数对象
		DaoParam param = _dao.createParam();
		
		//构建更新SQL
		StringBuilder sbsql = new StringBuilder("update ");
		sbsql.append(tableName);
		sbsql.append(" set ");
		
		Iterator<String> keyItr = mpData.keySet().iterator();
		while(keyItr.hasNext()) {
			String fieldName = keyItr.next();
			String fieldValue = mpData.get(fieldName);
			//构建更新SQL
			sbsql.append(fieldName + " = ?, ");
			//添加数据参数
			param.addValue(fieldValue);
			param.addType(mpParamType.get(fieldName));
		}
		
		//取更新SQL，格式：update tablename set field = ?, field = ? ...
		String sql = sbsql.substring(0, sbsql.length()-2);
		
		String keyField = DmDaoUtil.getKeyField(tableName);
		if (keyField == null || keyField.length() == 0) {
			_log.showError("update keyField is null! ");
			return false;
		}
		
		//添加where子句
		sql += " where " + keyField + " = ?";
		param.addStringValue(keyId);
		
		_log.showDebug("dmdao.update sql=" + sql);
		_log.showDebug("dmdao.update value=" + param.getValue());
		_log.showDebug("dmdao.update type=" + param.getType());
		
		//给更新参数对象添加SQL
		param.setSql(sql);
		
		return _dao.update(param);
	}
	
	/**
	 * 新增指定表的记录：
	 * 新增字段列表从mpData的参数列表中提取，数据类型从数据模型中取，主键字段名从数据模型中取。
	 * 构建新主键值。
	 * @param tableName -- 表名
	 * @param mpData -- 新增的数据内容
	 * @return 返回新增记录ID
	 */
	public static String insert(String tableName, Map<String,String> mpData) {
		String retKey = "";
		if (tableName == null || tableName.length() == 0) {
			_log.showError("insert param tableName is null! ");
			return retKey;
		}
		
		if (mpData == null || mpData.isEmpty()) {
			_log.showError("insert param mpData is null! ");
			return retKey;
		}
		
		//取表的字段名与数据类型集
		Map<String,String> mpParamType = DmDaoUtil.getParamType(tableName);
		if (mpParamType == null || mpParamType.isEmpty()) {
			_log.showError("insert mpParamType is null! ");
			return retKey;
		}
		
		//取主键字段名
		String keyField = DmDaoUtil.getKeyField(tableName);
		if (keyField == null || keyField.length() == 0) {
			_log.showError("insert keyField is null! ");
			return retKey;
		}
		
		//构建新主键值
		String keyValue = KeyCreator.getInstance().createKey(tableName);
		mpData.put(keyField, keyValue);
		
		//创建参数对象
		DaoParam param = _dao.createParam();
		
		//构建新增SQL
		StringBuilder sbsql = new StringBuilder("insert into ");
		sbsql.append(tableName);
		sbsql.append(" (");
		
		//保存?标记
		StringBuilder sbflag = new StringBuilder();
		
		Iterator<String> keyItr = mpData.keySet().iterator();
		while(keyItr.hasNext()) {
			String fieldName = keyItr.next();
			String fieldValue = mpData.get(fieldName);
			//构建新增SQL
			sbsql.append(fieldName + ", ");
			sbflag.append("?, ");
			//添加数据参数
			param.addValue(fieldValue);
			param.addType(mpParamType.get(fieldName));
		}
		
		//取新增SQL，格式：insert into tablename (field1, field2 ...
		String sql = sbsql.substring(0, sbsql.length()-2);
		//取新增标记SQL，格式：?, ?, ? ...
		String flag = sbflag.substring(0, sbflag.length()-2);
		
		//组合新增SQL
		sql += ") values (" + flag + ")";
		
		_log.showDebug("dmdao.insert sql=" + sql);
		_log.showDebug("dmdao.insert value=" + param.getValue());
		_log.showDebug("dmdao.insert type=" + param.getType());
		
		//给新增参数对象添加SQL
		param.setSql(sql);
		
		if (!_dao.update(param)) return retKey;
		
		return keyValue;
	}
	
	/**
	 * 复制指定表的记录：
	 * 先找出要复制的记录值，再新增记录，需要的数据模型信息从数据模型中取。
	 * @param tableName -- 表名
	 * @param keyId -- 被复制的记录ID
	 * @return 返回复制后的新记录ID
	 */
	public static String copy(String tableName, String keyId) {
		return copy(tableName, keyId, null);
	}
	
	/**
	 * 复制指定表的记录：
	 * 先找出要复制的记录值，然后替换需要修改的值，再新增记录;
	 * 需要的数据模型信息从数据模型中取。
	 * @param tableName -- 表名
	 * @param keyId -- 被复制的记录ID
	 * @param repData -- 需要替换的记录值
	 * @return 返回复制后的新记录ID
	 */
	public static String copy(String tableName, String keyId, Map<String,String> repData) {
		String retKey = "";
		if (tableName == null || tableName.length() == 0 || 
				keyId == null || keyId.length() == 0) {
			_log.showError("copy param tableName or keyId is null! ");
			return retKey;
		}
		
		String keyField = DmDaoUtil.getKeyField(tableName);
		if (keyField == null || keyField.length() == 0) {
			_log.showError("copy keyField is null! ");
			return retKey;
		}
		
		Map<String,String> mpData = queryMap(tableName, keyId);
		
		//替换要复制的数据
		if (repData != null && !repData.isEmpty()) {
			Iterator<String> keyItr = repData.keySet().iterator();
			while(keyItr.hasNext()) {
				String fieldName = keyItr.next();
				String fieldValue = repData.get(fieldName);
				mpData.put(fieldName, fieldValue);
			}
		}
		
		return insert(tableName, mpData);
	}
	
	/**
	 * 根据主键值删除指定表记录
	 * @param tableName -- 表名
	 * @param keyId -- 主键值
	 * @return
	 */
	public boolean delete(String tableName, String keyId) {
		if (tableName == null || tableName.length() == 0 || 
				keyId == null || keyId.length() == 0) {
			_log.showError("delete param tableName or keyId is null! ");
			return false;
		}
		
		String keyField = DmDaoUtil.getKeyField(tableName);
		if (keyField == null || keyField.length() == 0) {
			_log.showError("delete keyField is null! ");
			return false;
		}
		
		String sql = "delete from "+ tableName +" where "+ keyField +" = ?";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(keyId);
		
		return _dao.update(param);
	}
	
	/**
	 * 根据过滤语句删除指定表的记录值。
	 * @param tableName -- 表名
	 * @param where -- 过滤语句，不含where关键字
	 * @return
	 */
	public static boolean deleteByWhere(String tableName, String where) {
		if (tableName == null || tableName.length() == 0 || 
				where == null || where.length() == 0) {
			_log.showError("deleteByWhere param tableName or where is null! ");
			return false;
		}
		
		String sql = "delete from "+ tableName +" where "+ where;
		DaoParam param = _dao.createParam(sql);
		
		return _dao.update(param);
	}
}
