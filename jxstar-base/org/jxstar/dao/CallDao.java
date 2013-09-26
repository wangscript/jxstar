/*
 * JsonHandler.java 2009-6-10
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.jxstar.dao.pool.PooledConnection;
import org.jxstar.dao.transaction.TransactionException;
import org.jxstar.dao.transaction.TransactionManager;
import org.jxstar.dao.transaction.TransactionObject;
import org.jxstar.util.factory.SystemFactory;
import org.jxstar.util.log.Log;

/**
 * 处理存储过程返回值。
 *
 * @author TonyTan
 * @version 1.0, 2009-6-10
 */
public class CallDao {
	private static CallDao _instance = null;
	//日志对象
	private static Log _log = Log.getInstance();
	//事务管理对象
	private static TransactionManager _tranMng = null;
	
	private CallDao() {
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
	public static synchronized CallDao getInstance() {
		if (_instance == null) {
			_instance = new CallDao();
		}
		
		return _instance;
	}
	
	/**
	 * 创建DAO参数对象
	 * @return
	 */
	public CallParam createParam() {
		return new CallParam();
	}
	
	/**
	 * 创建DAO参数对象
	 * @param sql
	 * @return
	 */
	public CallParam createParam(String sql) {
		CallParam param = new CallParam();
		param.setSql(sql);
		
		return param;
	}
	
	/**
	 * 执行存储过程。
	 * 
	 * @param param - 更新的参数对象
	 * @return boolean
	 */
	public boolean execute(CallParam param) {
		boolean ret = false;
		if (param == null) param = new CallParam();
		
		String sql = param.getSql();
		//判断参数是否有效
		if (sql == null || sql.length() < 10) {
			_log.showWarn("update sql param is null! ");
			return false;
		}
		
		List<String> lsType = param.getType();
		List<String> lsValue = param.getValue();
		if (lsType.size() != lsValue.size()) {
			_log.showWarn("update type and value size differ! ");
			return false;
		}
		//注册输出参数
		List<Integer> lsOutType = param.getOutType();
		
		//根据配置输出SQL
		DaoUtil.debugSQL(param, "2");
		//取数据源名
		String dataSource = param.getDsName();
		
		Connection con = null;
		CallableStatement ps = null;
		TransactionObject tranObj = null;
		try {
			//如果本操作不采用公共事务处理则直接从连接池中取连接
			if (param.isUseTransaction()) {
				tranObj = _tranMng.getTransactionObject();
				con = tranObj.getConnection(dataSource);
			} else {
				con = PooledConnection.getInstance().getConnection(dataSource);
				if (con != null) con.setAutoCommit(true);
			}
			
			if (con == null){
				_log.showWarn("connection is null sql=" + sql);
				return false;
			}

			ps = con.prepareCall(sql);
			if (!lsValue.isEmpty()) {
				DaoUtil.setPreStmParams(lsValue, lsType, ps);
			}
			if (!lsOutType.isEmpty()) {
				//输入参数的个数
				int start = lsType.size();
				DaoUtil.setStmOutParams(start, lsOutType, ps);
			}
			
			long curTime = System.currentTimeMillis();
			//不处理返回值，因为有时执行成功也会返回负数
			ps.executeUpdate();
			ret = true;
			
			//处理输出参数
			if (!lsOutType.isEmpty()) {
				//输入参数的个数
				int start = lsType.size();
				DaoUtil.getStmOutParams(start, lsOutType.size(), ps, param);
			}
			
			//公共事务数据库更新才需要提交
			if (param.isUseTransaction()) {
				tranObj.commit();
			}
			
			DaoUtil.showUpdateTime(curTime, param);
		} catch(TransactionException e) {
			DaoUtil.closeTranObj(tranObj);
			DaoUtil.showException(e, param);
			return false;
		} catch (SQLException e) {
			DaoUtil.closeTranObj(tranObj);
			DaoUtil.showException(e, param);
			return false;
		} catch (Exception e) {
			DaoUtil.closeTranObj(tranObj);
			DaoUtil.showException(e, param);
			return false;
		} finally {
			try {
				if (ps != null) ps.close();
				ps = null;
				
				if (!param.isUseTransaction()) {
					if (con != null) {
						con.close();
					}
					con = null;
				}
			} catch (SQLException e) {
				_log.showError(e);
			}
			
		}
		
		return ret;
	}
}
