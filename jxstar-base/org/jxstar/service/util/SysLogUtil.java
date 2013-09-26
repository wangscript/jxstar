/*
 * Copyright(c) 2012 Donghong Inc.
 */
package org.jxstar.service.util;

import java.util.List;
import java.util.Map;

import org.jxstar.control.action.RequestContext;
import org.jxstar.dao.BaseDao;
import org.jxstar.dao.DaoParam;
import org.jxstar.util.DateUtil;
import org.jxstar.util.MapUtil;
import org.jxstar.util.config.SystemVar;
import org.jxstar.util.factory.FactoryUtil;
import org.jxstar.util.key.KeyCreator;
import org.jxstar.util.resource.JsParam;

/**
 * 系统操作日志管理类。
 * 增加两个系统变量：sys.log.start 是否启用操作日志记录
 * 		sys.log.events 记录操作日志的事件代码
 *
 * @author TonyTan
 * @version 1.0, 2012-10-26
 */
public class SysLogUtil {
	private static BaseDao _dao = BaseDao.getInstance();
	private static KeyCreator _key = KeyCreator.getInstance();
	private static String _sql = "insert into sys_log(log_id, fun_id, event_code, page_type, " +
			"user_id, user_name, event_name, fun_name, log_date, message, data_id) " +
			"values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	
	private static Map<String,String> _mpFunName = queryFunName();
	private static Map<String,String> _mpEventName = queryEventName();
	
	/**
	 * 根据请求对象记录操作日志
	 * @param request
	 * @return
	 */
	public static boolean writeLog(RequestContext request) {
		String funId = request.getFunID();
		String eventCode = request.getEventCode();
		String pageType = request.getPageType();
		Map<String,String> mpUser = request.getUserInfo();
		String userId = MapUtil.getValue(mpUser, "user_id");
		String userName = MapUtil.getValue(mpUser, "user_name");
		String message = request.getMessage();
		String keyId = request.getRequestValue(JsParam.KEYID);
		
		return writeLog(funId, eventCode, pageType, userId, userName, message, keyId);
	}

	/**
	 * 记录操作日志
	 * @param funId
	 * @param eventCode
	 * @param pageType
	 * @param userId
	 * @param userName
	 * @param message
	 * @param keyId
	 * @return
	 */
	public static boolean writeLog(String funId, String eventCode, String pageType, 
			String userId, String userName, String message, String keyId) {
		//是否启用日志记录
		String isWrite = SystemVar.getValue("sys.log.start", "0");
		if (!isWrite.equals("1")) return false;
		
		//启用操作日志的功能ID，用,,分隔
		String logFunIds = SystemVar.getValue("sys.log.start.funid");
		if (logFunIds.length() > 0) {
			//如果当前功能ID不在设置范围内，则不处理日志
			if (logFunIds.indexOf(","+funId+",") < 0) {
				return false;
			}
		}
		
		//是否是记录日志的事件代码
		String logEvents = SystemVar.getValue("sys.log.events");
		if (logEvents.indexOf(eventCode) < 0) return false;
		
		//给缺省值
		if (message == null || message.length() == 0) message = "操作成功！";
		if (pageType == null || pageType.length() == 0) pageType = "grid";
		
		if (keyId.length() > 100) keyId = keyId.substring(0, 100);
		
		//取功能名称
		String funName = _mpFunName.get(funId);
		//取事件名称
		String eventName = _mpEventName.get(eventCode);

		//新增日志记录
		DaoParam param = _dao.createParam(_sql);
		String logId = _key.createKey("sys_log");
		param.addStringValue(logId);
		param.addStringValue(funId);
		param.addStringValue(eventCode);
		param.addStringValue(pageType);
		param.addStringValue(userId);
		param.addStringValue(userName);
		param.addStringValue(eventName);
		param.addStringValue(funName);
		param.addDateValue(DateUtil.getTodaySec());
		param.addStringValue(message);
		param.addStringValue(keyId);
		
		return _dao.update(param);
	}
	
	//取所有功能名称
	private static Map<String,String> queryFunName() {
		Map<String,String> mpFunName = FactoryUtil.newMap();
		String sql = "select fun_id, fun_name from fun_base where reg_type <> 'nouse'";
		DaoParam param = _dao.createParam(sql);
		List<Map<String,String>> lsFun = _dao.query(param);
		
		for (Map<String,String> mpFun : lsFun) {
			mpFunName.put(mpFun.get("fun_id"), mpFun.get("fun_name"));
		}
		
		return mpFunName;
	}
	
	//取所有记录日志的事件名称
	private static Map<String,String> queryEventName() {
		Map<String,String> mpEventName = FactoryUtil.newMap();
		String logEvents = SystemVar.getValue("sys.log.events");
		if (logEvents.length() == 0) return mpEventName;
		
		String where = " event_code in ('" + logEvents.replace(",", "','") + "')";
		String sql = "select event_code, event_name from fun_event where " + where;
		DaoParam param = _dao.createParam(sql);
		List<Map<String,String>> lsEvent = _dao.query(param);
		
		for (Map<String,String> mpEvent : lsEvent) {
			mpEventName.put(mpEvent.get("event_code"), mpEvent.get("event_name"));
		}
		
		return mpEventName;
	}
}
