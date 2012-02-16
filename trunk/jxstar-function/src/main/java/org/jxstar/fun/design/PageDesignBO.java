/*
 * PageDesignBO.java 2009-10-13
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.fun.design;

import java.util.Map;

import org.jxstar.dao.DaoParam;
import org.jxstar.dao.util.BigFieldUtil;
import org.jxstar.service.BusinessObject;
import org.jxstar.service.define.DefineName;
import org.jxstar.util.MapUtil;
import org.jxstar.util.key.KeyCreator;
import org.jxstar.util.resource.JsMessage;

/**
 * 表格设计器的相关处理方法。
 *
 * @author TonyTan
 * @version 1.0, 2009-10-13
 */
public class PageDesignBO extends BusinessObject {
	private static final long serialVersionUID = 1L;

	/**
	 * 删除设计信息。
	 * @param funcId -- 功能ID
	 * @param pageType -- 页面类型
	 * @return
	 */
	public String deleteDesign(String funcId, String pageType) {
		if (funcId == null || funcId.length() == 0) {
			setMessage(JsMessage.getValue("pagedesign.funidisnull"));
			return _returnFaild;
		}
		if (pageType == null || pageType.length() == 0) {
			setMessage(JsMessage.getValue("pagedesign.pagetypeisnull"));
			return _returnFaild;
		}
		
		String sql = "delete from fun_design where fun_id = ? and page_type = ? ";

		DaoParam param = _dao.createParam(sql);
		param.addStringValue(funcId);
		param.addStringValue(pageType);
		param.setDsName(DefineName.DESIGN_NAME);
		
		boolean ret = _dao.update(param);
		if (!ret) {
			setMessage(JsMessage.getValue("pagedesign.dserror"));
			return _returnFaild;
		}
		
		return _returnSuccess;
	}
	
	/**
	 * 保存设计信息。
	 * @param funcId -- 功能ID
	 * @param pageType -- 页面类型
	 * @param content -- 设计信息
	 * @return
	 */
	public String saveDesign(String funcId, String pageType, String content) {		
		if (funcId == null || funcId.length() == 0) {
			setMessage(JsMessage.getValue("pagedesign.funidisnull"));
			return _returnFaild;
		}
		if (pageType == null || pageType.length() == 0) {
			setMessage(JsMessage.getValue("pagedesign.pagetypeisnull"));
			return _returnFaild;
		}
		
		boolean ret = false;
		if (hasDesign(funcId, pageType)) {
			ret = updateDesign(funcId, pageType, content);
		} else {
			ret = insertDesign(funcId, pageType, content);
		}
		
		if (!ret) {
			setMessage(JsMessage.getValue("pagedesign.sserror"));
			return _returnFaild;
		}
		
		return _returnSuccess;
	}
	
	/**
	 * 是否有设计信息
	 * @param funcId
	 * @param pageType
	 * @return
	 */
	private boolean hasDesign(String funcId, String pageType) {
		String sql = "select count(*) as cnt from fun_design where fun_id = ? and page_type = ?";
		DaoParam param = _dao.createParam(sql);
		param.setDsName(DefineName.DESIGN_NAME);
		param.addStringValue(funcId).addStringValue(pageType);
		Map<String,String> mpcnt = _dao.queryMap(param);
		
		return MapUtil.hasRecord(mpcnt);
	}
	
	//新增设计信息
	private boolean insertDesign(String funcId, String pageType, String content) {
		String sql = "insert into fun_design(design_id, fun_id, page_type) "+
		 "values(?, ?, ?)";

		//创建主键
		String keyId = KeyCreator.getInstance().createKey("fun_design");
		
		DaoParam param = _dao.createParam(sql);
		param.setDsName(DefineName.DESIGN_NAME);
		param.addStringValue(keyId).addStringValue(funcId).addStringValue(pageType);
		
		_dao.update(param);
		
		//保存大字段信息
		String usql = "update fun_design set page_content = ? where design_id = '"+ keyId +"'";
		
		return BigFieldUtil.updateStream(usql, content, DefineName.DESIGN_NAME);
	}
	
	//更新设计信息
	private boolean updateDesign(String funcId, String pageType, String content) {
		String sql = "update fun_design set page_content = ? " +
				"where fun_id = '"+ funcId +"' and page_type = '"+ pageType +"' ";
		
		return BigFieldUtil.updateStream(sql, content, DefineName.DESIGN_NAME);
	}
}
