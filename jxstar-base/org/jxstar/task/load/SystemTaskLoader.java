/*
 * SystemTaskLoader.java 2011-1-13
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.task.load;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jxstar.dao.BaseDao;
import org.jxstar.dao.DaoParam;
import org.jxstar.task.SystemLoader;
import org.jxstar.task.SystemTask;
import org.jxstar.util.MapUtil;
import org.jxstar.util.factory.FactoryUtil;
import org.jxstar.util.factory.SystemFactory;

/**
 * 后台任务加载管理器。
 *
 * @author TonyTan
 * @version 1.0, 2011-1-13
 */
public class SystemTaskLoader extends SystemLoader {
	//检查间隔时间，单位秒
	private static int _checkTime = 60;
	//保存所有加载的后台任务
	private static Map<String, SystemTask> _mpSystemTask = FactoryUtil.newMap();
	
	/**
	 * 加载所有后台任务
	 */
	protected void load() {
		//检查间隔时间，单位秒
		String checkTime = MapUtil.getValue(_initParam, "check_time", "60");
		_checkTime = Integer.parseInt(checkTime);
		
		//查询生效的后台任务
		List<Map<String,String>> lsTask = queryTask();
		if (lsTask.isEmpty()) {
			_log.showInfo("no define system task...");
			return;
		}
		
		//加载后台任务
		for (int i = 0, n = lsTask.size(); i < n; i++) {
			Map<String,String> mpTask = lsTask.get(i);
			
			loadTask(mpTask);
		}
	}
	
	/**
	 * 加载后台任务
	 * @param mpTask -- 任务定义信息
	 */
	public void loadTask(Map<String,String> mpTask) {
		String taskName = mpTask.get("task_name");
		_log.showDebug("load task: " + taskName + " ...");
		
		String taskClass = mpTask.get("task_class");
		SystemTask task = (SystemTask) SystemFactory.createObject(taskClass);
		if (task == null) {
			_log.showDebug("load task: " + taskName + " is null!");
			return;
		}
		
		String taskId = mpTask.get("task_id");
		
		synchronized(_mpSystemTask) {
			//先清除该线程
			disableTask(taskId);
			
			//保存后台任务
			_mpSystemTask.put(taskId, task);
			
			//启动后台任务
			task.init(_checkTime, mpTask);
			task.start();
		}
	}
	
	/**
	 * 注销线程
	 * @param taskId -- 任务ID
	 */
	public void disableTask(String taskId) {
		synchronized(_mpSystemTask) {
			SystemTask task = _mpSystemTask.get(taskId);
			if (task == null) return;
			_log.showDebug("disable task: " + task.getTaskName() + "...");
			
			//中断线程
			task.setDisabled(true);
			
			//从任务组中删除
			_mpSystemTask.remove(taskId);
			task = null;
		}
	}
	
	/**
	 * 在程序退出时注销所有线程，在servlet.destroy时调用
	 * 为防止在WebSphere中重复启动相同的定时任务线程
	 */
	public void destroyAllThread() {
		//能查到当前JVM中所有的活动线程，其它WEB应用中的线程也可以查到
		//通过thread instanceof SystemTask可以过滤为只处理当前WEB应用的定时任务
		Map<Thread, StackTraceElement[]> mpThread = Thread.getAllStackTraces();
		if (mpThread == null || mpThread.isEmpty()) return;
		
		Iterator<Thread> itr = mpThread.keySet().iterator();
		
		while(itr.hasNext()) {
			Thread thread = itr.next();
			
			String className = thread.getClass().toString();
			if (thread instanceof SystemTask || className.indexOf("org.jxstar.") >= 0) {
				thread.interrupt();
				System.out.println("..............thread.hashcode is " + thread.hashCode() + "; " + className + " is destroy!!");
			}
		}
	}

	/**
	 * 取所有生效的后台任务设置信息
	 * @return
	 */
	private List<Map<String,String>> queryTask() {
		String sql = "select task_name, task_class, task_plan, task_state, has_log, log_num, run_date, task_id " +
				 	 "from task_base where task_state = '2'";
		
		BaseDao _dao = BaseDao.getInstance();
		DaoParam param = _dao.createParam(sql);
		
		return _dao.query(param);
	}
}
