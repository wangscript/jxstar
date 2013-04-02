/*
 * DmUtil.java 2010-12-17
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.dm.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jxstar.dao.BaseDao;
import org.jxstar.dao.DaoParam;
import org.jxstar.dao.pool.DataSourceConfig;
import org.jxstar.dao.pool.DataSourceConfigManager;
import org.jxstar.dao.pool.PooledConnection;
import org.jxstar.dm.DmException;
import org.jxstar.util.log.Log;

/**
 * 数据库配置的常用工具类。
 *
 * @author TonyTan
 * @version 1.0, 2010-12-17
 */
public class DmUtil {
	//日志对象
	private static Log _log = Log.getInstance();
	
	/**
	 * 检查表名是否存在
	 * @param tableName -- 表名
	 * @param dsName -- 数据源
	 * @return
	 */
	public static boolean existTable(String tableName, String dsName) {
		BaseDao _dao = BaseDao.getInstance();
		
		String sql = "select count(*) as cnt from v_table_info where table_name = ?";
		DaoParam param = _dao.createParam(sql);
		
		param.addStringValue(tableName.toLowerCase());
		Map<String,String> mpCnt = _dao.queryMap(param);
		
		return !(mpCnt.get("cnt").equals("0"));
	}

	/**
	 * 执行数据库更新操作。
	 * @param lssql -- 更新SQL数组
	 * @param dsName -- 数据源
	 * @return
	 */
	public static boolean executeSQL(List<String> lssql, String dsName) throws DmException {
		//判断参数是否有效
		if (lssql == null || lssql.isEmpty()) {
			_log.showWarn("execute sql param is null! ");
			return false;
		}
		if (dsName == null || dsName.length() == 0) {
			dsName = DataSourceConfig.getDefaultName();
		}
		
		Statement stmt = null;
		Connection conn = null;
		try {
			conn = PooledConnection.getInstance().getConnection(dsName);
			if (conn == null){
				_log.showWarn("connection is null sql=" + lssql.toString());
				return false;
			}
			conn.setAutoCommit(true);
			
			stmt = conn.createStatement();
			
			for (int i = 0, n = lssql.size(); i < n; i++) {
				String sql = lssql.get(i).trim();
				if (sql.length() == 0) continue;
				
				//去掉注释语句
				if (sql.charAt(0) == '-') continue;
				
				//去掉sql中最后的;符号
				if (sql.charAt(sql.length()-1) == ';') {
					sql = sql.substring(0, sql.length()-1);
				}
				
				_log.showDebug("----batch exe sql=" + sql);
				stmt.addBatch(sql);
			}
			
		    stmt.executeBatch();
		} catch (SQLException e) {
			_log.showError(e);
			throw new DmException(e.getMessage());
		} finally {
			try {
				if (stmt != null) stmt.close();
				stmt = null;
				
				if (conn != null) conn.close();
				conn = null;
			} catch (SQLException e) {
				_log.showError(e);
			}
		}
		
		return true;
	}
	
	/**
	 * 取数据库名称
	 * @param dsName -- 数据源名
	 * @return
	 */
	public static String getDbSchema(String dsName) {
		DataSourceConfigManager dscm = DataSourceConfigManager.getInstance();
		DataSourceConfig dsc = dscm.getDataSourceConfig(dsName);
		
		return dsc.getSchemaName();
	}
	
	/**
	 * 去掉SQL中的注释，注释的格式是：--sql \r\n
	 * @param sql
	 * @return
	 */
	public static String parseSql(String sql) {
		//注释匹配，中间不含换行符
		String regex = "--[^\n]+\n";
		
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(sql);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			//String tag = m.group();
			m.appendReplacement(sb, "");
		}
		m.appendTail(sb);
		
		return sb.toString();
	}
	
	/**
	 * 判断字符串两头是否有单引号括住，如：'and'
	 * @param value
	 * @return
	 */
	public static boolean hasYinHao(String value) {
		if (value == null || value.length() < 3) return false;
		
		if ((value.charAt(0) == (char)39) && (value.charAt(value.length()-1) == (char)39)) {
			return true;
		}
		
		return false;
	}
}
