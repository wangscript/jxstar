/*
 * Copyright(c) 2012 Donghong Inc.
 */
package org.jxstar.service.event;

import java.util.List;
import java.util.Map;

import org.jxstar.control.action.RequestContext;
import org.jxstar.dao.DaoParam;
import org.jxstar.service.BusinessObject;
import org.jxstar.service.define.FunDefineDao;
import org.jxstar.util.MapUtil;

/**
 * 明细表保存时自动更新统计字段值。
 *
 * @author TonyTan
 * @version 1.0, 2012-7-17
 */
public class SubStatBO extends BusinessObject {
	private static final long serialVersionUID = 1L;
	
	/**
	 * 在明细表新增、保存、删除后调用
	 * @param request
	 * @return
	 */
	public String subStat(RequestContext request) {
		//外键值
		String fkValue = request.getRequestValue("fkValue");
		//父功能ID
		String pfunId = request.getRequestValue("pfunid");
		if (pfunId.length() == 0 || fkValue.length() == 0) {
			_log.showDebug("........sub stat param: parentFunId is null or fkValue is null!!");
			return _returnSuccess;
		}
		
		String json = subStat(fkValue, pfunId);
		setReturnData(json);
		
		return _returnSuccess;
	}
	
	/**
	 * 在主表提交后调用，暂时未用，直接嵌入到AuditEvent中了。
	 * @param request
	 * @return
	 */
	public String preAudit(RequestContext request) {
		//功能ID
		String funId = request.getFunID();
		//主键值
		String keyValue = request.getRequestValue("keyid");
		
		String json = subStat(keyValue, funId);
		setReturnData(json);
		
		return _returnSuccess;
	}
	
	/**
	 * 计算统计字段值
	 * @param keyValue
	 * @param funId
	 * @return
	 */
	public String subStat(String keyValue, String funId) {
		String retJson = "";
		if (keyValue == null || keyValue.length() == 0 || 
			keyValue == null || funId.length() == 0) return retJson;
		
		//取统计字段定义
		List<Map<String,String>> lsCol = queryStatCol(funId);
		if (lsCol.isEmpty()) {
			_log.showDebug("...........not define stat col!");
			return retJson;
		}
		
		//取表名与主键
		Map<String,String> mpFun = FunDefineDao.queryFun(funId);
		String table = mpFun.get("table_name");
		String pkCol = mpFun.get("pk_col");
		
		//执行统计
		StringBuilder sbjson = new StringBuilder();
		for(Map<String,String> mpCol : lsCol) {
			String statCol = mpCol.get("col_code");
			String value = statValue(keyValue, mpCol);
			_log.showDebug("...........stat value:" + value + "; stat col:" + statCol);
			
			updateStat(keyValue, value, table, pkCol, statCol);
			
			sbjson.append(statCol.replace(".", "__") + ":" + value + ",");
		}
		
		//返回统计数据到前台
		if (sbjson.length() > 0) {
			retJson = "{" + sbjson.substring(0, sbjson.length()-1) + "}";
		}
		_log.showDebug("...........return json:" + retJson);
		
		return retJson;
	}
	
	/**
	 * 取父功能的统计字段定义信息
	 * @param pfunId
	 * @return
	 */
	private List<Map<String,String>> queryStatCol(String pfunId) {
		StringBuilder sql = new StringBuilder("select stat_col, ");
		sql.append("stat_tables, stat_fkcol, stat_where, col_code ");
		sql.append("from fun_colext, fun_col ");
		sql.append("where stat_col > ' ' and stat_tables > ' ' and stat_fkcol > ' ' and ");
		sql.append("fun_col.col_id = fun_colext.col_id and fun_col.fun_id = ?");
		
		DaoParam param = _dao.createParam(sql.toString());
		param.addStringValue(pfunId);
		return _dao.query(param);
	}
	
	/**
	 * 获取统计值
	 * @param fkValue
	 * @param mpStat
	 * @return
	 */
	private String statValue(String fkValue, Map<String,String> mpStat) {
		String stat_col = mpStat.get("stat_col");
		String stat_tables = mpStat.get("stat_tables");
		String stat_fkcol = mpStat.get("stat_fkcol");
		String stat_where = MapUtil.getValue(mpStat, "stat_where").trim();
		
		StringBuilder sbsql = new StringBuilder("select ");
		sbsql.append("sum(" + stat_col + ") as stat ");
		sbsql.append("from " + stat_tables + " where ");
		sbsql.append(stat_fkcol + " = ? ");
		if (stat_where.length() > 0) {
			sbsql.append(" and (" + stat_where + ") ");
		}
		_log.showDebug("...........stat sub data sql=" + sbsql.toString());
		
		DaoParam param = _dao.createParam(sbsql.toString());
		param.addStringValue(fkValue);
		Map<String,String> mp = _dao.queryMap(param);
		
		return MapUtil.getValue(mp, "stat", "0");
	}
	
	/**
	 * 更新统计字段值
	 * @param fkValue
	 * @param statValue
	 * @param table
	 * @param pkCol
	 * @param statCol
	 * @return
	 */
	private boolean updateStat(String fkValue, String statValue, 
			String table, String pkCol, String statCol) {
		StringBuilder sbsql = new StringBuilder("update ");
		sbsql.append(table + " set " + statCol + " = ? where ");
		sbsql.append(pkCol + " = ?");
		_log.showDebug("...........update stat sub data sql=" + sbsql.toString());
		
		DaoParam param = _dao.createParam(sbsql.toString());
		param.addStringValue(statValue);
		param.addStringValue(fkValue);
		
		return _dao.update(param);
	}
}
