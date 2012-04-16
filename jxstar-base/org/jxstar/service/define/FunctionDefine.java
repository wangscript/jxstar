/*
 * FunctionDefine.java 2008-4-1
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.service.define;

import java.util.Map;

import org.jxstar.util.MapUtil;

/**
 * 功能对象，包涵操作一个功能的各种属性，不需要重新构建各种语句，用于提高系统速度。
 * 
 * @author TonyTan
 * @version 1.0, 2008-4-1
 */
public class FunctionDefine {
	//功能对象标示，是功能对象的唯一键
	private String _funid = null;
	//功能基本信息表
	private Map<String,String> _baseInfo = null;
	//功能查询语句
	private String _selectSQL = null;
	//功能统计语句
	private String _sumSQL = null;
	//功能查询过滤子句
	private String _whereSQL = null;
	//功能查询字段名列表
	private String[] _selectCol = null;
	//功能查询字段参数列表
	private String[] _selectParamType = null;
	
	//功能新增语句
	private String _insertSQL = null;
	//功能新增字段列表
	private String[] _insertCol = null;
	//功能新增参数数据类型
	private String[] _insertParamType = null;
	
	//功能更新语句
	private String _updateSQL = null;
	//功能更新字段列表
	private String[] _updateCol = null;		
	//功能更新参数数据类型
	private String[] _updateParamType = null;
	
	//删除语句
	private String _deleteSQL = null;
	//提交语句
	private String _auditSQL = null;
	
	public void setFunID(String funid) {
		_funid = funid;
	}
	public String getFunID() {
		return _funid;
	}
	
	/**
	 * 取该功能的基本信息，基本信息参数不带表名。
	 * 
	 * @return Map
	 */
	public Map<String,String> getBaseInfo() {
		return _baseInfo;
	}

	/**
	 * 设置该功能定义的基本信息，基本信息参数不带表名。
	 * 
	 * @param info
	 */
	public void setBaseInfo(Map<String,String> info) {
		_baseInfo = info;
	}	
	
	/**
	 * 取功能定义的基本信息中的一个属性值。
	 * 
	 * @param sElementName - 参数名
	 * @return String
	 */
	public String getElement(String sElementName) {
		return MapUtil.getValue(_baseInfo, sElementName);
	}
	
	/**
	 * 设置该功能的查询语句，样式：select tbl.col1, tbl.col2... from tbl。
	 * 
	 * @param selectSQL
	 */
	public void setSelectSQL(String selectSQL) {
		_selectSQL = selectSQL;
	}
	
	/**
	 * 取该功能的查询语句，样式：select tbl.col1, tbl.col2... from tbl。
	 * 
	 * @return
	 */
	public String getSelectSQL() {
		return _selectSQL;
	}
	
	/**
	 * 设置该功能的where语句，样式：(tbl.col1 = val and ...) 。
	 * 
	 * @param whereSQL
	 */
	public void setWhereSQL(String whereSQL) {
		_whereSQL = whereSQL;
	}	
	
	/**
	 * 取该功能的where语句，样式：(tbl.col1 = val and ...) 。
	 * 
	 * @return String
	 */
	public String getWhereSQL() {
		return _whereSQL;
	}

	/**
	 * 取该功能的查询字段名，样式：tbl.col1,tbl.col2。
	 * 
	 * @return String[]
	 */
	public String[] getSelectCol() {
		return _selectCol;
	}

	/**
	 * 设置该功能的查询字段名，样式：tbl.col1,tbl.col2。
	 * 
	 * @param col
	 */
	public void setSelectCol(String[] col) {
		_selectCol = col;
	}

	/**
	 * 取该功能的查询参数类型，样式：string;string;date... 。
	 * 
	 * @return String][
	 */
	public String[] getSelectParamType() {
		return _selectParamType;
	}

	/**
	 * 设置该功能的查询参数类型，样式：string;string;date... 。
	 * 
	 * @param paramType
	 */
	public void setSelectParamType(String[] paramType) {
		_selectParamType = paramType;
	}
	
	/**
	 * 取该功能的新增语句，
	 * 样式：insert into tbl(col1, col2...) values(?, ?, ...)
	 * 
	 * @return String
	 */
	public String getInsertSQL() {
		return _insertSQL;
	}

	/**
	 * 设置该功能的新增语句，
	 * 样式：insert into tbl(col1, col2...) values(?, ?, ...)
	 * 
	 * @param insertSQL
	 */
	public void setInsertSQL(String insertSQL) {
		_insertSQL = insertSQL;
	}
	
	/**
	 * 取该功能的新增字段列表, 用于从map中取值, 
	 * 样式：tbl.col1,tbl.col2...
	 * 
	 * @return String[]
	 */
	public String[] getInsertCol() {
		return _insertCol;
	}
	
	/**
	 * 设置该功能的新增字段列, 用于从map中取值, 
	 * 样式：tbl.col1,tbl.col2...
	 * 
	 * @param insertCol
	 */
	public void setInsertCol(String[] insertCol) {
		_insertCol = insertCol;
	}
	
	/**
	 * 取该功能的新增参数类型，样式：string;string;date... 。
	 * 
	 * @return String[]
	 */
	public String[] getInsertParamType() {
		return _insertParamType;
	}
	/**
	 * 设置该功能的新增参数类型，样式：string;string;date... 。
	 * 
	 * @param paramType
	 */
	public void setInsertParamType(String[] paramType) {
		_insertParamType = paramType;
	}
	
	/**
	 * 取该功能的更新语句，
	 * 样式：update tbl set col1 = ?, col2 = ?, ... where keycol = ? 。
	 * 
	 * @return String
	 */
	public String getUpdateSQL() {
		return _updateSQL;
	}

	/**
	 * 设置该功能的更新语句，
	 * 样式：update tbl set col1 = ?, col2 = ?, ... where keycol = ? 。
	 * 
	 * @param updateSQL
	 */
	public void setUpdateSQL(String updateSQL) {
		_updateSQL = updateSQL;
	}
	
	/**
	 * 取该功能的更新字段列表, 用于从map中取值, 
	 * 样式：tbl.col1,tbl.col2...
	 * 
	 * @return String[]
	 */
	public String[] getUpdateCol() {
		return _updateCol;
	}

	/**
	 * 设置该功能的更新字段列, 用于从map中取值, 
	 * 样式：tbl.col1,tbl.col2...
	 * 
	 * @param updateCol
	 */
	public void setUpdateCol(String[] updateCol) {
		_updateCol = updateCol;
	}	
	
	/**
	 * 取该功能的更新参数类型，样式：string;string;date... 。
	 * 
	 * @return String[]
	 */
	public String[] getUpdateParamType() {
		return _updateParamType;
	}

	/**
	 * 设置该功能的更新参数类型，样式：string;string;date... 。
	 * 
	 * @param paramType
	 */
	public void setUpdateParamType(String[] paramType) {
		_updateParamType = paramType;
	}
	
	/**
	 * 设置该功能的删除SQL, 样式: delete from tbl where pkcol like ?
	 * 
	 * @param deleteSQL
	 */
	public void setDeleteSQL(String deleteSQL) {
		_deleteSQL = deleteSQL;
	}
	
	/**
	 * 取该功能的删除SQL, 样式: delete from tbl where pkcol like ?
	 * 
	 * @return String
	 */
	public String getDeleteSQL() {
		return _deleteSQL;
	}	
	
	/**
	 * 设置该功能的提交SQL, 样式: update tbl set audit_col = '1' where pkcol like ?
	 * 
	 * @param auditSQL
	 */
	public void setAuditSQL(String auditSQL) {
		_auditSQL = auditSQL;
	}
	
	/**
	 * 取该功能的提交SQL, 样式: update tbl set audit_col = '1' where pkcol like ?
	 * 
	 * @return String
	 */
	public String getAuditSQL() {
		return _auditSQL;
	}	
	
	/**
	 * 该对象描述
	 */
	public String toString() {
		String sret = "function id is null!";
		if (_funid != null) {
			sret = "function id is " + _funid + " ."; 
		}
		return sret;
	}
	
	/**
	 * 取功能统计语句
	 * @return
	 */
	public String getSumSQL() {
		return _sumSQL;
	}
	
	/**
	 * 设置功能统计语句
	 * @param sumsql
	 */
	public void setSumSQL(String sumsql) {
		_sumSQL = sumsql;
	}
}
