/*
 * DeleteEvent.java 2009-5-29
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.service.event;


import java.util.List;
import java.util.Map;

import org.jxstar.control.action.RequestContext;
import org.jxstar.dao.DaoParam;
import org.jxstar.service.BoException;
import org.jxstar.service.BusinessEvent;
import org.jxstar.service.define.FunctionDefine;
import org.jxstar.service.studio.AttachBO;
import org.jxstar.util.StringUtil;
import org.jxstar.util.resource.JsMessage;
import org.jxstar.util.resource.JsParam;

/**
 * 业务记录删除事件。
 *
 * @author TonyTan
 * @version 1.0, 2009-5-29
 */
public class DeleteEvent extends BusinessEvent {
	private static final long serialVersionUID = 6950498972296811537L;

	/**
	 * 执行删除方法
	 */
	public String delete(RequestContext requestContext) {
		try {
			init(requestContext);
		} catch (BoException e) {
			_log.showError(e);
			return _returnFaild;
		}
		
		String[] asKey = requestContext.getRequestValues(JsParam.KEYID);
		if (asKey == null || asKey.length == 0) {
			//找不到删除记录的键值！
			setMessage(JsMessage.getValue("functionbm.deletekeynull"));
			return _returnFaild;
		}
		
		//选择的记录中存在已删除的记录，请刷新数据后再操作
		if (checkDeleted(asKey)) {
			setMessage(JsMessage.getValue("functionbm.deletenum"));
			return _returnFaild;
		}
		
		//取删除SQL语句
		String delSql = _funObject.getDeleteSQL();
		
		DaoParam param;
		String funType = _funObject.getElement("reg_type");
		for (int i = 0; i < asKey.length; i++) {
			String sKeyID = asKey[i];
			
			//如果为树形主功能,则删除下级数据
			if (funType.equals("treemain")) {
				delSql = delSql.replaceFirst("= \\?", "like \\?");
				param = _dao.createParam(delSql);
				param.addStringValue(sKeyID + "%").setDsName(_dsName);
			} else {
				param = _dao.createParam(delSql);
				param.addStringValue(sKeyID).setDsName(_dsName);
			}
			_log.showDebug("delete sql=" + delSql);
			_log.showDebug("delete keyid=" + sKeyID);
			
			//删除子记录
			if (!deleteSub(sKeyID)) {
				return _returnFaild;
			}
			
			//删除相关附件
			String tableName = _funObject.getElement("table_name");
			deleteAttach(tableName, sKeyID);
			
			//删除主记录
			if (!_dao.update(param)) {
				setMessage(JsMessage.getValue("functionbm.deletefaild"));//删除记录失败！
				return _returnFaild;
			}
		}
		
		return _returnSuccess;
	}
	
	/**
	 * 检查已经删除的记录，如果存在，则不能继续执行
	 * @param keyIds
	 * @return
	 */
	private boolean checkDeleted(String[] keyIds) {
		if (keyIds == null || keyIds.length == 0) return false;
		
		String pkcol = _funObject.getElement("pk_col");
		String table = _funObject.getElement("table_name");
		
		StringBuilder sb = new StringBuilder("select count(*) as cnt from ");
		sb.append(table).append(" where ").append(pkcol).append(" = ?");
		
		DaoParam param = _dao.createParam(sb.toString());
		for (String keyId : keyIds) {
			param.addStringValue(keyId);
			Map<String, String> mp = _dao.queryMap(param);
			
			int num = Integer.parseInt(mp.get("cnt"));
			if (num == 0) return true;
			
			param.clearParam();
		}
		
		return false;
	}
	
	/**
	 * 删除当前功能的子记录
	 * 
	 * @param sKeyID - 外键值
	 * @return boolean
	 */
	private boolean deleteSub(String sKeyID) {
		//取当前功能的子功能定义ID字符串
		String sSubID = _funObject.getElement("subfun_id");
		if (sSubID.length() == 0) {
			return true;
		}
		
		FunctionDefine subfun = null;
		String[] aSubID = sSubID.split(",");
		//分别删除单个子功能的数据
		for (int i = 0; i < aSubID.length; i++) {
			String subFunId = aSubID[i].trim();
			//创建子功能对象
			subfun = _funManger.getDefine(subFunId);
			if (subfun == null) {
				_log.showWarn("create sub function object fiald, funid:{0}! ", subFunId);
				return false;
			}
			
			//如果该子功能定义了外键,则取外键字段名,如果没有定义则采用主表的主键名
			String fkCol = subfun.getElement("fk_col");
			if (fkCol.length() == 0) {
				fkCol = StringUtil.getNoTableCol(_pkColName);
			}
			
			//构建删除子功能数据的SQL
			StringBuilder subdel = new StringBuilder("delete from ")
				.append(subfun.getElement("table_name"))
				.append(" where " + fkCol + " like ?");
			_log.showDebug("delete subfun sql=" + subdel.toString());
			
			DaoParam param = _dao.createParam(subdel.toString());
			param.addStringValue(sKeyID).setDsName(_dsName);
			if (!_dao.update(param)) {
				_log.showWarn("delete {0} data faild! ", subFunId);
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * 删除当前表记录的附件
	 * 
	 * @param tableName -- 业务表名
	 * @param dataId -- 记录ID
	 * @return
	 */
	private boolean deleteAttach(String tableName, String dataId) {
		String sql = "select attach_id from sys_attach where table_name = ? and data_id = ?";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(tableName).addStringValue(dataId).setDsName(_dsName);
		
		List<Map<String, String>> lsData = _dao.query(param);
		if (lsData.isEmpty()) return true;
		
		String[] attachIds = new String[lsData.size()];
		for (int i = 0; i < lsData.size(); i++) {
			attachIds[i] = lsData.get(i).get("attach_id");
		}
		
		//删除相关附件，不处理删除异常
		AttachBO attach = new AttachBO();
		attach.deleteAttach(attachIds);
		
		return true;
	}
}
