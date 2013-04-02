/*
 * KeyCreator.java 2010-11-4
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.util.key;

import java.util.Map;


import org.jxstar.dao.BaseDao;
import org.jxstar.dao.DaoParam;
import org.jxstar.util.config.SystemConfig;
import org.jxstar.util.factory.FactoryUtil;
import org.jxstar.util.log.Log;


/**
 * 主键编码规则为：前缀+3位随机数字+流水号
 * 前缀 -- 采用项目代号；
 * 3位随机号 -- 为保证主键值唯一
 * 流水号 -- 采用唯一的数字；
 * 
 * 主键序号生成设计思想与CodeCreator编码序号相同。
 * 
 * 为避免在集群环境下造成主键重复与死锁的情况，改为每次更新POOL_SIZE值。
 * 
 * @author TonyTan
 * @version 1.0, 2010-11-4
 */
public class KeyCreator {
	private static KeyCreator _instance = new KeyCreator();
	
	private static Log _log = Log.getInstance();
	private static BaseDao _dao = BaseDao.getInstance();

	//每次取主键数量
	private static final int POOL_SIZE = 50;
	//缓存不同表的键对象
	private static Map<String, KeyInfo> _keyList = FactoryUtil.newMap();
	
	private KeyCreator() {}
	
	public static KeyCreator getInstance() {
		return _instance;
	}
	
	/**
	 * 创建一数据表的主键值.
	 * 
	 * @param tableName - 数据表名
	 * @return
	 */
	public synchronized String createKey(String tableName) {
		return getKeyPrefix() + getRandom() + createSerialNo(tableName);
	}
	
	/**
	 * 创建新的树型记录ID
	 * @param parentID
	 * @param level
	 * @param tableName
	 * @param pkField
	 * @param levelCol
	 * @param dsName
	 * @return
	 */
	public synchronized String createTreeKey(String parentID, int level,
			 String tableName, String pkField, String levelCol, String dsName) {
		if (parentID == null) parentID = "";

		StringBuilder selsql = new StringBuilder();
		StringBuilder wheresql = new StringBuilder();
		selsql.append(" select max(" + pkField + ") as maxval, count(" + pkField + ") as cnt ");
		wheresql.append(" from " + tableName );
		if (parentID.length() == 0) {
			wheresql.append(" where " + pkField + " like '%' ");
			wheresql.append(" and " + levelCol + " = " + level);
		} else {
			wheresql.append(" where " + pkField + " like '" + parentID + "%' ");
			wheresql.append(" and " + levelCol + " = " + level);
		}
		selsql.append(wheresql);

		_log.showDebug("treeid selectsql = " + selsql.toString());

		DaoParam param = _dao.createParam(selsql.toString());
		param.setDsName(dsName);
		Map<String,String> keyMap = _dao.queryMap(param);
		
		String maxVal = keyMap.get("maxval");
		if (maxVal.length() == 0) {
			if (parentID.length() == 0) {
				return "1001";
			} else {
				//本级别第一个节点
				return parentID + "0001";
			}
		} else {
			if (maxVal.equals("9999")) {
				_log.showWarn("treeid exceed 9999!");
				return "9999";
			}
		}
		if (maxVal.length() > 4) {
			maxVal = maxVal.substring(maxVal.length()-4, maxVal.length());
		}
		maxVal = "0000" + (Long.parseLong(maxVal) + 1);
		String treeID = parentID + maxVal.substring(maxVal.length()-4, maxVal.length());
		
		return treeID;
	}
	
	/**
	 * 取配置文件中的项目代号为主键前缀
	 * @return
	 */
	private String getKeyPrefix() {
		String prefix = SystemConfig.getConfigByKey("ServerConfigs", "projectcode");
		if (prefix.length() == 0) prefix = "dh";
		return prefix;
	}
	
	/**
	 * 取主键的序号
	 * @return
	 */
	private String createSerialNo(String tableName) {
		KeyInfo keyInfo;
		
		tableName = tableName.toLowerCase();
		if (_keyList.containsKey(tableName)) {
			keyInfo = _keyList.get(tableName);
		} else {
			//如果缓存对象数量超过1000，则清除所有缓存对象
			if (_keyList.size() > 1000) {
				_keyList.clear();
			}
			
			keyInfo = new KeyInfo(POOL_SIZE, tableName);
			_keyList.put(tableName, keyInfo);
		}
		
		//取新的序号
		int serial = keyInfo.getNextKey();
		return Integer.toString(serial);
	}
	
	/**
	 * 为确保主键万无一失，每个主键前加一个3位随机数
	 * @return
	 */
	private String getRandom() {
		int random =  (int) (Math.random() * 1000);
		StringBuilder fix = new StringBuilder(Integer.toString(random));
		
		int len = 3 - fix.length();
		for (int i = 0; i < len; fix.insert(0, '0'), i++);
		
		return fix.toString();
	}
	
	/**
	 * 主键唯一序号生成类
	 * 因为系统采用每次取值后就更新最大值的方式，如果两个会话的同一个表名都同时更新
	 * sys_tableid的值，会造成死锁，所以添加uparam.setUseTransaction(false);取独立
	 * 的连接更新最大，可以避免sys_tableid表死锁。
	 * 
	 * @author TonyTan
	 * @version 1.0, 2010-11-2
	 */
	private class KeyInfo {
		private int keyMax;
		private int keyMin;
		private int nextKey;
		private int poolSize;
		private String keyName;
		
		public KeyInfo(int poolSize, String keyName) {
			this.poolSize = poolSize;
			this.keyName = keyName;
			
			retrieveFromDB();
		}
		
		public int getNextKey() {
			if (nextKey > keyMax) {
				retrieveFromDB();
			}
			
			return nextKey++;
		}
		
		private synchronized void retrieveFromDB() {
			//从数据库中取上次分配的最大值
			int dbmax = 0;
			String ssql = "select max_value from sys_tableid where table_name = ?";
			
			DaoParam sparam = _dao.createParam(ssql);
			sparam.addStringValue(keyName);
			Map<String, String> mpMax = _dao.queryMap(sparam);
			
			if (!mpMax.isEmpty()) {
				dbmax = Integer.parseInt(mpMax.get("max_value"));
				
				//每次取新号时，更新当前最大值；采用累加poolSize的方式可以解决多线程多事务累加不丢失的问题。
				String usql = "update sys_tableid set max_value = max_value + ? where table_name = ?";
				DaoParam uparam = _dao.createParam(usql);
				uparam.setUseTransaction(false);
				uparam.addIntValue(Integer.toString(poolSize));
				uparam.addStringValue(keyName);
				if (!_dao.update(uparam)) {
					_log.showWarn("get next code no update error!! tablename={0}!!", keyName);
				}
			} else {
				//新建一条记录
				String usql = "insert into sys_tableid(max_value, table_name) values(?, ?)";
				DaoParam uparam = _dao.createParam(usql);
				uparam.setUseTransaction(false);
				uparam.addIntValue(Integer.toString(poolSize));
				uparam.addStringValue(keyName);
				if (!_dao.update(uparam)) {
					_log.showWarn("get next keyid no update error!! tablename={0}!!", keyName);
				}
			}
			
			keyMax = dbmax + poolSize;
			keyMin = dbmax + 1;
			nextKey = keyMin;
		}
	}
}
