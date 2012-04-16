/*
 * GridSaveEvent.java 2009-5-29
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.service.event;

import java.util.List;
import java.util.Map;


import org.jxstar.control.action.RequestContext;
import org.jxstar.service.BoException;
import org.jxstar.service.BusinessEvent;
import org.jxstar.service.util.ServiceUtil;
import org.jxstar.util.MapUtil;
import org.jxstar.util.factory.FactoryUtil;
import org.jxstar.util.resource.JsMessage;
import org.jxstar.util.resource.JsParam;

/**
 * 业务记录在表格中的保存事件。
 *
 * @author TonyTan
 * @version 1.0, 2009-5-29
 */
public class GridSaveEvent extends BusinessEvent {
	private static final long serialVersionUID = 1L;

	/**
	 * 执行保存方法
	 */
	public String gridSave(RequestContext requestContext) {
		try {
			init(requestContext);
		} catch (BoException e) {
			_log.showError(e);
			return _returnFaild;
		}
		
		//判断是否定义表格编辑字段
		String[] gucols = _funObject.getUpdateCol();
		if (gucols == null || gucols.length == 0) {
			setMessage(JsMessage.getValue("functionbm.geditcolnull"));
			return _returnFaild;
		}
		
		//取值数组对象
		List<Map<String,String>> lsVals = ServiceUtil.getRequestMaps(
				requestContext, gucols);
		if (lsVals.isEmpty()) {
			//保存的记录为空
			setMessage(JsMessage.getValue("functionbm.savekeynull"));
			return _returnFaild;
		}
		
		//保存新增的记录ID
		List<String> lsKeyId = FactoryUtil.newList(); 
		StringBuilder sbRetKey = new StringBuilder("[");
		for (int i = 0; i < lsVals.size(); i++) {
			Map<String,String> mpVal = lsVals.get(i);
			String sKeyValue = MapUtil.getValue(mpVal, _pkColName);
			
			try {
				if (sKeyValue.length() == 0) {
					CreateEvent ce = new CreateEvent();
					ce.init(requestContext);
					String keyId = ce.create(lsVals.get(i), requestContext);
					if (keyId.length() == 0) {
						setMessage(JsMessage.getValue("functionbm.newkeynull"));
						return _returnFaild;
					} else {
						sbRetKey.append("{index:'"+ i +"', keyid:'"+ keyId +"'},");
						lsKeyId.add(keyId);
					}
				} else {
					SaveEvent se = new SaveEvent();
					se.init(requestContext);
					if (!se.save(lsVals.get(i), sKeyValue, requestContext)) {
						setMessage(se.getMessage());
						return _returnFaild;
					}
				}
			} catch (BoException e) {
				_log.showError(e);
				setMessage(e.getMessage());
				return _returnFaild;
			}
		}
		if (sbRetKey.length() > 1) {
			sbRetKey.setCharAt(sbRetKey.length()-1, ']');
		} else {
			sbRetKey.append("]");
		}
		//把新增主键值返回到前台
		requestContext.setReturnData(sbRetKey.toString());
		//把新增主键值存入环境参数中
		if (!lsKeyId.isEmpty()) {
			requestContext.setRequestValue(JsParam.KEYID, 
					lsKeyId.toArray(new String[lsKeyId.size()]));
		}

		return _returnSuccess;
	}
}
