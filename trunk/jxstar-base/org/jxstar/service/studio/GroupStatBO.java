/*
 * GroupStatBO.java 2010-12-13
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.service.studio;

import java.util.Map;


import org.jxstar.control.action.RequestContext;
import org.jxstar.dao.DaoParam;
import org.jxstar.dao.JsonDao;
import org.jxstar.service.BoException;
import org.jxstar.service.BusinessObject;
import org.jxstar.service.define.FunDefineDao;
import org.jxstar.service.util.WhereUtil;
import org.jxstar.util.StringUtil;
import org.jxstar.util.resource.JsMessage;

/**
 * 通用分组统计：
 * 根据高级查询界面中的查询条件、结合当前功能的查询作为统计数据范围；
 * 根据分组字段、统计字段统计，返回统计结果到前台。
 *
 * @author TonyTan
 * @version 1.0, 2010-12-13
 */
public class GroupStatBO extends BusinessObject {
	private static final long serialVersionUID = 1L;
	//记录数统计字段
	private static final String RECORDNUM = "recordnum";
	
	/**
	 * 统计结果
	 * @param request
	 * @return
	 */
	public String groupStat(RequestContext request) {
		DaoParam param = null;
		try {
			param = statDaoParam(request);
		} catch (BoException e) {
			_log.showError(e);
			setMessage(e.getMessage());
			return _returnFaild;
		}
		
		//取分组字段与统计字段
		String charField = request.getRequestValue("charfield");
		String numField = request.getRequestValue("numfield");
		
		//取分组统计数据
		JsonDao jsonDao = JsonDao.getInstance();
		String[] cols = getFieldCodes(charField, numField);
		String strJson = jsonDao.query(param, cols);
		
		StringBuilder sbJson = new StringBuilder("{total:20,root:[");
		sbJson.append(strJson + "]}");
		//查询SQL异常
		if (strJson == null) {
			setMessage(JsMessage.getValue("web.query.error"));
			return _returnFaild;
		}
		//_log.showDebug("json=" + sbJson.toString());
		
		//返回查询数据
		setReturnData(sbJson.toString());
		
		return _returnSuccess;
	}
	
	/**
	 * 构建统计数据的查询对象，中输出统计数据类中需要使用。
	 * @param request
	 * @return
	 * @throws BoException
	 */
	private DaoParam statDaoParam(RequestContext request) throws BoException {
		String funid = request.getRequestValue("query_funid");
		String userid = request.getRequestValue("user_id");
		String wheresql = request.getRequestValue("where_sql");
		String wheretype = request.getRequestValue("where_type");
		String wherevalue = request.getRequestValue("where_value");
		//查询方式：0 -- 普通查询 1 -- 高级查询
		String querytype = request.getRequestValue("query_type");
		
		//取功能定义查询where
		String where = "";
		try {
			where = WhereUtil.queryWhere(funid, userid, wheresql, querytype);
		} catch (BoException e) {
			setMessage(e.getMessage());
			_log.showError(e);
		}
		
		//分组字段
		String charField = request.getRequestValue("charfield");
		if (charField == null || charField.length() == 0) {
			throw new BoException(JsMessage.getValue("groupstatbo.charfield.null"));
		}
		
		//统计字段
		String numField = request.getRequestValue("numfield");
		if (numField == null) numField = "";
		//取功能定义对象
		Map<String, String> mpFun = FunDefineDao.queryFun(funid);
		//取FROM子句
		String fromsql = mpFun.get("from_sql");
		if (fromsql == null || fromsql.length() == 0) {
			throw new BoException(JsMessage.getValue("groupstatbo.fromsql.null"));
		}
		
		//构建完整的统计语句
		StringBuilder sbsel = new StringBuilder();
		sbsel.append("select " + charField + ", " + addSumField(numField));
		sbsel.append(" " + fromsql);
		if (where != null && where.length() > 0) {
			sbsel.append(" where " + where);
		}
		sbsel.append(" group by " + charField);
		
		_log.showDebug("groupsql select:" + sbsel.toString());
		_log.showDebug("groupsql wheretype:" + wheretype);
		_log.showDebug("groupsql wherevalue:" + wherevalue);
		
		//数据源名称
		String dsname = mpFun.get("ds_name");
		//构建查询参数对象
		DaoParam param = new DaoParam();
		param.setUseParse(true);//处理统计字段添加空值处理的函数
		param.setSql(sbsel.toString());
		param.setValue(wherevalue).setType(wheretype).setDsName(dsname);
		
		return param;
	}
	
	/**
	 * 取字段名：把两个字段串合并为一个字段数组，最后添加记录数字段
	 * @param charField -- 分组字段串
	 * @param numField -- 统计字段串
	 * @return
	 */
	private String[] getFieldCodes(String charField, String numField) {
		String fields = delTable(charField);
		if (numField == null || numField.length() == 0) {
			fields += RECORDNUM;
		} else {
			fields += delTable(numField) + RECORDNUM;
		}
		
		return fields.split(",");
	}
	
	/**
	 * 取字段标题：把两个字段串合并为一个字段数组，最后添加记录数字段
	 * @param charField -- 分组字段标题串
	 * @param numField -- 统计字段标题串
	 * @return
	 */
	/*
	public String[] getFieldTitles(String charField, String numField) {
		String fields = charField + ",";
		if (numField == null || numField.length() == 0) {
			fields += "记录数";
		} else {
			fields += numField + ",记录数";
		}
		
		return fields.split(",");
	}*/
	
	/**
	 * 给统计字段添加求和关键字
	 * @param numField -- 统计字段串
	 * @return
	 */
	private String addSumField(String numField) {
		if (numField == null || numField.length() == 0) {
			return "count(*) as "+ RECORDNUM;
		}
		
		String[] fields = numField.split(",");
		
		StringBuilder sbfield = new StringBuilder();
		for (int i = 0, n = fields.length; i < n; i++) {
			//字段名中添加了表名，防止多表列同名
			String colname = StringUtil.getNoTableCol(fields[i]);
			sbfield.append("sum({ISNULL}(" + fields[i] + ", 0)) as "+ colname +",");
		}
		sbfield.append("count(*) as "+ RECORDNUM);
		
		return sbfield.toString();
	}
	
	//去掉字段中的表名
	private String delTable(String fields) {
		if (fields == null || fields.length() == 0) return "";
		
		String[] fs = fields.split(",");
		StringBuilder sbfield = new StringBuilder();
		for (String field : fs) {
			String colname = StringUtil.getNoTableCol(field);
			sbfield.append(colname).append(",");
		}
		
		return sbfield.toString();
	}
}
