/*
 * AuditEvent.java 2009-5-29
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
import org.jxstar.service.define.FunDefineDao;
import org.jxstar.service.define.FunctionDefine;
import org.jxstar.util.DateUtil;
import org.jxstar.util.MapUtil;
import org.jxstar.util.StringUtil;
import org.jxstar.util.resource.JsMessage;
import org.jxstar.util.resource.JsParam;

/**
 * 业务记录提交事件。
 *
 * @author TonyTan
 * @version 1.0, 2009-5-29
 */
public class AuditEvent extends BusinessEvent {
	private static final long serialVersionUID = 8033698043346942238L;

	/**
	 * 执行提交方法
	 */
	public String audit(RequestContext requestContext) {
		try {
			init(requestContext);
		} catch (BoException e) {
			_log.showError(e);
			return _returnFaild;
		}
		
		//取复核值：1 表示复核，0 表示取消复核
		String auditVal = requestContext.getRequestValue("auditvalue", "1");
		
		String[] asKey = requestContext.getRequestValues(JsParam.KEYID);
		if (asKey == null || asKey.length == 0) {
			//找不到提交记录的键值！
			setMessage(JsMessage.getValue("functionbm.auditkeynull"));
			return _returnFaild;
		}		
		
		//取提交的SQL语句, 如果更新用户信息则SQL会增加两个信息字段, 最后一个?是主键字段
		String auditSql = _funObject.getAuditSQL();		
		//取提交的参数值
		String value = auditVal + ";";
		//取提交的参数值数据类型
		String type = "string;string";
		
		//给修改时间与修改人赋值
		String hasUser = _funObject.getElement("is_userinfo");
		if (hasUser.equals("1")) {
			value = auditVal + ";" + DateUtil.getTodaySec() + ";" + MapUtil.getValue(_userInfo, "user_id") + ";";
			type = "string;date;string;string";
		}
		
		//输出调试信息
		_log.showDebug("audit sql=" + auditSql);
		_log.showDebug("audit type=" + type);
		_log.showDebug("audit value=" + value);
		
		//执行提交语句
		for (int i = 0; i < asKey.length; i++) {
			String sKeyID = asKey[i];
			
			//判断是否有明细记录
			if (!checkDetailData(sKeyID)) return _returnFaild;	
			
			//给汇总字段赋值
			statColValue(sKeyID);
			sKeyID = value + sKeyID;
			DaoParam param = _dao.createParam(auditSql);
			param.setValue(sKeyID).setType(type).setDsName(_dsName);
			if (!_dao.update(param)) {
				_log.showDebug("------audit=" + sKeyID);
				//提交记录失败！
				setMessage(JsMessage.getValue("functionbm.auditfaild"));
				return _returnFaild;				
			}
		}
		
		return _returnSuccess;
	}
	
	/**
	 * 检查子功能是否有明细数据.
	 * 
	 * @param sKeyID - 当前记录主键值
	 * @return boolean
	 */
	private boolean checkDetailData(String sKeyID) {
		String sSubID = _funObject.getElement("val_subid");
		if (sSubID.trim().length() == 0) {//没有要检查的子功能
			return true;
		}
		
		FunctionDefine subfun = null;
		String[] aSubID = sSubID.split(",");
		//分别检查单个子功能的数据
		for (int i = 0; i < aSubID.length; i++) {
			//创建子功能对象
			subfun = _funManger.getDefine(aSubID[i]);
			if (subfun == null) {
				_log.showWarn("create sub function object fiald, funid:{0}! ", aSubID[0]);
				return false;
			}
			String sFunName = subfun.getElement("fun_name");
			
			//如果该子功能定义了外键,则取外键字段名,如果没有定义则采用主表的主键名
			String fkCol = subfun.getElement("fk_col");
			if (fkCol.length() == 0) {
				fkCol = StringUtil.getNoTableCol(_pkColName);
			}
			
			//构建查询子功能数据的SQL
			StringBuilder subsql = new StringBuilder("select count(*) as cnt from ")
				.append(subfun.getElement("table_name"))
				.append(" where " + fkCol + " like ?");
			_log.showDebug("select subfun sql=" + subsql.toString());
			
			DaoParam param = _dao.createParam(subsql.toString());
			param.addStringValue(sKeyID).setDsName(_dsName);
			Map<String,String> mpCnt = _dao.queryMap(param);
			if (!MapUtil.hasRecord(mpCnt)) {
				//返回信息：子功能{0}没有数据，必须填写！
				setMessage(JsMessage.
						getValue("functionbm.checksubval", sFunName));
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * 给汇总字段赋值
	 * 
	 * @param sKeyID - 当前记录ID
	 */
	private void statColValue(String sKeyID) {
		//获取统计字段定义信息
		List<Map<String, String>> lsStatCol = FunDefineDao.queryStatCol(_funID);
		if (lsStatCol == null || lsStatCol.isEmpty()) return; 
			
		for (int i = 0; i < lsStatCol.size(); i++) {
			Map<String,String> mpStatCol = lsStatCol.get(i);
			String sUpdateCol = (String) mpStatCol.get("col_code");
			String sStatTable = (String) mpStatCol.get("stat_tables");
			String sStatCol   = (String) mpStatCol.get("stat_col");
			String sStatFkcol = (String) mpStatCol.get("stat_fkcol");
			String sStatWhere = (String) mpStatCol.get("stat_where");
			
			StringBuilder sbStatSQL = new StringBuilder("select sum("+sStatCol+") as val ");
				sbStatSQL.append(" from ").append(sStatTable);
				sbStatSQL.append(" where ").append(sStatWhere);
				sbStatSQL.append(" and "+sStatFkcol+" = ?");
			_log.showDebug("stat col sql=" + sbStatSQL.toString());
			
			DaoParam param = _dao.createParam(sbStatSQL.toString());
			param.setValue(sKeyID).setDsName(_dsName);
			Map<String,String> mpSum = _dao.queryMap(param);
			if (mpSum != null && !mpSum.isEmpty()) {
				String sVal = (String) mpSum.get("val");
				_log.showDebug("update stat col val:{0}", sVal+";"+sKeyID);
				
				StringBuilder sbUpdate = new StringBuilder("update ");
					sbUpdate.append(_tableName + " set ");
					sbUpdate.append(sUpdateCol + " = ? where ");
					sbUpdate.append(_pkColName + " = ? ");
				_log.showDebug("update stat col sql=" + sbUpdate.toString());
				
				DaoParam param1 = _dao.createParam(sbUpdate.toString());
				param1.addStringValue(sVal).addStringValue(sKeyID).setDsName(_dsName);
				_dao.update(param1);
			}
		}
	}
}
