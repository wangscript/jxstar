/*
 * WarnTask.java 2011-3-17
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */

package org.jxstar.service.warn;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jxstar.control.action.RequestContext;
import org.jxstar.dao.DaoParam;
import org.jxstar.service.control.ServiceController;
import org.jxstar.service.define.DefineDataManger;
import org.jxstar.task.SystemTask;
import org.jxstar.task.TaskException;
import org.jxstar.util.DateUtil;
import org.jxstar.util.MapUtil;
import org.jxstar.util.StringUtil;
import org.jxstar.util.SystemUtil;
import org.jxstar.util.factory.FactoryUtil;
import org.jxstar.util.factory.SystemFactory;
import org.jxstar.util.key.KeyCreator;
import org.jxstar.util.resource.JsMessage;
import org.jxstar.util.resource.JsParam;

/**
 * 上报组件的后台任务。
 * 上报任务轮询检查注册了上报提醒的功能，当出现满足指定条件的记录时，
 * 将产生工作消息提醒指定的用户，并能触发执行应用程序的事件。
 * 
 * 上报组件处理内容如下：
 * 1、检索满足上报条件的记录；
 * 2、解析上报提醒消息；
 * 3、检索符合条件的上报用户；
 * 4、给用户发送消息；
 * 5、触发注册事件执行；
 * 6、记录上报日志；
 *
 * @author TonyTan
 * @version 1.0, 2011-3-17
 */
public class WarnTask extends SystemTask {
	
	/**
	 * 执行上报任务
	 */
	public void execute() throws TaskException {
		//取所有注册的上报记录
		List<Map<String,String>> lsWarn = WarnUtil.queryWarn();
		if (lsWarn.isEmpty()) {
			_log.showDebug(" warn: no warn define");
			return;
		}
		
		DefineDataManger manger = DefineDataManger.getInstance();
		
		for (int i = 0, n = lsWarn.size(); i < n; i++) {
			//上报执行开始时间
			Date startTime = new Date();
			
			Map<String,String> mpWarn = lsWarn.get(i);
			
			String warnName = mpWarn.get("warn_name");
			_log.showDebug("开始执行上报任务：" + warnName);
			
			String warnId = mpWarn.get("warn_id");
			String funId = mpWarn.get("fun_id");
			
			//执行间隔时间值
			String runPlan = mpWarn.get("run_plan");
			//上次执行时间
			String runDate = mpWarn.get("run_date");
			
			//检查是否到达有效时间
			if (!validPlan(runDate, runPlan)) {
				_log.showDebug("上报任务【{0}】，还未经过执行间隔时间，不能执行", warnName);
				continue;
			}
			
			//判断间隔时间值
			String timeValue = mpWarn.get("time_value");
			String whereSql = mpWarn.get("where_sql");
			
			String eventCode = mpWarn.get("event_code");
			
			//取功能定义信息
			Map<String,String> mpFun = manger.getFunData(funId);
			//取功能主键字段
			String keyName = mpFun.get("pk_col");
			keyName = StringUtil.getNoTableCol(keyName);
			//取功能表名
			String tableName = mpFun.get("table_name");
			
			//是否根据权限通知，如果为是，则根据系统授权查找用户，否则根据通知人员明细查找用户
			String useRole = mpWarn.get("use_role");
			
			List<Map<String,String>> lsData = 
				WarnUtil.queryData(funId, tableName, keyName, warnId, whereSql, timeValue);
			
			//执行错误信息
			String errorMsg = "";
			try {
				for (int j = 0, m = lsData.size(); j < m; j++) {
					Map<String,String> mpData = lsData.get(j);
					
					//取需要上报通知的用户
					List<Map<String,String>> lsUser = null;
					if (useRole.equals("1")) {
						lsUser = WarnUtil.queryRoleUser(funId, mpData);
					} else {
						lsUser = WarnUtil.queryWarnUser(warnId, mpData);
					}
					_log.showDebug("----------有效用户数量：" + lsUser.size());
					
					for (int k = 0, l = lsUser.size(); k < l; k++) {
						Map<String,String> mpUser = lsUser.get(k);
						
						//给用户发送上报消息
						sendWarnAssign(mpWarn, mpFun, mpData, mpUser);
						
						//触发上报事件
						if (eventCode.length() > 0) {
							//取记录主键值
							String dataId = mpData.get(keyName);
							executeEvent(funId, eventCode, dataId, mpUser);
						}
					}
				}
			} catch(TaskException e) {
				_log.showError(e);
				errorMsg = e.getMessage();
			}
			
			//更新上次执行日期
			updateWarnDate(warnId);
			
			//是否记录日志
			String hasLog = mpWarn.get("has_log");
			if (hasLog.equals("1")) {
				//日志最大数量
				String logNum = mpWarn.get("log_num");
				//记录上报任务执行日志
				writeWarnLog(warnId, logNum, errorMsg, startTime);
			}
		}
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
	private boolean sendWarnAssign(Map<String,String> mpWarn, Map<String,String> mpFun,
			Map<String,String> mpData, Map<String,String> mpUser) throws TaskException {
		//是否发送短信
		//String sendSms = mpWarn.get("send_sms");
		//是否发送邮件
		//String sendEmail = mpWarn.get("send_email");
		
		String isAssign = mpWarn.get("is_assign");
		
		MessageUtil.sendWarnAssign(mpWarn, mpFun, mpData, mpUser);
		
		//不是待办任务的上报才需要发送系统消息，如报警、预警等
		if (isAssign.equals("0")) {
			MessageUtil.sendWarnMsg(mpWarn, mpFun, mpData, mpUser);
		}
		
		return true;
	}
	
	/**
	 * 判断当前时间是否到达可以执行时间，判断方法为：
	 * 当前时间 >= 上次执行时间 + 执行间隔时间值
	 * @param runDate -- 上次执行时间
	 * @param runPlan -- 执行间隔时间值，格式如：1d, 1h, 1m，最后一位字符为时间单位
	 * @return
	 */
	private boolean validPlan(String runDate, String runPlan) {
		if (runDate.length() == 0 || runPlan.length() == 0) return true;
		
		//转换上次运行时间
		Calendar cal = DateUtil.strToCalendar(runDate);
		
		//添加间隔值
		cal = WarnUtil.intervalDate(cal, runPlan);
		
		//判断是否到达执行时间
		Calendar today = Calendar.getInstance();
		return today.compareTo(cal) >= 0;
	}
	
	/**
	 * 上报执行后触发的事件代码
	 * @param funId -- 功能ID
	 * @param eventCode -- 事件代码
	 * @param dataId -- 业务主键值
	 * @param mpUser -- 用户信息
	 * @return
	 */
	private boolean executeEvent(String funId, String eventCode, 
			String dataId, Map<String,String> mpUser) throws TaskException {
		//请求参数对象
		RequestContext requestContext = toRequestContext(funId, eventCode, dataId, mpUser);
		
		//创建服务控制器对象
		ServiceController serviceController = (ServiceController) SystemFactory
										.createSystemObject("ServiceController");
		if (serviceController == null) {
			throw new TaskException(JsMessage.getValue("commonaction.createenginefaild"));
		}
		
		//执行失败时，把内部的消息抛给前台
		if (!serviceController.execute(requestContext)) {
			String message = requestContext.getMessage();
			if (message == null || message.length() == 0) {
				message = JsMessage.getValue("commonaction.faild");
			}
			_log.showDebug("上报【{0}】事件执行结果：" + message, eventCode);
			//"上报【{0}】事件执行结果："
			throw new TaskException(JsMessage.getValue("warntask.warnexe") + message, eventCode);
		} else {
			_log.showDebug("上报【{0}】事件执行成功！", eventCode);
		}
		
		return true;
	}
	
	/**
	 * 取得触发事件的上下文对象
	 * @param funId -- 功能ID
	 * @param eventCode -- 事件代码
	 * @param dataId -- 业务主键值
	 * @param mpUser -- 用户信息
	 * @return
	 */
	private RequestContext toRequestContext(String funId, String eventCode, 
			String dataId, Map<String,String> mpUser) {
		Map<String,Object> mpRequest = FactoryUtil.newMap();
		//请求参数对象
		RequestContext requestContext = new RequestContext(mpRequest);
		
		//取页面类型
		String pageType = "grid";
		//设置头信息
		requestContext.setFunID(funId);
		requestContext.setPageType(pageType);
		requestContext.setEventCode(eventCode);
		
		mpRequest.put("funid", funId);
		mpRequest.put("pagetype", pageType);
		mpRequest.put("eventcode", eventCode);
		
		//触发事件不考虑事务，由后台任务调用处理
		mpRequest.put("support_tran", "0");
		
		//业务数据主键值
		mpRequest.put(JsParam.KEYID, dataId);
		
		requestContext.setUserInfo(mpUser);
		
		return requestContext;
	}
	
	/**
	 * 更新上报的上次执行日期
	 * @param warnId -- 上报ID
	 * @return
	 */
	private boolean updateWarnDate(String warnId) {
		String sql = "update warn_base set run_date = ? where warn_id = ?";
		DaoParam param = _dao.createParam(sql);
		param.addDateValue(DateUtil.getTodaySec());
		param.addStringValue(warnId);
		
		return _dao.update(param);
	}
	
	/**
	 * 记录上报任务的执行日志
	 * @param warnId -- 上报定义ID
	 * @param logNum -- 保留日志条数
	 * @param errorMsg -- 执行错误消息
	 * @param startTime -- 执行开始时间
	 * @return
	 */
	private boolean writeWarnLog(String warnId, String logNum, String errorMsg, Date startTime) {
		//如果日志数量超了，则删除
		int hasNum = getLogNum(warnId);
		if (logNum.length() > 0) {
			int logSize = Integer.parseInt(logNum);
			if (hasNum > logSize) {
				delWarnLog(warnId);
			}
		}
		
		//当前时间
		String curTime = DateUtil.getTodaySec();
		
		//新增运行日志
		String sql = "insert into warn_log(log_id, warn_id, start_date, end_date, server_name, server_ip, run_error) "+
					 "values(?, ?, ?, ?, ?, ?, ?)";
		
		DaoParam param = _dao.createParam(sql);
		
		String logId = KeyCreator.getInstance().createKey("warn_log");
		String sTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(startTime);
		String serverName = SystemUtil.getHostName();
		String serverIp = SystemUtil.getIPAddress();
		
		param.addStringValue(logId);
		param.addStringValue(warnId);
		param.addDateValue(sTime);
		param.addDateValue(curTime);
		param.addStringValue(serverName);
		param.addStringValue(serverIp);
		param.addStringValue(errorMsg);
		
		return _dao.update(param);
	}
	
	/**
	 * 删除任务执行日志
	 * @param warnId -- 后台任务ID
	 */
	private boolean delWarnLog(String warnId) {
		String sql = "delete from warn_log where warn_id = ?";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(warnId);
		return _dao.update(param);
	}
	
	/**
	 * 取任务日志记录数
	 * @param warnId -- 后台任务ID
	 * @return
	 */
	private int getLogNum(String warnId) {
		String sql = "select count(*) as cnt from warn_log where warn_id = ?";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(warnId);
		
		return MapUtil.hasRecodNum(_dao.queryMap(param));
	}
}
