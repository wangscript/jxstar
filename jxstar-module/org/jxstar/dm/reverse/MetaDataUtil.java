/*
 * MetaDataUtil.java 2010-12-23
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.dm.reverse;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.jxstar.dao.pool.PooledConnection;
import org.jxstar.dm.util.DmUtil;
import org.jxstar.util.factory.FactoryUtil;
import org.jxstar.util.log.Log;

/**
 * 读取数据库元数据的工具类。
 *
 * @author TonyTan
 * @version 1.0, 2010-12-23
 */
public class MetaDataUtil {
	//日志对象
	private static Log _log = Log.getInstance();
	
	/**
	 * 查询表对象
	 * @param tableName -- 表名
	 * @param dsName -- 数据源名
	 * @return
	 * @deprecated 直接从系统表中取值
	 */
	public static Map<String,String> getTableMeta(String tableName, String dsName) {
		Map<String,String> mpTable = FactoryUtil.newMap();
		String schema = DmUtil.getDbSchema(dsName);
		
		ResultSet rs = null;
		Connection conn = null;
		DatabaseMetaData dbmd = null;
		try {
			conn = PooledConnection.getInstance().getConnection(dsName);
			if (conn == null) return mpTable;
			
			dbmd = conn.getMetaData();
			//查询表对象
			rs = dbmd.getTables(null, schema.toUpperCase(), tableName.toUpperCase(), new String[]{"TABLE"});

			if (rs.next()) {
				String tableTitle = rs.getString("REMARKS");
				
				mpTable.put("table_name", tableName);
				mpTable.put("table_title", tableTitle);
				_log.showDebug("========" + mpTable.toString());
			}
		} catch (SQLException e) {
			_log.showError(e);
		} finally {
			try {
				if (rs != null) rs.close();
				rs = null;
				
				if (conn != null) conn.close();
				conn = null;
			} catch (SQLException e) {
				_log.showError(e);
			}
		}
		
		return mpTable;
	}
	
	/**
	 * 查询指定表的字段信息
	 * @param tableName -- 表名
	 * @param dsName -- 数据源名
	 * @return
	 * @deprecated 直接从系统表中取值
	 */
	public static List<Map<String,String>> getFieldMeta(String tableName, String dsName) {
		List<Map<String,String>> lsRet = FactoryUtil.newList();
		String schema = DmUtil.getDbSchema(dsName);
		
		ResultSet rs = null;
		Connection conn = null;
		DatabaseMetaData dbmd = null;
		try {
			conn = PooledConnection.getInstance().getConnection(dsName);
			if (conn == null) return lsRet;
			
			dbmd = conn.getMetaData();
			//查询指定表的字段信息
			rs = dbmd.getColumns(null, schema.toUpperCase(), tableName.toUpperCase(), null);

			while (rs.next()) {
				String fieldName = rs.getString("COLUMN_NAME");		//String 列名称 
				String dataType = rs.getString("DATA_TYPE");		//int 来自 java.sql.Types 的 SQL 类型
				String typeName = rs.getString("TYPE_NAME");		//String 数据源依赖的类型名称
				String dataSize = rs.getString("COLUMN_SIZE");		//int 列的大小。对于 char 或 date 类型，列的大小是最大字符数，对于 numeric 和 decimal 类型，列的大小就是精度。
				String precision = rs.getString("DECIMAL_DIGITS");	//int 小数部分的位数 
				String nullable = rs.getString("IS_NULLABLE");		//String "NO" 表示明确不允许列使用 NULL 值，"YES" 表示可能允许列使用 NULL 值。
				//String defaultValue = rs.getString("COLUMN_DEF");		//String 默认值（可为 null）
				String fieldTitle = rs.getString("REMARKS");			//String 描述列的注释（可为 null）
				
				Map<String,String> mpField = FactoryUtil.newMap();
				mpField.put("field_name", fieldName);
				mpField.put("field_title", fieldTitle);
				mpField.put("data_type", dataType);
				mpField.put("type_name", typeName);
				mpField.put("data_size", dataSize);
				mpField.put("precision", precision);
				mpField.put("nullable", nullable);
				//mpField.put("default_value", defaultValue);
				_log.showDebug("========" + mpField.toString());
				
				lsRet.add(mpField);
			}
		} catch (SQLException e) {
			_log.showError(e);
		} finally {
			try {
				if (rs != null) rs.close();
				rs = null;
				
				if (conn != null) conn.close();
				conn = null;
			} catch (SQLException e) {
				_log.showError(e);
			}
		}
		
		return lsRet;
	}
	
	/**
	 * 查询表的主键信息
	 * @param tableName -- 表名
	 * @param dsName -- 数据源名
	 * @return
	 */
	public static Map<String,String> getKeyMeta(String tableName, String dsName) {
		Map<String,String> mpKey = FactoryUtil.newMap();
		String schema = DmUtil.getDbSchema(dsName);
		
		ResultSet rs = null;
		Connection conn = null;
		DatabaseMetaData dbmd = null;
		try {
			conn = PooledConnection.getInstance().getConnection(dsName);
			if (conn == null) return mpKey;
			
			dbmd = conn.getMetaData();
			//查询表的主键信息
			rs = dbmd.getPrimaryKeys(null, schema.toUpperCase(), tableName.toUpperCase());

			if (rs.next()) {
				String keyName = rs.getString("PK_NAME");
				keyName = keyName == null ? "" : keyName;
				String keyField = rs.getString("COLUMN_NAME");
				keyField = keyField == null ? "" : keyField;
				
				mpKey.put("key_name", keyName.toUpperCase());
				mpKey.put("key_field", keyField.toLowerCase());
				//_log.showDebug("========" + mpKey.toString());
			}
		} catch (SQLException e) {
			_log.showError(e);
		} finally {
			try {
				if (rs != null) rs.close();
				rs = null;
				
				if (conn != null) conn.close();
				conn = null;
			} catch (SQLException e) {
				_log.showError(e);
			}
		}
		
		return mpKey;
	}
	
	/**
	 * 查询指定表的索引信息，不含主键
	 * @param tableName -- 表名
	 * @param dsName -- 数据源名
	 * @return
	 */
	public static List<Map<String,String>> getIndexInfo(String tableName, String dsName) {
		List<Map<String,String>> lsRet = FactoryUtil.newList();
		String schema = DmUtil.getDbSchema(dsName);
		
		ResultSet rs = null;
		Connection conn = null;
		DatabaseMetaData dbmd = null;
		try {
			conn = PooledConnection.getInstance().getConnection(dsName);
			if (conn == null) return lsRet;
			
			//取该表的主键名
			String keyName = "";
			Map<String,String> mpKey = getKeyMeta(tableName, dsName);
			if (!mpKey.isEmpty()) {
				keyName = mpKey.get("key_name");
			}
			
			dbmd = conn.getMetaData();
			//查询指定表的索引信息
			rs = dbmd.getIndexInfo(null, schema.toUpperCase(), tableName.toUpperCase(), false, false);

			while (rs.next()) {
				String indexName = rs.getString("INDEX_NAME");			//String => 索引名称；
				indexName = indexName == null ? "" : indexName;
				String indexField = rs.getString("COLUMN_NAME");		//String => 列名称；
				indexField = indexField == null ? "" : indexField;
				String position = rs.getString("ORDINAL_POSITION");		//short => 索引中的列序列号；
				position = position == null ? "" : position;
				String nonunique = rs.getString("NON_UNIQUE");			//boolean => 索引值是否可以不惟一 
				nonunique = nonunique == null ? "" : nonunique;
				
				//为空不处理
				if (indexName.length() == 0) continue;
				//小于1的不处理
				if (position.equals("0")) continue;
				//主键不处理
				if (keyName.equalsIgnoreCase(indexName)) continue;
				
				Map<String,String> mpIndex = FactoryUtil.newMap();
				mpIndex.put("index_name", indexName.toUpperCase());
				mpIndex.put("column_name", indexField.toLowerCase());
				mpIndex.put("column_position", position);
				mpIndex.put("isunique", getIsUnique(nonunique));

				lsRet.add(mpIndex);
			}
		} catch (SQLException e) {
			_log.showError(e);
		} finally {
			try {
				if (rs != null) rs.close();
				rs = null;
				
				if (conn != null) conn.close();
				conn = null;
			} catch (SQLException e) {
				_log.showError(e);
			}
		}
		
		return lsRet;
	}
	
	/**
	 * 取是否唯一索引
	 * @param non -- 不唯一
	 * @return
	 */
	private static String getIsUnique(String non) {
		if (non == null) return "0";
		
		non = non.toLowerCase();
		String isunique = "0";
		
		if (non.equals("false") || non.equals("true")) {
			if (non.equals("false")) {
				isunique = "1";
			} else {
				isunique = "0";
			}
		} else {
			if (non.equals("1") || non.equals("0")) {
				if (non.equals("1")) {
					isunique = "0";
				} else {
					isunique = "1";
				}
			}
		}
		
		return isunique;
	}
}
