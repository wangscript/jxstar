/*
 * WarnPlanBO.java 2011-3-18
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */

package org.jxstar.service.warn;

import org.jxstar.dao.DaoParam;
import org.jxstar.dao.JsonDao;
import org.jxstar.service.BusinessObject;
import org.jxstar.util.ArrayUtil;
import org.jxstar.util.resource.JsMessage;

/**
 * 处理上报定义的状态。
 *
 * @author TonyTan
 * @version 1.0, 2011-3-18
 */
public class WarnPlanBO extends BusinessObject {
	private static final long serialVersionUID = -6792035603481733192L;
	
	/**
	 * 查找新上报消息返回到前台
	 * @param userId -- 用户ID
	 * @return
	 */
	public String queryNewWarn(String userId) {
		StringBuilder sbsql = new StringBuilder();
		String selectsql = "select fun_id, warn_id, warn_name, count(*) as warn_num from warn_assign ";
		
		sbsql.append(selectsql);
		sbsql.append("where user_id = ? and is_assign = '1' group by fun_id, warn_id, warn_name");

		DaoParam param = _dao.createParam(sbsql.toString());
		param.addStringValue(userId);
		
		//取查询字段名
		String[] cols = ArrayUtil.getGridCol(selectsql);
		
		//查询页面数据
		JsonDao jsonDao = JsonDao.getInstance();
		String strJson = jsonDao.query(param, cols);
		
		//查询SQL异常
		if (strJson == null) {
			_log.showWarn("simplequery error!");
			setMessage(JsMessage.getValue("web.query.error"));
			return _returnFaild;
		}
		
		StringBuilder sbJson = new StringBuilder("{root:[").append(strJson + "]}");
		//_log.showDebug("json=" + sbJson.toString());
		
		//返回查询数据
		setReturnData(sbJson.toString());
		
		return _returnSuccess;
	}
	
	/**
	 * 删除执行完的上报消息失败
	 * @param funId -- 功能ID
	 * @param dataId -- 数据ID
	 * @return
	 */
	public String deleteEndAssign(String funId, String dataId) {
		String sql = "delete from warn_assign where fun_id = ? and data_id = ? ";
		
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(funId);
		param.addStringValue(dataId);
		
		if (!_dao.update(param)) {
			//"删除执行完的上报消息失败！"
			setMessage(JsMessage.getValue("warnplanbo.delerror"));
			return _returnFaild;
		}
		
		return _returnSuccess;
	}

	/**
	 * 启用并加载上报
	 * @param warnId -- 上报ID
	 * @return
	 */
	public String valid(String warnId) {
		String sql = "update warn_base set run_state = '2' where warn_id = ?";
		
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(warnId);
		
		if (!_dao.update(param)) {
			//"修改后台上报【{0}】的状态为生效失败！"
			setMessage(JsMessage.getValue("warnplanbo.useerror"), warnId);
			return _returnFaild;
		}
		
		return _returnSuccess;
	}
	
	/**
	 * 禁用上报
	 * @param warnId -- 上报ID
	 * @return
	 */
	public String disable(String warnId) {
		String sql = "update warn_base set run_state = '3' where warn_id = ?";
		
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(warnId);
		
		if (!_dao.update(param)) {
			//"修改后台上报【{0}】的状态为禁用失败！"
			setMessage(JsMessage.getValue("warnplanbo.diserror"), warnId);
			return _returnFaild;
		}
		
		return _returnSuccess;
	}
}
