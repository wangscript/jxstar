/*
 * Copyright(c) 2013 Donghong Inc.
 */
package org.jxstar.service.check;

import org.jxstar.control.action.RequestContext;
import org.jxstar.service.BusinessObject;

/**
 * 检查项测试类。
 *
 * @author TonyTan
 * @version 1.0, 2013-3-29
 */
public class CheckDemoBO extends BusinessObject {
	private static final long serialVersionUID = 1L;

	public String check(String keyId) {
		_log.showDebug("..........检查项方法，keyId={0}！", keyId);
		
		return _returnSuccess;
	}
	
	public String check1(String keyId) {
		_log.showDebug("..........检查项方法1，keyId={0}！", keyId);
		
		if (keyId.length() > 0) {
			setReturnData("{param1:'tan', param2:'zhibin'}");
			return _returnFaild;
		}
		
		return _returnSuccess;
	}
	
	public String check2(String keyId, RequestContext request) {
		String userId = request.getUserInfo().get("user_id");
		_log.showDebug("..........检查项方法1，keyId={0}，userid={1}！", keyId, userId);
		
		if (keyId.length() > 0) {
			setReturnData("{param2:'aa', param3:'bb'}");
			return _returnFaild;
		}
		
		return _returnSuccess;
	}
}
