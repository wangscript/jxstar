/*
 * SaveEvent.java 2009-5-29
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.service.event;

import java.util.Map;


import org.jxstar.control.action.RequestContext;
import org.jxstar.dao.DaoParam;
import org.jxstar.dao.DmDao;
import org.jxstar.service.BoException;
import org.jxstar.service.BusinessEvent;
import org.jxstar.service.util.ServiceUtil;
import org.jxstar.util.ArrayUtil;
import org.jxstar.util.DateUtil;
import org.jxstar.util.MapUtil;
import org.jxstar.util.config.SystemVar;
import org.jxstar.util.resource.JsMessage;
import org.jxstar.util.resource.JsParam;

/**
 * 业务记录保存事件。
 *
 * @author TonyTan
 * @version 1.0, 2009-5-29
 */
public class SaveEvent extends BusinessEvent {
	private static final long serialVersionUID = 1L;

	/**
	 * 执行保存方法
	 */
	public String save(RequestContext requestContext) {
		try {
			init(requestContext);
		} catch (BoException e) {
			_log.showError(e);
			return _returnFaild;
		}
		//取主键值
		String sKeyValue = requestContext.getRequestValue(JsParam.KEYID);
		if (sKeyValue == null || sKeyValue.length() == 0) {
			//保存的键值为空！
			setMessage(JsMessage.getValue("functionbm.savekeynull"));
			return _returnFaild;
		}
		
		//被修改的记录值
		Map<String,String> mpValue = ServiceUtil.getDirtyData(
				requestContext.getRequestMap(), _tableName);
		if (mpValue.isEmpty()) {
			setMessage(JsMessage.getValue("functionbm.savenodirty"));
			return _returnFaild;
		}
		
		try {
			dirtySave(mpValue, sKeyValue, requestContext);
		} catch (BoException e) {
			//_log.showError(e); 这类提示异常不用输出到日志
			setMessage(e.getMessage());
			return _returnFaild;
		}
		
		return _returnSuccess;
	}
	
	/**
	 * 只保存脏数据：取出修改了的数据与字段名，构建更新SQL。
	 * @param mpValue
	 * @param sKeyValue
	 * @param requestContext
	 * @return
	 */
	public boolean dirtySave(Map<String,String> mpValue, String sKeyValue, 
			RequestContext requestContext) throws BoException {
		//判断是否有重复值
		ServiceUtil.isRepeatVal(sKeyValue, mpValue, requestContext);	

		//给修改时间与修改人赋值
		String hasUser = _funObject.getElement("is_userinfo");
		if (hasUser.equals("1")) {
			String modUserCol = SystemVar.getValue("sys.field.mod_ufield", "modify_userid");
			String modDateCol = SystemVar.getValue("sys.field.mod_dfield", "modify_date");
			
			mpValue.put(modDateCol, DateUtil.getTodaySec());
			mpValue.put(modUserCol, MapUtil.getValue(_userInfo, "user_id"));
		}
		
		//保存记录
		if (!DmDao.update(_tableName, sKeyValue, mpValue)) {
			throw new BoException(JsMessage.getValue("functionbm.savefaild"));	
		}
		
		_log.showDebug("save success, current keyid is " + sKeyValue);
		return true;
	}
	
	/**
	 * 保存数据：从功能定义表中取所有更新字段保存数据。Grid保存用。
	 * @param mpValue -- 数据值
	 * @param sKeyValue -- 主键值
	 * @param requestContext -- 环境对象
	 * @return boolean
	 */
	public boolean save(Map<String,String> mpValue, String sKeyValue, 
			RequestContext requestContext) throws BoException {
		//判断是否有重复值
		ServiceUtil.isRepeatVal(sKeyValue, mpValue, requestContext);	
		
		//取保存的SQL语句, 如果更新用户信息则SQL会增加两个信息字段, 最后一个?是主键字段
		String saveSql = _funObject.getUpdateSQL();
		//取保存的记录值
		String[] value = ServiceUtil.requestMapToArray(
				mpValue, _funObject.getUpdateCol());
		if (value == null || value.length == 0) {
			//保存的记录值为空！
			throw new BoException(JsMessage.getValue("functionbm.savevaluenull"));
		}
		
		//给修改时间与修改人赋值
		String hasUser = _funObject.getElement("is_userinfo");
		if (hasUser.equals("1")) {
			//如果保存用户信息, 约定最后第三、二个字段为修改时间与修改人ID
			int len = value.length;
			value[len - 3] = DateUtil.getTodaySec();
			value[len - 2] = MapUtil.getValue(_userInfo, "user_id");
		}
		
		//取保存的值数据类型
		String[] type = _funObject.getUpdateParamType();
		//输出调试信息
		_log.showDebug("save sql=" + saveSql);
		_log.showDebug("save type=" + ArrayUtil.arrayToString(type));
		_log.showDebug("save value=" + ArrayUtil.arrayToString(value));
		
		//执行保存
		DaoParam param = _dao.createParam(saveSql);
		param.setValues(value).setTypes(type).setDsName(_dsName);
		if (!_dao.update(param)) {
			//保存记录失败！
			throw new BoException(JsMessage.getValue("functionbm.savefaild"));	
		}
		
		_log.showDebug("save success, current keyid is " + sKeyValue);
		return true;
	}
}
