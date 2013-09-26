/*
 * FunctionDefineBuilder.java 2009-5-28
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.service.define;

import java.util.List;
import java.util.Map;

import org.jxstar.service.BoException;
import org.jxstar.service.util.FunStatus;
import org.jxstar.util.ArrayUtil;
import org.jxstar.util.MapUtil;
import org.jxstar.util.StringUtil;
import org.jxstar.util.config.SystemVar;
import org.jxstar.util.factory.FactoryUtil;

/**
 * 功能对象构建器，负责生成功能对象的各属性。
 * 
 * @author TonyTan
 * @version 1.0, 2009-5-28
 */
public class FunctionDefineBuilder {
	
	/**
	 * 创建指定功能ID的功能对象.
	 * @param funId
	 * @return
	 */
	public FunctionDefine build(String funId) throws BoException {
		DefineDataManger manger = DefineDataManger.getInstance();
		//取功能定义信息
		Map<String,String> mpFun = manger.getFunData(funId);
		if (mpFun == null || mpFun.isEmpty()) {
			throw new BoException("funid:" + funId + " function base info is null! ");
		}
		
		//取字段定义信息
		List<Map<String,String>> lsCol = manger.getColData(funId);
		
		return build(mpFun, lsCol);
	}

	/**
	 * 初始化功能对象.
	 * @param funObject
	 * @return
	 */
	private FunctionDefine build(Map<String,String> mpFun, 
			List<Map<String,String>> lsCol) {
		FunctionDefine funRet = new FunctionDefine();
		funRet.setFunID(mpFun.get("fun_id"));

		//赋值基本功能信息
		funRet.setBaseInfo(mpFun);
		
		//这种功能为特殊功能,不需要定义字段列表
		if (lsCol.isEmpty()) {
			return funRet;
		}		
		
		//当前功能的表名
		String sTableName = MapUtil.getValue(mpFun, "table_name");		
		
		//给查询子句赋值
		String whereSQL = MapUtil.getValue(mpFun, "where_sql");
		
		//解析where子句中的参数{VALIDDATA}
		String auditCol = MapUtil.getValue(mpFun, "audit_col");
		String flag = MapUtil.getValue(mpFun, "valid_flag");
		//取真实状态值
		flag = FunStatus.getValidStatus(mpFun.get("fun_id"), flag);
		if (whereSQL.indexOf("{VALIDDATA}") >= 0) {
			if (auditCol.length() > 0) {
				String validsql = auditCol + "='" + flag + "'";
				whereSQL = whereSQL.replace("{VALIDDATA}", validsql);
			}
		}
		if (whereSQL.length() == 0) {
			funRet.setWhereSQL("");
		} else {
			funRet.setWhereSQL("("+whereSQL+")");
		}	

		//查询字段列名数组与数据类型数组
		List<String> lsCols = FactoryUtil.newList();			
		List<String> lsParamTypes = FactoryUtil.newList();
		//新增字段列名数组与数据类型数组
		List<String> lsColi = FactoryUtil.newList();
		List<String> lsParamTypei = FactoryUtil.newList();
		//保存字段列名数组与数据类型数组
		List<String> lsColu = FactoryUtil.newList();
		List<String> lsParamTypeu = FactoryUtil.newList();
		//保存统计字段
		List<String> lsSums = FactoryUtil.newList();
		
		//循环从字段定义表中取值
		for (int i = 0; i < lsCol.size(); i++) {
			Map<String,String> mpCol = lsCol.get(i);
			
			String colCode = MapUtil.getValue(mpCol, "col_code").toLowerCase();
			String colType = MapUtil.getValue(mpCol, "data_type");
			String isupdate = MapUtil.getValue(mpCol, "is_update");
			//String isGridEdit = MapUtil.getStringVal(mpCol, "is_gridedit");
			
			lsCols.add(colCode);
			lsParamTypes.add(colType);
			//添加需要更新的字段
			if (isupdate.equals("1")) {
				lsColi.add(colCode);
				lsParamTypei.add(colType);
				
				lsColu.add(colCode);
				lsParamTypeu.add(colType);
			}
			
			//添加统计字段
			String issum = MapUtil.getValue(mpCol, "is_statcol");
			if (issum.equals("1")) {
				lsSums.add(colCode);
			}
		}
		
		//构建统计语句
		if (!lsSums.isEmpty()) {
			StringBuilder sql = new StringBuilder("select ");
			for (int i = 0, n = lsSums.size(); i < n; i++) {
				String col = lsSums.get(i);
				String colname = StringUtil.getNoTableCol(col);
				sql.append("sum("+col+") as "+ colname +",");//oracle中的别名长度不能超过30，添加别名方便过滤需要隐藏的字段
			}
			 String ssql = sql.substring(0, sql.length()-1);
			 ssql += " " + MapUtil.getValue(mpFun, "from_sql");
			 funRet.setSumSQL(ssql);
		}
		
		//是否保存用户信息
		String isSaveUser = MapUtil.getValue(mpFun, "is_userinfo");
		boolean isUserInfo = "1".equals(isSaveUser);
		//业务表系统字段名
		String sAddDateCol = "", sAddUserCol = "", sModDateCol = "", sModUserCol = "";
		if (isUserInfo) {
			sAddUserCol = SystemVar.getValue("sys.field.add_ufield", "add_userid");
			sAddDateCol = SystemVar.getValue("sys.field.add_dfield", "add_date");
			sModUserCol = SystemVar.getValue("sys.field.mod_ufield", "modify_userid");
			sModDateCol = SystemVar.getValue("sys.field.mod_dfield", "modify_date");
		}
		//添加保存用户信息的字段
		if (isUserInfo) {
			//给新增语句添加新增时间、新增人字段
			lsColi.add(sTableName + "." + sAddDateCol);
			lsColi.add(sTableName + "." + sAddUserCol);
			lsParamTypei.add("date");
			lsParamTypei.add("string");
			
			//给保存语句添加修改时间、修改人字段			
			lsColu.add(sTableName + "." + sModDateCol);
			lsColu.add(sTableName + "." + sModUserCol);
			lsParamTypeu.add("date");
			lsParamTypeu.add("string");
		}
		
		//给查询语句赋值
		if (!lsCols.isEmpty()) {
			String[] col = ArrayUtil.listToArray(lsCols);
			String[] type = ArrayUtil.listToArray(lsParamTypes);
			StringBuilder sql = new StringBuilder("select ");
				sql.append(ArrayUtil.arrayToString(col) + " ");
				sql.append(MapUtil.getValue(mpFun, "from_sql"));

			funRet.setSelectCol(col);
			funRet.setSelectParamType(type);
			funRet.setSelectSQL(sql.toString());
		}

		//给新增语句赋值
		if (!lsColi.isEmpty()) {
			String[] col = ArrayUtil.listToArray(lsColi);
			String[] type = ArrayUtil.listToArray(lsParamTypei);
			StringBuilder sql = new StringBuilder("insert into ");
				sql.append(sTableName);
				sql.append("(" + ArrayUtil.arrayToString(col) + ") values (");
				sql.append(StringUtil.getFillString("?,", col.length));
				sql.append(")");
						
			funRet.setInsertCol(col);
			funRet.setInsertParamType(type);
			funRet.setInsertSQL(sql.toString());
		}
		
		//当前功能的主键
		String pkcol = MapUtil.getValue(mpFun, "pk_col");
		String where = " where " + pkcol + " = ? ";
		
		//给保存语句赋值
		if (!lsColu.isEmpty()) {
			String[] col = ArrayUtil.listToArray(lsColu);
			String[] type = ArrayUtil.listToArray(lsParamTypeu);
			StringBuilder sql = new StringBuilder("update ");
				sql.append(sTableName);
				sql.append(" set ");
				sql.append(ArrayUtil.arrayToString(col, "=?,"));
				sql.append(where);
			
			//字段列与参数中要加where中的字段
			funRet.setUpdateCol(ArrayUtil.arrayAddString(col, pkcol));
			funRet.setUpdateParamType(ArrayUtil.arrayAddString(type, "string"));
			funRet.setUpdateSQL(sql.toString());
		}
		
		//给删除语句赋值
		StringBuilder sqldel = new StringBuilder();
			sqldel.append("delete from ");
			sqldel.append(sTableName);
			sqldel.append(where);
		funRet.setDeleteSQL(sqldel.toString());
		
		//给签字语句赋值
		StringBuilder sqlaudit = new StringBuilder();
			sqlaudit.append("update ");
			sqlaudit.append(sTableName);
			sqlaudit.append(" set ");
			sqlaudit.append(MapUtil.getValue(mpFun, "audit_col") + "=? ");
			//添加修改时间与修改人字段
			if (isUserInfo) {
				sqlaudit.append("," + sModDateCol + "=? ");
				sqlaudit.append("," + sModUserCol + "=? ");
			}
			sqlaudit.append(where);
		funRet.setAuditSQL(sqlaudit.toString());
		
		return funRet;
	}
}
