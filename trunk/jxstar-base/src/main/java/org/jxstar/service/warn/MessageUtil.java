/*
 * MessageUtil.java 2011-3-17
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */

package org.jxstar.service.warn;

import java.util.Map;

import org.jxstar.dao.DmDao;
import org.jxstar.service.util.TaskUtil;
import org.jxstar.task.TaskException;
import org.jxstar.util.DateUtil;
import org.jxstar.util.StringUtil;
import org.jxstar.util.factory.FactoryUtil;
import org.jxstar.util.resource.JsMessage;

/**
 * 处理上报组件的消息发送。
 *
 * @author TonyTan
 * @version 1.0, 2011-3-17
 */
public class MessageUtil {

	/**
	 * 发送上报的通知消息
	 * @param mpWarn -- 上报定义
	 * @param mpFun -- 功能定义
	 * @param mpData -- 应用数据
	 * @param mpUser -- 上报用户
	 * @return
	 * @throws TaskException
	 */
	public static boolean sendWarnAssign(Map<String,String> mpWarn, Map<String,String> mpFun,
			Map<String,String> mpData, Map<String,String> mpUser) throws TaskException {
		String warnId = mpWarn.get("warn_id");
		String isAssign = mpWarn.get("is_assign");
		String warnName = mpWarn.get("warn_name");
		
		//消息内容
		String warnDesc = mpWarn.get("warn_desc");
		//解析后的消息内容
		warnDesc = TaskUtil.parseAppField(warnDesc, mpData, false);
		
		//取功能ID
		String funId = mpWarn.get("fun_id");
		//取功能主键字段
		String keyName = mpFun.get("pk_col");
		keyName = StringUtil.getNoTableCol(keyName);
		//取记录主键值
		String dataId = mpData.get(keyName);
		
		//发送消息
		Map<String,String> mpAssign = FactoryUtil.newMap();
		mpAssign.put("warn_id", warnId);
		mpAssign.put("warn_name", warnName);
		mpAssign.put("warn_desc", warnDesc);
		mpAssign.put("user_id", mpUser.get("user_id"));
		mpAssign.put("user_name", mpUser.get("user_name"));
		mpAssign.put("start_date", DateUtil.getTodaySec());
		mpAssign.put("run_state", "0");
		mpAssign.put("is_assign", isAssign);
		mpAssign.put("fun_id", funId);
		mpAssign.put("data_id", dataId);
		mpAssign.put("add_userid", "sys_warn");
		mpAssign.put("add_date", DateUtil.getTodaySec());
		//新增消息记录
		String msgId = DmDao.insert("warn_assign", mpAssign);
		if (msgId == null || msgId.length() == 0) {
			//"创建上报系统消息失败！"
			throw new TaskException(JsMessage.getValue("messageutil.createerror"));
		}
		
		return true;
	}
	
	/**
	 * 发送上报的通知消息
	 * @param mpWarn -- 上报定义
	 * @param mpFun -- 功能定义
	 * @param mpData -- 应用数据
	 * @param mpUser -- 上报用户
	 * @return
	 * @throws TaskException
	 */
	public static boolean sendWarnMsg(Map<String,String> mpWarn, Map<String,String> mpFun,
			Map<String,String> mpData, Map<String,String> mpUser) throws TaskException {
		//消息内容
		String warnDesc = mpWarn.get("warn_desc");
		//解析后的消息内容
		warnDesc = TaskUtil.parseAppField(warnDesc, mpData, false);
		
		//取功能ID
		String funId = mpWarn.get("fun_id");
		//取功能主键字段
		String keyName = mpFun.get("pk_col");
		keyName = StringUtil.getNoTableCol(keyName);
		//取记录主键值
		String dataId = mpData.get(keyName);
		
		//发送消息
		Map<String,String> mpMsg = FactoryUtil.newMap();
		mpMsg.put("content", warnDesc);
		mpMsg.put("from_userid", "sys_warn");
		mpMsg.put("from_user", "上报任务");
		mpMsg.put("to_userid", mpUser.get("user_id"));
		mpMsg.put("to_user", mpUser.get("user_name"));
		mpMsg.put("send_date", DateUtil.getTodaySec());
		mpMsg.put("isto", "1");
		mpMsg.put("msg_state", "1");
		mpMsg.put("msg_type", "sys");
		mpMsg.put("fun_id", funId);
		mpMsg.put("data_id", dataId);
		mpMsg.put("add_userid", "sys_warn");
		mpMsg.put("add_date", DateUtil.getTodaySec());
		//新增消息记录
		String msgId = DmDao.insert("plet_msg", mpMsg);
		if (msgId == null || msgId.length() == 0) {
			throw new TaskException(JsMessage.getValue("messageutil.createerror"));
		}
		
		return true;
	}
}
