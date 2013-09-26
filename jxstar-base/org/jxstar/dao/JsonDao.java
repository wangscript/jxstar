/*
 * JsonHandler.java 2009-6-10
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


import org.jxstar.dao.transaction.TransactionManager;
import org.jxstar.dao.transaction.TransactionObject;
import org.jxstar.util.factory.SystemFactory;
import org.jxstar.util.log.Log;

/**
 * 通过查询语句生成JSON对象。
 *
 * @author TonyTan
 * @version 1.0, 2009-6-10
 */
public class JsonDao {
	private static JsonDao _instance = null;
	//日志对象
	private static Log _log = Log.getInstance();
	//事务管理对象
	private static TransactionManager _tranMng = null;
	
	private JsonDao() {
		_tranMng = (TransactionManager) SystemFactory.
							createSystemObject("TransactionManager");
		if (_tranMng == null) {
			_log.showWarn("TransactionManager Object create failded! ");
		}
	}
	
	/**
	 * 采用单例模式
	 * @return
	 */
	public static synchronized JsonDao getInstance() {
		if (_instance == null) {
			_instance = new JsonDao();
		}
		
		return _instance;
	}
	
	/**
	 * 查询结果构建为JSON对象
	 * 
	 * @param param - 查询的参数对象
	 * @param cols - 带表名的字段名数组
	 * @return 返回数据格式为：{},{},{}，如果没有记录则返回null
	 */
	public String query(DaoParam param, String[] cols) {
		String retJson = null;
		//判断参数是否有效
		if (param == null) {
			_log.showWarn("query(): daoparam is not validity! ");
			return retJson;
		}
		String sql = param.getSql();
		if (sql == null || sql.length() < 10) {
			_log.showWarn("query(): sql param is not validity! ");
			return retJson;
		}
		
		List<String> lsType = param.getType();
		List<String> lsValue = param.getValue();
		if (lsType.size() != lsValue.size()) {
			_log.showWarn("query(): param is not validity! ");
			return retJson;
		}
		
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		TransactionObject tranObj = null;
		try {
			tranObj = _tranMng.getTransactionObject();
			con = tranObj.getConnection(param.getDsName());
			
			if (con == null) {
				_log.showWarn("connection is null.");
				return null;
			}
			
			ps = con.prepareStatement(sql);
			ps = DaoUtil.setPreStmParams(lsValue, lsType, ps);

			long curTime = System.currentTimeMillis();
			rs = ps.executeQuery();
			DaoUtil.showQueryTime(curTime, param);
			
			List<String> hcs = param.getHideCols();
			//结果集转换为Json对象字符串
			retJson = DaoUtil.getRsToJson(rs, cols, hcs);
			
			//由于查询操作的connection需要释放，所以执行提交操作
			tranObj.commit();
		} catch(SQLException e) {
			DaoUtil.closeTranObj(tranObj);
			DaoUtil.showException(e, param);
			return null;
		} catch (Exception e) {
			DaoUtil.closeTranObj(tranObj);
			DaoUtil.showException(e, param);
			return null;
		} finally {
			try {
				if (rs != null) rs.close();
				rs = null;
				if (ps != null) ps.close();
				ps = null;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return retJson;
	}
}
