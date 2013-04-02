/*
 * Copyright(c) 2013 Donghong Inc.
 */
package org.jxstar.service.check;

import java.util.List;
import java.util.Map;

import org.jxstar.control.action.RequestContext;
import org.jxstar.service.BusinessObject;
import org.jxstar.util.config.SystemVar;
import org.jxstar.util.resource.JsParam;

/**
 * 执行检查项设置。
 * 暂时只处理audit事件的检查项。
 *
 * @author TonyTan
 * @version 1.0, 2013-3-28
 */
public class EventCheckBO extends BusinessObject {
	private static final long serialVersionUID = 1L;

	/**
	 * 检查所有启用的检查项的类设置与SQL设置执行
	 * @param request -- 请求
	 * @return
	 */
	public String execute(RequestContext request) {
		//系统变量设置：是否启用事件检查项设置
		String v = SystemVar.getValue("sys.event.check", "0");
		if (!v.equals("1")) {
			return _returnSuccess;
		}
		
		if (request == null) {
			_log.showWarn("EventCheck context param is null! ");
			return _returnSuccess;
		}
		
		//获取参数取功能ID与事件代号
		String funId = request.getFunID();
		String eventCode = request.getEventCode();
		if (funId.length() == 0 || eventCode.length() == 0) {
			_log.showWarn("EventCheck funid or eventcode is null! ");
			return _returnSuccess;
		}
		_log.showDebug("EventCheck funid={0} eventcode={1}", funId, eventCode);
		
		if (!eventCode.equals("audit")) return _returnSuccess;
		
		//取出当前主键值数组
		String[] asKey = request.getRequestValues(JsParam.KEYID);
		if (asKey == null || asKey.length == 0) {
			_log.showWarn("EventCheck not find keyid data! ");
			return _returnSuccess;
		}
		
		//获取启用的检查项
		List<Map<String,String>> lsCheck = EventCheckUtil.queryCheck(funId, eventCode);
		if (lsCheck.isEmpty()) {
			_log.showDebug("EventCheck not define!");
			return _returnSuccess;
		}
		
		for (String keyId : asKey) {
			boolean ret = exeAllCheck(keyId, lsCheck, request);
			if (!ret) {
				_log.showDebug("EventCheck execute faild, keyid={0}!", keyId);
				return _returnFaild;
			}
		}
		
		return _returnSuccess;
	}
	
	/**
	 * 执行一条记录的所有检查项，并返回错误检查数据
	 * @param keyId -- 主键值
	 * @param lsCheck -- 所有检查项
	 * @param request -- 请求上下文
	 * @return
	 */
	private boolean exeAllCheck(String keyId, List<Map<String,String>> lsCheck, RequestContext request) {
		boolean faild = false;
		StringBuffer sbinfo = new StringBuffer();
		for (Map<String,String> mpCheck : lsCheck) {
			String check_name = mpCheck.get("check_name");
			String faild_desc = mpCheck.get("faild_desc");
			
			//执行单项检查项
			boolean b = exeOneCheck(keyId, mpCheck, request);
			
			//把检查项名称、检查结果、检查数据，返回到前台
			if (b) {
				sbinfo.append("{checkName:'"+ check_name +"', result:true},");
			} else {
				sbinfo.append("{checkName:'"+ check_name +"', result:false, faildDesc:'"+ faild_desc +"', " +
						"keyid:'"+ keyId +"', message:'"+ request.getMessage() +"', data:"+ request.getReturnData() +"},");
				faild = true;
			}
			
			//清空上一个类的消息与数据
			request.setMessage("");
			request.setReturnData("");
		}
		
		//如果检查项失败，则返回检查项信息到前台
		if (faild) {
			String json = "[" + sbinfo.substring(0, sbinfo.length()-1) + "]";
			_log.showDebug(".........EventCheck check faild returndata=" + json);
			request.setReturnExtData("checkMsg", json);
			return false;
		}
		
		return true;
	}
	
	/**
	 * 执行检查项，如果检查项失败，从request.getMessage中取错误信息，
	 * 从request.getReturnData()取返回数据，返回的数据不能是数组，只能是{}对象。
	 * @param keyId -- 主键值
	 * @param mpCheck -- 检查项设置
	 * @param request -- 请求上下文
	 * @return 
	 */
	private boolean exeOneCheck(String keyId, Map<String,String> mpCheck, RequestContext request) {
		//1 类设置、2 SQL设置
		String setType = mpCheck.get("set_type");
		
		if (setType.equals("1")) {
			if (! EventCheckUtil.exeCheckClass(keyId, mpCheck, request)) {
				return false;
			}
		} else if (setType.equals("2")) {
			if (! EventCheckUtil.exeCheckSql(keyId, mpCheck, request)) {
				return false;
			}
		}
		
		return true;
	}
	
	
}
