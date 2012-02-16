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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.jxstar.dao.pool.PooledConnection;
import org.jxstar.dm.DmException;
import org.jxstar.dm.util.DmUtil;
import org.jxstar.util.factory.FactoryUtil;
import org.jxstar.util.log.Log;
import org.jxstar.util.resource.JsMessage;

/**
 * 读取数据库元数据的工具类。
 *
 * @author TonyTan
 * @version 1.0, 2010-12-23
 * @deprecated 直接从不同数据库的系统表中取值
 */
public class MetaDataUtil {
	//日志对象
	private static Log _log = Log.getInstance();
	
	/**
	 * 查询所有表对象
	 * @param dsName -- 数据源名
	 * @return
	 * @throws DmException
	 */
	public static List<Map<String,String>> getTableMeta(String dsName) throws DmException {
		List<Map<String,String>> lsRet = FactoryUtil.newList();
		String schema = DmUtil.getDbSchema(dsName);
		
		ResultSet rs = null;
		Connection conn = null;
		DatabaseMetaData dbmd = null;
		try {
			conn = PooledConnection.getInstance().getConnection(dsName);
			if (conn == null){//"数据源【{0}】取数据库连接为空！"
				throw new DmException(JsMessage.getValue("metautil.connull", dsName));
			}
			
			dbmd = conn.getMetaData();
			//查询所有表对象
			rs = dbmd.getTables(null, schema.toUpperCase(), null, new String[]{"TABLE"});

			ResultSetMetaData rsmd = rs.getMetaData();
			int fcnt = rsmd.getColumnCount();
			
			while (rs.next()) {
				for (int i = 1; i <= fcnt; i++) {
					String name = rsmd.getColumnName(i);
					String value = rs.getString(name);
					System.out.print(name + "=" + value + ";");
				}
				System.out.println("");
				
				String tableName = rs.getString("TABLE_NAME");
				String tableTitle = rs.getString("REMARKS");
				
				Map<String,String> mpTable = FactoryUtil.newMap();
				mpTable.put("table_name", tableName);
				mpTable.put("table_title", tableTitle);
				//_log.showDebug("========" + mpTable.toString());
				
				lsRet.add(mpTable);
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
	 * 查询指定表的字段信息
	 * @param tableName -- 表名
	 * @param dsName -- 数据源名
	 * @return
	 * @throws DmException
	 */
	public static List<Map<String,String>> getFieldMeta(String tableName, String dsName) throws DmException {
		List<Map<String,String>> lsRet = FactoryUtil.newList();
		String schema = DmUtil.getDbSchema(dsName);
		
		ResultSet rs = null;
		Connection conn = null;
		DatabaseMetaData dbmd = null;
		try {
			conn = PooledConnection.getInstance().getConnection(dsName);
			if (conn == null){
				throw new DmException(JsMessage.getValue("metautil.connull", dsName));
			}
			
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
	 * @throws DmException
	 */
	public static Map<String,String> getKeyMeta(String tableName, String dsName) throws DmException {
		Map<String,String> mpKey = FactoryUtil.newMap();
		String schema = DmUtil.getDbSchema(dsName);
		
		ResultSet rs = null;
		Connection conn = null;
		DatabaseMetaData dbmd = null;
		try {
			conn = PooledConnection.getInstance().getConnection(dsName);
			if (conn == null){
				throw new DmException(JsMessage.getValue("metautil.connull", dsName));
			}
			
			dbmd = conn.getMetaData();
			//查询表的主键信息
			rs = dbmd.getPrimaryKeys(null, schema.toUpperCase(), tableName.toUpperCase());

			if (rs.next()) {
				String keyName = rs.getString("PK_NAME");
				String keyField = rs.getString("COLUMN_NAME");
				
				mpKey.put("pk_name", keyName);
				mpKey.put("column_name", keyField);
				_log.showDebug("========" + mpKey.toString());
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
	 * 查询指定表的索引信息
	 * @param tableName -- 表名
	 * @param dsName -- 数据源名
	 * @return
	 * @throws DmException
	 */
	public static List<Map<String,String>> getIndexMeta(String tableName, String dsName) throws DmException {
		List<Map<String,String>> lsRet = FactoryUtil.newList();
		String schema = DmUtil.getDbSchema(dsName);
		
		ResultSet rs = null;
		Connection conn = null;
		DatabaseMetaData dbmd = null;
		try {
			conn = PooledConnection.getInstance().getConnection(dsName);
			if (conn == null){
				throw new DmException(JsMessage.getValue("metautil.connull", dsName));
			}
			
			dbmd = conn.getMetaData();
			//查询指定表的索引信息
			rs = dbmd.getIndexInfo(null, schema.toUpperCase(), tableName.toUpperCase(), false, false);

			while (rs.next()) {
				String indexName = rs.getString("INDEX_NAME");		//String => 索引名称；
				String indexField = rs.getString("COLUMN_NAME");		//String => 列名称；
				String position = rs.getString("ORDINAL_POSITION");		//short => 索引中的列序列号；
				String nonunique = rs.getString("NON_UNIQUE");	//boolean => 索引值是否可以不惟一 
				
				Map<String,String> mpIndex = FactoryUtil.newMap();
				mpIndex.put("index_name", indexName);
				mpIndex.put("index_field", indexField);
				mpIndex.put("position", position);
				mpIndex.put("non_unique", nonunique);
				_log.showDebug("========" + mpIndex.toString());

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
}
