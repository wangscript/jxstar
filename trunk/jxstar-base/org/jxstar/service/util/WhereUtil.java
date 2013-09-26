/*
 * WhereUtil.java 2010-11-10
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.service.util;

import java.util.Map;


import org.jxstar.service.BoException;
import org.jxstar.service.define.DefineDataManger;
import org.jxstar.util.StringUtil;
import org.jxstar.util.log.Log;
import org.jxstar.util.resource.JsMessage;

/**
 * 功能定义的查询语句WhereSql构建工具类。
 *
 * @author TonyTan
 * @version 1.0, 2010-11-10
 */
public class WhereUtil {
	private static Log _log = Log.getInstance();
	
	/**
	 * 取一个功能的基础查询语句，处理功能定义中的where、归档处理的where，
	 * 并处理where的自定义常量标志。
	 * @param funid -- 功能ID
	 * @return
	 */
	public static String queryBaseWhere(String funid) {
		//取功能定义信息
		DefineDataManger manger = DefineDataManger.getInstance();
		Map<String,String> mpFun = manger.getFunData(funid);
		
		StringBuilder sbWhere = new StringBuilder();
		
		//取where语句
		sbWhere.append(mpFun.get("where_sql"));
		
		//取归档where子句
		String isArchive = mpFun.get("is_archive");
		//普通查询加归档、高级查询不加归档
		String auditCol = mpFun.get("audit_col");
		if (isArchive.equals("1") && auditCol.length() > 0) {
			if (sbWhere.length() > 0) {
				sbWhere.append(" and ");
			}
			//取设置的业务状态值
			String audit0 = FunStatus.getValue(funid, "audit0", "0"), audit6 = "6";
			sbWhere.append(" (").append(auditCol).append(" = '"+ audit0 +"' or ");
			sbWhere.append(auditCol).append(" = '"+ audit6 +"') ");
		}
		
		String whereSql = sbWhere.toString();
		//解析wheresql子句中的参数{VALIDDATA}
		if (whereSql.length() > 0 && whereSql.indexOf("{VALIDDATA}") >= 0 && auditCol.length() > 0) {
			String flag = mpFun.get("valid_flag");
			flag = FunStatus.getValidStatus(funid, flag);//取真实状态值
			
			String validsql = auditCol + "='" + flag + "'";
			whereSql = whereSql.replace("{VALIDDATA}", validsql);
		}
		
		return whereSql;
	}
	
	/**
	 * 取功能完整的查询SQL，添加了功能定义中的where，归档处理的where，
	 * 数据权限where，并处理where的自定义常量标志。
	 * @param funid -- 功能定义ID
	 * @param userid -- 用户ID
	 * @param basewhere -- 前台where
	 * @param querytype -- 查询类型：1 -- 高级查询不处理归档 0 -- 普通查询处理归档
	 * @return
	 */
	public static String queryWhere(String funid, String userid, String basewhere, 
							String querytype) throws BoException {
		if (userid == null || userid.length() == 0) {
			throw new BoException(JsMessage.getValue("param.null.userid"));
		}
		if (funid == null || funid.length() == 0) {
			throw new BoException(JsMessage.getValue("param.null.funid"));
		}
		
		if (basewhere == null) basewhere = "";
		if (querytype == null) querytype = "0";
		
		//取功能定义信息
		DefineDataManger manger = DefineDataManger.getInstance();
		Map<String,String> mpFun = manger.getFunData(funid);
		
		//处理后的where
		StringBuilder sbWhere = new StringBuilder(basewhere);
		
		//取where语句
		String where = StringUtil.addkf(mpFun.get("where_sql"));
		if (where.length() > 0) {
			if (sbWhere.length() > 0) {
				sbWhere.append(" and ");
			}
			sbWhere.append(where);
		}
		
		//取归档where子句
		String isArchive = mpFun.get("is_archive");
		//普通查询加归档、高级查询不加归档
		String auditCol = mpFun.get("audit_col");
		if (isArchive.equals("1") && auditCol.length() > 0 && !querytype.equals("1")) {
			if (sbWhere.length() > 0) {
				sbWhere.append(" and ");
			}
			//取设置的业务状态值
			String audit0 = FunStatus.getValue(funid, "audit0", "0"), audit6 = "6";
			sbWhere.append(" (").append(auditCol).append(" = '"+ audit0 +"' or ");
			sbWhere.append(auditCol).append(" = '"+ audit6 +"') ");
		}
		
		//取数据权限where语句
		SysDataManager datamgr = SysDataManager.getInstance();
		String datasql = datamgr.getDataWhere(userid, funid);
		_log.showDebug("gridquery data right sql:" + datasql);
		if (datasql != null && datasql.length() > 0) {
			if (sbWhere.length() > 0) {
				sbWhere.append(" and ");
			}
			sbWhere.append(datasql);
		}
		
		String whereSql = sbWhere.toString();
		//解析wheresql子句中的参数{VALIDDATA}
		if (whereSql.length() > 0 && whereSql.indexOf("{VALIDDATA}") >= 0 && auditCol.length() > 0) {
			String flag = mpFun.get("valid_flag");
			flag = FunStatus.getValidStatus(funid, flag);//取真实状态值
			
			String validsql = auditCol + "='" + flag + "'";
			whereSql = whereSql.replace("{VALIDDATA}", validsql);
		}
		_log.showDebug("gridquery where sql:" + whereSql);
		
		return whereSql;
	}
	
	/**
	 * 取系统构建的的查询SQL，添加了归档处理的where，数据权限where，并处理其中的自定义常量标志，
	 * 不含功能定义where，暂时用于报表模板解析。
	 * 
	 * @param funid -- 功能定义ID
	 * @param userid -- 用户ID
	 * @param basewhere -- 前台where
	 * @param querytype -- 高级查询 1、普通查询 0
	 * @return
	 */
	public static String systemWhere(String funid, String userid, 
			String basewhere, String queryType) throws BoException {
		if (userid == null || userid.length() == 0) {
			throw new BoException(JsMessage.getValue("param.null.userid"));
		}
		if (funid == null || funid.length() == 0) {
			throw new BoException(JsMessage.getValue("param.null.funid"));
		}
		
		if (basewhere == null) basewhere = "";
		
		//取功能定义信息
		DefineDataManger manger = DefineDataManger.getInstance();
		Map<String,String> mpFun = manger.getFunData(funid);
		
		//解析wheresql子句中的参数{VALIDDATA}
		String auditCol = mpFun.get("audit_col");
		if (basewhere.length() > 0 && basewhere.indexOf("{VALIDDATA}") >= 0 && auditCol.length() > 0) {
			String flag = mpFun.get("valid_flag");
			flag = FunStatus.getValidStatus(funid, flag);//取真实状态值
			
			String validsql = auditCol + "='" + flag + "'";
			basewhere = basewhere.replace("{VALIDDATA}", validsql);
		}
		
		//处理后的where
		StringBuilder sbWhere = new StringBuilder(basewhere);
		
		//取归档where子句
		String isArchive = mpFun.get("is_archive");
		//普通查询加归档、高级查询不加归档
		if (isArchive.equals("1") && auditCol.length() > 0 && !queryType.equals("1")) {
			if (sbWhere.length() > 0) {
				sbWhere.append(" and ");
			}
			//取设置的业务状态值
			String audit0 = FunStatus.getValue(funid, "audit0", "0"), audit6 = "6";
			sbWhere.append(" (").append(auditCol).append(" = '"+ audit0 +"' or ");
			sbWhere.append(auditCol).append(" = '"+ audit6 +"') ");
		}
		
		//取数据权限where语句
		SysDataManager datamgr = SysDataManager.getInstance();
		String datasql = datamgr.getDataWhere(userid, funid);
		_log.showDebug("gridquery data right sql:" + datasql);
		if (datasql != null && datasql.length() > 0) {
			if (sbWhere.length() > 0) {
				sbWhere.append(" and ");
			}
			sbWhere.append(datasql);
		}
		
		String whereSql = sbWhere.toString();
		_log.showDebug("system where sql:" + whereSql);
		
		return whereSql;
	}
}
