/*
 * BaseDao.java 2009-5-28
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
import java.util.Map;

import org.jxstar.dao.pool.PooledConnection;
import org.jxstar.dao.transaction.TransactionException;
import org.jxstar.dao.transaction.TransactionManager;
import org.jxstar.dao.transaction.TransactionObject;
import org.jxstar.util.factory.FactoryUtil;
import org.jxstar.util.factory.SystemFactory;
import org.jxstar.util.log.Log;

/**
 * 框架实现的数据库访问对象，在业务对象采用单列模式。
 * 
 * 集成了所有与数据库交互的接口。
 * 返回的MAP对象中的KEY值都是小写。
 * 如果访问数据出错，返回的集合对象isEmpty()为真s。
 * 
 * update操作默认是自动提交，如果用TransactionManager设置事务标示，
 * 则默认支持事务。
 * 
 * BO对象中的常见用法是：
 * DaoParam param = _dao.createParam(sql);
 * param.addStringValue(value);
 * ...
 * _dao.update(param);
 * 或者
 * _dao.query(param);
 * 
 * @author TonyTan
 * @version 1.0, 2009-5-28
 */
public class BaseDao {
	private static BaseDao _instance = null;
	//日志对象
	private static Log _log = Log.getInstance();
	//事务管理对象
	private static TransactionManager _tranMng = null;
	
	private BaseDao() {
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
	public static synchronized BaseDao getInstance() {
		if (_instance == null) {
			_instance = new BaseDao();
		}
		
		return _instance;
	}
	
	/**
	 * 创建DAO参数对象
	 * @return
	 */
	public DaoParam createParam() {
		return new DaoParam();
	}
	
	/**
	 * 创建DAO参数对象
	 * @param sql
	 * @return
	 */
	public DaoParam createParam(String sql) {
		DaoParam param = new DaoParam();
		param.setSql(sql);
		
		return param;
	}
	
	/**
	 * 更新操作
	 * 
	 * @param param - 更新的参数对象
	 * @return boolean
	 */
	public boolean update(DaoParam param) {
		boolean ret = false;
		if (param == null) param = new DaoParam();
		
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
		
		//根据配置输出SQL
		DaoUtil.debugSQL(param, "2");
		//取数据源名
		String dataSource = param.getDsName();
		
		Connection con = null;
		PreparedStatement ps = null;
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

			ps = con.prepareStatement(sql);
			if (!lsValue.isEmpty()) {
				ps = DaoUtil.setPreStmParams(lsValue, lsType, ps);
			}
			
			long curTime = System.currentTimeMillis();
			int iret = ps.executeUpdate();
			if (iret >= 0) ret = true;
			
			//公共事务数据库更新才需要提交
			if (param.isUseTransaction()) {
				if (iret >= 0) {
					tranObj.commit();
				} else {
					tranObj.rollback();
				}
			}
			
			DaoUtil.showUpdateTime(curTime, sql);
		} catch(TransactionException e) {
			DaoUtil.closeTranObj(tranObj);
			DaoUtil.showException(e, sql);
			return false;
		} catch (SQLException e) {
			DaoUtil.closeTranObj(tranObj);
			DaoUtil.showException(e, sql);
			return false;
		} catch (Exception e) {
			DaoUtil.closeTranObj(tranObj);
			DaoUtil.showException(e, sql);
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
	
	/**
	 * 查询多条数据
	 * 
	 * @param param - 查询的参数对象
	 * @return List<Map<String,String>>			
	 */
	public List<Map<String,String>> query(DaoParam param) {
		return query(param, -1);
	}
	
	/**
	 * 查询一条数据
	 * 
	 * @param param - 查询的参数对象
	 * @return Map<String,String>
	 */
	public Map<String,String> queryMap(DaoParam param) {
		List<Map<String,String>> lsRet = query(param, 1);
		
		if (lsRet == null || lsRet.isEmpty()) {
			return FactoryUtil.newMap();
		}
		
		return lsRet.get(0);
	}

	/**
	 * 查询多条数据
	 * 
	 * @param param - 查询的参数对象
	 * @param recNum -- 控制取recNum条记录，如果<=0，则不限制数量
	 * @return List<Map<String,String>>			
	 */
	private List<Map<String,String>> query(DaoParam param, int recNum) {
		List<Map<String,String>> lsRet = FactoryUtil.newList();
		
		if (param == null) param = new DaoParam();
		
		String sql = param.getSql();
		//判断参数是否有效
		if (sql == null || sql.length() < 10) {
			_log.showWarn("query sql param is null! ");
			return lsRet;
		}	
		
		List<String> lsType = param.getType();
		List<String> lsValue = param.getValue();
		if (lsType.size() != lsValue.size()) {
			_log.showWarn("query type and value size differ! ");
			return lsRet;
		}
		
		//根据配置输出SQL
		DaoUtil.debugSQL(param, "1");
		//取数据源名
		String dataSource = param.getDsName();

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		TransactionObject tranObj = null;
		try {
			if (param.isUseTransaction()) {
				tranObj = _tranMng.getTransactionObject();
				con = tranObj.getConnection(dataSource);
			} else {
				con = PooledConnection.getInstance().getConnection(dataSource);
				if (con != null) con.setAutoCommit(true);
			}
			
			if (con == null) {
				_log.showWarn("connection is null sql=" + sql);
				return lsRet;
			}
			
			ps = con.prepareStatement(sql);
			if (!lsValue.isEmpty()) {
				ps = DaoUtil.setPreStmParams(lsValue, lsType, ps);
			}

			long curTime = System.currentTimeMillis();
			rs = ps.executeQuery();
			DaoUtil.showQueryTime(curTime, sql);
			
			//结果集转换为List对象
			lsRet = DaoUtil.getRsToList(rs, recNum);
			
			//如果不执行提交方法，在非事务环境中会存在连接泄露
			if (param.isUseTransaction()) {
				tranObj.commit();
			}
		} catch(SQLException e) {
			DaoUtil.closeTranObj(tranObj);
			DaoUtil.showException(e, sql);
			return lsRet;
		} catch (Exception e) {
			DaoUtil.closeTranObj(tranObj);
			DaoUtil.showException(e, sql);
			return lsRet;
		} finally {
			try {
				if (rs != null) rs.close();
				rs = null;
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

		return lsRet;
	}
}
