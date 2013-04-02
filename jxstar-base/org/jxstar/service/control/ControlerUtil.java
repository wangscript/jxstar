/*
 * ControlerUtil.java 2009-5-28
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.service.control;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.jxstar.control.action.RequestContext;
import org.jxstar.service.BusinessObject;
import org.jxstar.service.define.EventDefine;
import org.jxstar.util.DateUtil;
import org.jxstar.util.MapUtil;
import org.jxstar.util.log.Log;
import org.jxstar.util.resource.JsParam;

/**
 * 服务控制器助手对象。
 *
 * @author TonyTan
 * @version 1.0, 2009-5-28
 */
public class ControlerUtil {
	//--------------module param type--------------//
	//上下文件信息
	private static final String PARAM_CONTEXT = "context";
	//当前记录的主键值, 类型为String
	private static final String PARAM_TYPE_KEYID = "keyid";
	//当前记录的多个主键值, 类型为String[]
	private static final String PARAM_TYPE_ASKEY = "askey";
	//从当前上下文环境变量对象中取参数值, 类型为String
	private static final String PARAM_TYPE_REQUEST = "parameter";	
	//从当前上下文环境变量对象中取参数数组, 类型为String[]
	private static final String PARAM_TYPE_REQUESTS = "parameters";
	//常量值
	private static final String PARAM_TYPE_CONSTANT = "constant";
	//--------------module param type--------------//
	
	//日志对象
	private static Log _log = Log.getInstance();

	/**
	 * 执行事件调用的类.
	 * 
	 * @param lsInvoke - 事件之前或之后的方法
	 * @param requestContext - 上下文信息
	 * @return boolean 返回true表示成功; 返回false表示失败.
	 */
	public static boolean executeEvent(List<Map<String,String>> lsInvoke, 
			RequestContext requestContext) {
		if (lsInvoke == null || lsInvoke.isEmpty()) return true;
		
		for (int i = 0; i < lsInvoke.size(); i++) {
			Map<String,String> mpModule = lsInvoke.get(i);
			
			//调用ID, 用于检索参数
			String sInvokeID = MapUtil.getValue(mpModule, "invoke_id");
			//调用组件的类名
			String sModuleName = MapUtil.getValue(mpModule, "module_name");
			//调用组件的方法名
			String sMethodName = MapUtil.getValue(mpModule, "method_name");
			//调用组件的方法参数
			List<Map<String, String>> lsParam = EventDefine.getModuleParam(sInvokeID);
			//获取方法参数值对象
			Object[] params = getEventParam(lsParam, requestContext);
			
			_log.showDebug("========executeEvent className = " + sModuleName + ":" + sMethodName);
			
			if (! invoke(sModuleName, sMethodName, params, requestContext)) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * 调用类的方法.
	 * 
	 * @param sClassName - 类名
	 * @param sMethodName - 方法名
	 * @param params - 方法的参数
	 * @param requestContext - 上下文信息
	 * @return boolean 返回true表示成功; 返回false表示失败.
	 */
    public static boolean invoke(String sClassName, String sMethodName,
			Object[] params, RequestContext requestContext) {
		if (sClassName == null || sClassName.length() == 0 ||
				sMethodName == null || sMethodName.length() == 0) {
			_log.showWarn("invoke module param is null! ");
			return false;
		}
		//创建组件的class对象
		Class<?> clzz = null;
		try {
			clzz = Class.forName(sClassName);
		} catch (ClassNotFoundException e) {
			_log.showError(e);
			return false;
		}
		//创建组件对象的实例		
		Object clzzObj = null;
		try {
			clzzObj = clzz.newInstance();
		} catch (IllegalArgumentException e) {
			_log.showError(e);
			return false;
		} catch (SecurityException e) {
			_log.showError(e);
			return false;
		} catch (InstantiationException e) {
			_log.showError(e);
			return false;
		} catch (IllegalAccessException e) {
			_log.showError(e);
			return false;
		}
		
		//创建组件的方法对象		
		Method method = null;
		Class<?>[] clzzParams = getParamClass(params);
		try {
			method = clzz.getMethod(sMethodName, clzzParams);
		} catch (SecurityException e) {
			_log.showError(e);
			return false;
		} catch (NoSuchMethodException e) {
			_log.showError(e);
			return false;
		}
		//调用该对象的方法
		try {			
			Object objRet = method.invoke(clzzObj, params).toString();
			//如果是业务组件，则把响应参数返回给服务层
			if (clzzObj instanceof BusinessObject) {
				BusinessObject module = (BusinessObject) clzzObj;
				String message = module.getMessage();
				String returnData = module.getReturnData();
				
				if (message != null && message.length() > 0) {
					requestContext.setMessage(message);
				}
				
				if (returnData != null && returnData.length() > 0) {
					requestContext.setReturnData(returnData);
				}
			}
			return (! "false".equals(objRet.toString()));
		} catch (IllegalArgumentException e) {
			_log.showError(e);
			return false;
		} catch (IllegalAccessException e) {
			_log.showError(e);
			return false;
		} catch (InvocationTargetException e) {
			_log.showError(e);
			return false;
		}
	}
	
	/**
	 * 获取组件方法参数的值对象.
	 * 
	 * @param lsParam - 方法参数定义信息.
	 * @param requestContext - 上下文信息
	 * @return Object[]
	 */
	private static Object[] getEventParam(List<Map<String, String>> lsParam, 
			RequestContext requestContext) {
		if (lsParam == null || lsParam.isEmpty()) return null;
		if (requestContext == null) return null;
		
		//取环境变量中的用户信息
		Map<String,String> userInfo = requestContext.getUserInfo();
		
		Object[] objRet = new Object[lsParam.size()];
		for (int i = 0; i < lsParam.size(); i++) {
			Map<String, String> mpParam = lsParam.get(i);
			
			String sParamType = MapUtil.getValue(mpParam, "param_type");
			String sParamName = MapUtil.getValue(mpParam, "param_name"); 
			String sParamValue = MapUtil.getValue(mpParam, "param_value"); 
			
			Object objValue = null;
			if (sParamType.equals(PARAM_CONTEXT)) {
				objValue = requestContext;
			} else if (sParamType.equals(PARAM_TYPE_KEYID)) {
				objValue = requestContext.getRequestValue(JsParam.KEYID);
			} else if (sParamType.equals(PARAM_TYPE_ASKEY)) {
				objValue = requestContext.getRequestValues(JsParam.KEYID);
			} else if (sParamType.equals(PARAM_TYPE_REQUEST)) {
				objValue = requestContext.getRequestValue(sParamName);
			} else if (sParamType.equals(PARAM_TYPE_REQUESTS)) {
				objValue = requestContext.getRequestValues(sParamName);
			} else if (sParamType.equals(PARAM_TYPE_CONSTANT)) {
				objValue = getConstantParam(sParamValue, userInfo);
			} else {
				objValue = sParamValue;
			}
			if (objValue == null) {
				_log.showWarn("module param convert faild! ");
				return null;
			}
			
			objRet[i] = objValue;
			
			//_log.showDebug("========param " + i + " value=" + objValue.toString());
		}
		return objRet;
	}
	
	/**
	 * 根据参数值定义转换为真实的参数值, 支持的参数值有:
	 * {CURDATE} - 当前日期
	 * {CURDATETIME} - 当前时间
	 * {CURUSER} - 当前用户信息
	 * {CURUSERID} - 当前用户ID
	 * {CURUSERCODE} - 当前用户编码
	 * {CURUSERNAME} - 当前用户名称
	 * {CURDEPTID} - 当前部门ID
	 * {CURDEPTCODE} - 当前部门编码
	 * {CURDEPTNAME} - 当前部门名称
	 * 
	 * @param sParam - 参数值
	 * @param userInfo - 当前用户信息
	 * @return Object
	 */
	public static Object getConstantParam(String sParam, 
			Map<String,String> userInfo) {
		if (sParam == null || sParam.length() == 0) {
			_log.showWarn("sParam is null!");
			return "";
		}
		if (userInfo == null || userInfo.isEmpty()) {
			_log.showWarn("userInfo is null!");
			return sParam;
		}
		
		if (sParam.equals("{CURDATE}")) {
			return DateUtil.getToday();
		} else if (sParam.equals("{CURDATETIME}")) {
			return DateUtil.getTodaySec();
		} else if (sParam.equals("{CURUSER}")) {
			return userInfo;
		} else if (sParam.equals("{CURUSERID}")) {
			return MapUtil.getValue(userInfo, "user_id");
		} else if (sParam.equals("{CURUSERCODE}")) {
			return MapUtil.getValue(userInfo, "user_code");
		} else if (sParam.equals("{CURUSERNAME}")) {
			return MapUtil.getValue(userInfo, "user_name");
		} else if (sParam.equals("{CURDEPTID}")) {
			return MapUtil.getValue(userInfo, "dept_id");
		} else if (sParam.equals("{CURDEPTCODE}")) {
			return MapUtil.getValue(userInfo, "dept_code");
		} else if (sParam.equals("{CURDEPTNAME}")) {
			return MapUtil.getValue(userInfo, "dept_name");
		} else if (sParam.equals("{CURROLEID}")) {
			return MapUtil.getValue(userInfo, "role_id");
		} 
		
		//字符串常量
		return sParam;
	}
	
	/**
	 * 获取参数数组的class信息.
	 * 
	 * @param params - 参数数组
	 * @return Class[]
	 */
	@SuppressWarnings("rawtypes")
    private static Class[] getParamClass(Object[] params) {
		if (params == null) return null;
		
		Class[] clzzRet = new Class[params.length];
		
		for (int i = 0; i < params.length; i++) {
			Class pc = null;
			if (params[i] instanceof Map) {
				pc = Map.class;
			} else if (params[i] instanceof List) {
				pc = List.class;
			} else {
				pc = params[i].getClass();
			}

			clzzRet[i] = pc;
		}
		return clzzRet;
	}
}
