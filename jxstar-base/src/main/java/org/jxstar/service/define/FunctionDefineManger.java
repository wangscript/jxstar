/*
 * FunctionDefineManger.java 2009-5-28
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.service.define;

import java.util.List;
import java.util.Map;


import org.jxstar.service.BoException;
import org.jxstar.util.config.SystemVar;
import org.jxstar.util.factory.FactoryUtil;
import org.jxstar.util.log.Log;

/**
 * 功能对象管理器，支持构建功能对象的缓存，也可以不使用缓存。
 * 通过配置，在生产系统中可以使用缓存，在开发过程中不使用缓存，每次都重新生成功能对象。
 * 
 * @author TonyTan
 * @version 1.0, 2009-5-28
 */
public class FunctionDefineManger {
	private static FunctionDefineManger _instance = null;
	//日志对象
	private static Log _log = Log.getInstance();
	//功能对象池
	private static Map<String,FunctionDefine> _mpFunObject = null;
	//功能构建者
	private static FunctionDefineBuilder _builder = null;
	
	private FunctionDefineManger() {
		_mpFunObject = FactoryUtil.newMap(); 
		_builder = new FunctionDefineBuilder();
	}
	
	/**
	 * 获取功能对象管理器, 采用单例模式.
	 * @return FunObjectManger
	 */
	public static synchronized FunctionDefineManger getInstance() {
		if (_instance == null) {
			_instance = new FunctionDefineManger();
		}
		return _instance;
	}
	
	/**
	 * 创建该功能标示的对象。
	 * 
	 * @param funId - 功能标示
	 */
	public FunctionDefine getDefine(String funId) {
		FunctionDefine objRet = null;
		if (funId == null || funId.length() == 0) {
			_log.showWarn("create function object fiald: funId is null! ");
			return null;
		}
		
		//是否启用缓存
		String usePool = SystemVar.getValue("fun.define.usepool", "0");
		
		try {
			if (usePool.equals("1")) {
				synchronized(_mpFunObject) {
					objRet = _mpFunObject.get(funId);
					if (objRet == null) {
						objRet = _builder.build(funId);
						_mpFunObject.put(funId, objRet);
					}
				}
			} else {
				objRet = _builder.build(funId);
			}
		} catch(BoException e) {
			_log.showError(e);
		}
			
		return objRet;
	}
	
	/**
	 * 清除功能定义缓存数据
	 * @param funId -- 功能ID
	 */
	public void clearDefine(String funId) {
		if (funId == null || funId.length() == 0) return;
		
		//是否启用缓存
		String usePool = SystemVar.getValue("fun.define.usepool", "0");
		
		if (usePool.equals("1")) {
			_mpFunObject.remove(funId);
		}
	}
	
	/**
	 * 构建本系统所有有效的功能对象，先查询系统所有的有效功能，
	 * 创建每个功能对象保存到对象池中.
	 * 
	 * @return boolean 
	 */
	public boolean loadDefine() {
		//是否系统启动时全部装载功能对象
		String loadAll = SystemVar.getValue("fun.define.loadall", "0");
		if (!loadAll.equals("1")) return false;
		
		List<Map<String, String>> lsFun = FunDefineDao.queryFun();
		if (lsFun == null || lsFun.isEmpty()) {
			_log.showWarn("query all function base info is null! ");
			return false;
		}
		
		FunctionDefine objRet = null;
		for (int i = 0; i < lsFun.size(); i++) {
			String funId = lsFun.get(i).get("fun_id");
			if (funId == null || funId.length() == 0) {
				_log.showWarn("query all function base info funId is null! ");
				return false;
			}
			
			try {
				objRet = _builder.build(funId);
			} catch (BoException e) {
				_log.showError(e);
			}
			if (objRet == null) {
				_log.showWarn("create function object fiald: funId is {0}.", funId);
				return false;
			}
			
			_mpFunObject.put(funId, objRet);
		}
		
		return true;
	}	
}
