/*
 * CreateEvent.java 2009-5-29
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.service.event;

import java.util.Map;


import org.jxstar.control.action.RequestContext;
import org.jxstar.service.BoException;
import org.jxstar.service.BusinessEvent;
import org.jxstar.service.util.ServiceUtil;
import org.jxstar.util.MapUtil;
import org.jxstar.util.key.CodeCreator;
import org.jxstar.util.resource.JsMessage;
import org.jxstar.util.resource.JsParam;

/**
 * 业务记录新增事件。
 *
 * @author TonyTan
 * @version 1.0, 2009-5-29
 */
public class CreateEvent extends BusinessEvent {
	private static final long serialVersionUID = 1L;

	/**
	 * 执行新增方法
	 */
	public String create(RequestContext requestContext) {
		try {
			init(requestContext);
		} catch (BoException e) {
			_log.showError(e);
			return _returnFaild;
		}
		//参数对象
		String[] icols = _funObject.getInsertCol();
		Map<String,String> mpValue = ServiceUtil.getRequestMap(requestContext, icols);
		
		//新增记录
		String sKeyID = "";
		try {
			sKeyID = create(mpValue, requestContext);
			if (sKeyID.length() == 0) {
				setMessage(JsMessage.getValue("functionbm.newkeynull"));
				return _returnFaild;
			}
		} catch (BoException e) {
			//_log.showError(e);
			setMessage(e.getMessage());
			return _returnFaild;
		}
		
		
		//把新增主键值返回到前台
		requestContext.setReturnValue(JsParam.KEYID, sKeyID);
		//把新增主键值存入环境参数中
		requestContext.setRequestValue(JsParam.KEYID, sKeyID);
		return _returnSuccess;
	}
	
	/**
	 * 新增记录。
	 * @param mpValue -- 记录值
	 * @param requestContext -- 环境对象
	 * @return
	 */
	public String create(Map<String,String> mpValue, 
							RequestContext requestContext) throws BoException {
		//取外键字段与外键值
		String fkCol = _funObject.getElement("fk_col");
		if (fkCol.length() > 0) {
			//如果外键名没有添加表名，则添加
			if (fkCol.indexOf(".") < 0) {
				fkCol = _tableName + "." + fkCol;
			}
			//取外键值			
			String fkValue = MapUtil.getValue(mpValue, fkCol);
			if (fkValue.length() == 0) {
				fkValue = requestContext.getRequestValue("fkValue");
				if (fkValue.length() == 0) {
					throw new BoException(JsMessage.getValue("be.nofkvalue"));
				}
				mpValue.put(fkCol, fkValue);
			}
		}
		
		//判断是否有重复值
		ServiceUtil.isRepeatVal("", mpValue, requestContext);
		
		//创建主键值
		String sKeyID = ServiceUtil.createPkValue(mpValue, requestContext);
		if (sKeyID == null || sKeyID.length() == 0) {
			//新增记录时生成的主键值为空！
			throw new BoException(JsMessage.getValue("functionbm.newkeynull"));
		}
		mpValue.put(_pkColName, sKeyID);
		
		//创建编码值
		if (_codeColName != null && _codeColName.length() > 0) {
			String sCode = MapUtil.getValue(mpValue, _codeColName);
			if (sCode.length() == 0) {
				sCode = CodeCreator.getInstance().createCode(_funID, mpValue);
				if (sCode == null || sCode.length() == 0) {
					//新增记录时生成的单据编码为空！
					throw new BoException(JsMessage.getValue("functionbm.newcodenull"));
				}
				mpValue.put(_codeColName, sCode);
				//把编码值传递前台
				requestContext.setReturnValue(_codeColName, sCode);
			}
		}
		//_log.showDebug("=========userinfo=" + _userInfo);
		//_log.showDebug("=========currdate=" + DateUtil.getTodaySec());
		
		if (!ServiceUtil.insertRow(mpValue, _userInfo, _funObject)) {
			//新增记录失败！
			throw new BoException(JsMessage.getValue("functionbm.newfaild"));
		}
		
		_log.showDebug("insert success, keyid is " + sKeyID);
		return sKeyID;
	}
}
