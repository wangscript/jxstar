/*
 * DataSourceConfigManager.java 2009-5-28
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.dao.pool;

import java.util.List;
import java.util.Map;

import org.jxstar.util.MapUtil;
import org.jxstar.util.factory.FactoryUtil;
import org.jxstar.util.log.Log;

/**
 * 数据源配置对象集。
 * 
 * @author TonyTan
 * @version 1.0, 2009-5-28
 */
public class DataSourceConfigManager {
	private static DataSourceConfigManager _dscMng = new DataSourceConfigManager();
	//数据源配置对象集
	private static Map<String,Object> _dsConfigMap = FactoryUtil.newMap();
	//日志对象
	private static Log _log = Log.getInstance();
	
	private DataSourceConfigManager() {}
	
	/**
	 * 采用单例模式。
	 * @return
	 */
	public static DataSourceConfigManager getInstance() {
		return _dscMng;
	}	
	
	/**
	 * 初始化数据源配置管理器，读取数据源配置信息。
	 */
	public void init(List<Map<String,String>> lsDataSource) {
		if (lsDataSource == null || lsDataSource.isEmpty()) {
			_log.showWarn("data source object list is null! ");
			return;
		}
		
		for (int i = 0; i < lsDataSource.size(); i++) {
			Map<String,String> mpDs = lsDataSource.get(i);
			if (mpDs == null || mpDs.isEmpty()) {
				_log.showWarn("data source object is null! ");
				return;
			}
			
			DataSourceConfig dsc = new DataSourceConfig();
			dsc.setDataSourceName(mpDs.get("sourcename"));
			dsc.setSchemaName(mpDs.get("schemaname"));
			dsc.setDriverClass(mpDs.get("driverclass"));
			dsc.setJdbcUrl(mpDs.get("jdbcurl"));
			dsc.setUserName(mpDs.get("username"));
			dsc.setPassWord(mpDs.get("password"));
			dsc.setMaxConNum(mpDs.get("maxconnum"));
			dsc.setMaxWaitTime(mpDs.get("maxwaittime"));
			dsc.setTranLevel(mpDs.get("tranlevel"));
			dsc.setDataSourceType(mpDs.get("datasourcetype"));
			dsc.setJndiName(mpDs.get("jndiname"));	
			dsc.setDbmsType(mpDs.get("dbmstype"));
			dsc.setValidIdle(MapUtil.getValue(mpDs,  "valididle"));
			dsc.setValidTest(MapUtil.getValue(mpDs,  "validtest"));
			dsc.setValidQuery(MapUtil.getValue(mpDs, "validquery"));
			//_log.showDebug("数据源: " + dsc.toString());
			
			_dsConfigMap.put(dsc.getDataSourceName(), dsc);
		}
	}
	
	/**
	 * 根据数据源名取数据源配置对象.
	 * 
	 * @param sDataSourceName -- 数据源名
	 * @return
	 */
	public DataSourceConfig getDataSourceConfig(String sDataSourceName) {
		return (DataSourceConfig) _dsConfigMap.get(sDataSourceName);
	}
	
	/**
	 * 外部添加数据源定义信息
	 * @param sourceName -- 数据源名
	 * @param schemaName -- 数据库名
	 * @param driveClass -- 驱动类名
	 * @param jdbcUrl -- 连接描述
	 * @param userName -- 用户名
	 * @param passWord -- 密码
	 * @param dbmsType -- 数据库类型
	 */
	public void addDataSourceConfig(String sourceName, 
			String schemaName, 
			String driveClass, 
			String jdbcUrl, 
			String userName, 
			String passWord, 
			String dbmsType) {
		if (_dsConfigMap.containsKey(sourceName)) {
			//删除数据源定义信息
			_dsConfigMap.remove(sourceName);
			//删除数据连接
			PooledConnection.getInstance().delConnection(sourceName);
		}
		
		DataSourceConfig dsc = new DataSourceConfig();
		dsc.setDataSourceName(sourceName);
		dsc.setSchemaName(schemaName);
		dsc.setDriverClass(driveClass);
		dsc.setJdbcUrl(jdbcUrl);
		dsc.setUserName(userName);
		dsc.setPassWord(passWord);
		dsc.setDbmsType(dbmsType);
		
		_dsConfigMap.put(dsc.getDataSourceName(), dsc);
	}
	
	/**
	 * 添加外部数据源信息
	 * @param dsc
	 */
	public void addDataSourceConfig(DataSourceConfig dsc) {
		if (dsc == null) return;
		String sourceName = dsc.getDataSourceName();
		
		if (_dsConfigMap.containsKey(sourceName)) {
			//删除数据源定义信息
			_dsConfigMap.remove(sourceName);
			//删除数据连接
			PooledConnection.getInstance().delConnection(sourceName);
		} else {
			_dsConfigMap.put(sourceName, dsc);
		}
	}
}
