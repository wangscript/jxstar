/*
 * DefineDataManger.java 2008-4-4
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.service.define;

import java.util.List;
import java.util.Map;

import org.jxstar.util.config.SystemVar;
import org.jxstar.util.factory.FactoryUtil;
import org.jxstar.util.log.Log;

/**
 * 读取功能定义数据，并根据系统参数fun.define.usepool判断，是否启用缓存；
 * 如果启用缓存，则第一次通过FunDefineDao从数据库中读取，以后都从缓存中读取。
 * 
 * @author TonyTan
 * @version 1.0, 2008-4-4
 */
public class DefineDataManger {
	private static DefineDataManger _instance = null;
	//日志对象
	private static Log _log = Log.getInstance();
	//每一个功能的基本定义信息缓存池对象
	private static Map<String,Map<String,String>> _mpFunction = null;
	//每一个功能的字段定义信息缓存池对象
	private static Map<String,List<Map<String,String>>> _mpColumn = null;
	
	private DefineDataManger() {
		_mpColumn = FactoryUtil.newMap();
		_mpFunction = FactoryUtil.newMap();
	}
	
	public static synchronized DefineDataManger getInstance() {
		if (_instance == null) {
			_instance = new DefineDataManger();
		}
		return _instance;
	}
	
	/**
	 * 取功能定义基本信息
	 * @param funId - 功能ID
	 * @return Map
	 */
	public Map<String,String> getFunData(String funId) {
		Map<String,String> mpRet = null;
		if (funId == null || funId.length() == 0) {
			_log.showWarn("funid is null!");
			return null;
		}
		
		//是否启用缓存
		String usePool = SystemVar.getValue("fun.define.usepool", "0");
		
		if (usePool.equals("1")) {
			synchronized(_mpFunction) {
				mpRet = _mpFunction.get(funId);
				if (mpRet == null) {
					mpRet = FunDefineDao.queryFun(funId);
					_mpFunction.put(funId, mpRet);
				}
			}
		} else {
			mpRet = FunDefineDao.queryFun(funId);
		}
		
		return mpRet;
	}
	
	/**
	 * 清除功能定义缓存数据，在修改了功能信息时需要调用
	 * @param funId -- 功能ID
	 */
	public void clearFunData(String funId) {
		if (funId == null || funId.length() == 0) return;
		
		//是否启用缓存
		String usePool = SystemVar.getValue("fun.define.usepool", "0");
		
		if (usePool.equals("1")) {
			_mpFunction.remove(funId);
		}
	}

	/**
	 * 查询该功能的字段列表
	 * @param funId - 功能ID
	 * @return List
	 */
	public List<Map<String,String>> getColData(String funId) {
		List<Map<String,String>> lsRet = null;
		if (funId == null || funId.length() == 0) {
			_log.showWarn("funid is null!");
			return null;
		}
		
		//是否启用缓存
		String usePool = SystemVar.getValue("fun.define.usepool", "0");
		
		if (usePool.equals("1")) {
			synchronized(_mpFunction) {
				lsRet = _mpColumn.get(funId);
				if (lsRet == null) {
					lsRet = FunDefineDao.queryCol(funId);
					_mpColumn.put(funId, lsRet);
				}
			}
		} else {
			lsRet = FunDefineDao.queryCol(funId);
		}
		
		return lsRet;
	}
	
	/**
	 * 清除字段定义缓存数据，在修改了字段信息时需要调用
	 * @param funId -- 功能ID
	 */
	public void clearColData(String funId) {
		if (funId == null || funId.length() == 0) return;
		
		//是否启用缓存
		String usePool = SystemVar.getValue("fun.define.usepool", "0");
		
		if (usePool.equals("1")) {
			_mpColumn.remove(funId);
		}
	}
}
