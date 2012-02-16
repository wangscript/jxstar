/*
 * TaskPlanBO.java 2011-1-14
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.service.studio;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.jxstar.dao.DaoParam;
import org.jxstar.service.BusinessObject;
import org.jxstar.task.DatePlan;
import org.jxstar.task.TaskException;
import org.jxstar.task.load.SystemTaskLoader;
import org.jxstar.util.StringUtil;
import org.jxstar.util.resource.JsMessage;

/**
 * 后台任务设置处理类。
 *
 * @author TonyTan
 * @version 1.0, 2011-1-14
 */
public class TaskPlanBO extends BusinessObject {
	private static final long serialVersionUID = -5272517044276820978L;
	
	/**
	 * 启用并加载任务
	 * @param taskId -- 任务ID
	 * @return
	 */
	public String valid(String taskId) {
		String sql = "update task_base set task_state = '2' where task_id = ?";
		
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(taskId);
		
		if (!_dao.update(param)) {
			//"修改后台任务【{0}】的状态为生效失败！"
			setMessage(JsMessage.getValue("taskplanbo.useerror"), taskId);
			return _returnFaild;
		}
		
		//取后台任务信息
		Map<String,String> mpTask = getTask(taskId);
		
		//加载后台任务
		SystemTaskLoader taskLoader = new SystemTaskLoader();
		taskLoader.loadTask(mpTask);
		
		return _returnSuccess;
	}
	
	/**
	 * 重载后台任务
	 * @param taskId -- 任务ID
	 * @return
	 */
	public String reload(String taskId) {
		//取后台任务信息
		Map<String,String> mpTask = getTask(taskId);
		
		//加载后台任务
		SystemTaskLoader taskLoader = new SystemTaskLoader();
		taskLoader.loadTask(mpTask);
		
		return _returnSuccess;
	}
	
	/**
	 * 禁用后台任务
	 * @param taskId -- 任务ID
	 * @return
	 */
	public String disable(String taskId) {
		String sql = "update task_base set task_state = '3' where task_id = ?";
		
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(taskId);
		
		if (!_dao.update(param)) {
			//"修改后台任务【{0}】的状态为禁用失败！"
			setMessage(JsMessage.getValue("taskplanbo.diserror"), taskId);
			return _returnFaild;
		}
		
		//禁用后台任务
		SystemTaskLoader taskLoader = new SystemTaskLoader();
		taskLoader.disableTask(taskId);
		
		Map<String,String> mpTask = getTask(taskId);
		String taskName = mpTask.get("task_name");
		_log.showDebug("disable task: " + taskName + " ...");
		
		return _returnSuccess;
	}

	/**
	 * 生成执行计划
	 * @param curDate -- 开始时间
	 * @param datePlan -- 执行计划
	 * @return
	 */
	public String viewPlan(String curDate, String datePlan) {
		DatePlan plan = new DatePlan();
		
		Date startDate = null;
		try {
			startDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(curDate);
		} catch (ParseException e) {
			_log.showError(e);
			//"开始时间【{0}】格式不正确！"
			setMessage(JsMessage.getValue("taskplanbo.starterror"), curDate);
			return _returnFaild;
		}

		StringBuilder sbDate = new StringBuilder();
		try {
			for (int i = 0; i < 18; i++) {
				startDate = plan.nextValidDate(startDate, datePlan);
				
				String sdate = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(startDate);
				sbDate.append(sdate).append("\n");
			}
		} catch (TaskException e) {
			_log.showError(e);
			//"生成执行计划失败：{0}！"
			setMessage(JsMessage.getValue("taskplanbo.planerror"), e.getMessage());
			return _returnFaild;
		}
		_log.showDebug("执行计划：\n" + sbDate.toString());
		
		//返回信息到前台
		String json = StringUtil.strForJson(sbDate.toString());
		setReturnData("{plan:'"+ json +"'}");
		
		return _returnSuccess;
	}
	
	/**
	 * 取后台任务定义信息
	 * @param taskId -- 任务ID
	 * @return
	 */
	private Map<String,String> getTask(String taskId) {
		String sql = "select task_name, task_class, task_plan, task_state, has_log, log_num, run_date, task_id " +
					 "from task_base where task_id = ?";
		
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(taskId);
		
		return _dao.queryMap(param);
	}
}
