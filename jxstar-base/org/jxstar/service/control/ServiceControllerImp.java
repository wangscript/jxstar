/*
 * ServiceControllerImp.java 2009-5-28
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.service.control;

import java.util.List;
import java.util.Map;


import org.jxstar.control.action.RequestContext;
import org.jxstar.dao.transaction.TransactionException;
import org.jxstar.dao.transaction.TransactionManager;
import org.jxstar.service.define.EventDefine;
import org.jxstar.service.util.SysLogUtil;
import org.jxstar.util.config.SystemVar;
import org.jxstar.util.factory.SystemFactory;
import org.jxstar.util.log.Log;

/**
 * 服务控制器的实现。
 *
 * @author TonyTan
 * @version 1.0, 2009-5-28
 */
public class ServiceControllerImp implements ServiceController {
	// 日志对象
	private static Log _log = Log.getInstance();
	// 事务管理对象
	private static TransactionManager _tranMng = null;
	
	public ServiceControllerImp() {
		_tranMng = (TransactionManager) SystemFactory
				.createSystemObject("TransactionManager");
		if (_tranMng == null) {
			_log.showWarn("TransactionManager create faild! ");
		}
	}
	
	/**
	 * 根据上下文信息执行服务处理.
	 * 
	 * @param requestContext - 上下文信息
	 * @return boolean 返回true表示成功; 返回false表示失败.
	 */
	public boolean execute(RequestContext requestContext) {
		if (requestContext == null) {
			_log.showWarn("process context param is null! ");
			return false;
		}
		//获取参数取功能ID与事件代号
		String funID = requestContext.getFunID();
		String eventCode = requestContext.getEventCode();
		if (funID.length() == 0 || eventCode.length() == 0) {
			_log.showWarn("process funid or eventcode is null! ");
			return false;
		}
		String pageType = requestContext.getPageType();
		_log.showDebug("process funid={0} eventcode={1} pagetype={2}", 
				funID, eventCode, pageType);
		
		//如果是审批通过事件，则应该取审批目标功能的ID
		String checkFunId = funID;
		if (eventCode.indexOf("process_") == 0) {
			checkFunId = requestContext.getRequestValue("check_funid");
		}
		
		//取得事件调用类列表
		List<Map<String,String>> lsInvoke = null;
		if (eventCode.equals("audit") || eventCode.equals("process_3")) {
			lsInvoke = EventDefine.getAuditModule(funID, eventCode, checkFunId);
		} else {
			lsInvoke = EventDefine.getEventModule(funID, eventCode);
		}
		if (lsInvoke.isEmpty()) {
			_log.showWarn("eventcode {0} process lsInvoke is null! ", eventCode);
			return false;
		}
		
		//临时处理，审批通过后调用audit执行后事件用，修改funID为目标审批功能ID
		if (eventCode.indexOf("process_") == 0) {
			requestContext.setFunID(checkFunId);
			requestContext.setRequestValue("funid", checkFunId);
		}
		
		//是否支持事务，缺省是支持事务，在外部系统调用控制器时，
		//可以设置控制器不支持事务，由外部系统处理事务，如工作流的处理机。
		String supportTran = requestContext.getRequestValue("support_tran");
		boolean isTran = (!supportTran.equals("0"));
		
		boolean hasLock = SystemVar.getValue("sys.lock.has", "1").equals("1");
		//检查当前正在执行的操作，如果存在就退出
		if (hasLock && ControlerLock.checkDoing(requestContext)) return false;
		
		//开始一个事务
		if (isTran) _tranMng.startTran();
		try {			
			//执行事件的调用类
			if (!ControlerUtil.executeEvent(lsInvoke, requestContext)) {
				if (isTran) _tranMng.rollbackTran();
				return false;
			}
			_log.showDebug("process event {0} success. ", eventCode);
	
			if (isTran) _tranMng.commitTran();
		} catch (TransactionException e) {
			_log.showError(e);
			return false;
		} finally {
			//删除业务锁注册记录
			if (hasLock) {
				ControlerLock.delDoing(requestContext);
			}
			//记录操作日志
			SysLogUtil.writeLog(requestContext);
		}

		return true;
	}
}
