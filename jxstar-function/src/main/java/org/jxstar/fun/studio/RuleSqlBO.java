/*
 * SqlRuleMBO.java 2011-2-22
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.fun.studio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.jxstar.dao.DmDao;
import org.jxstar.dao.pool.PooledConnection;
import org.jxstar.dao.util.SQLParseException;
import org.jxstar.dao.util.SqlParser;
import org.jxstar.service.BusinessObject;
import org.jxstar.util.DateUtil;
import org.jxstar.util.factory.FactoryUtil;
import org.jxstar.util.factory.SystemFactory;
import org.jxstar.util.resource.JsMessage;

/**
 * 处理SQL规则定义信息。
 *
 * @author TonyTan
 * @version 1.0, 2011-2-22
 */
public class RuleSqlBO extends BusinessObject {
	private static final long serialVersionUID = 446723545778510597L;

	/**
	 * 取来源SQL中的字段明细：
	 * 先删除参数明细记录；
	 * 解析来源SQL取空结果集，从结果集的元数据中取字段名与字段类型；
	 * @param ruleId -- SQL规则ID，参数明细的外键
	 * @param userId -- 当前用户ID
	 * @return
	 */
	public String createSqlParam(String ruleId, String userId) {
		if (ruleId == null || ruleId.length() == 0) {
			//"SQL规则定义时生成参数明细的参数不正确！"
			setMessage(JsMessage.getValue("rulesqlbo.deterror"));
			return _returnFaild;
		}
		//从数据库中来源SQL语句
		Map<String,String> mpRule = DmDao.queryMap("fun_rule_sql", ruleId);
		String srcSql = mpRule.get("src_sql");
		
		//解析来源SQL，用于查询空结果集
		String sql = parseSql(srcSql);
		if (sql == null || sql.length() == 0) {
			//"SQL规则定义中的来源SQL定义不正确，解析SQL为空串！"
			setMessage(JsMessage.getValue("rulesqlbo.sqlerror"));
			return _returnFaild;
		}
		
		List<String[]> lsParam = getParamList(sql);
		if (lsParam.isEmpty()) {//"SQL规则定义中的来源SQL定义不正确，解析字段信息为空！"
			setMessage(JsMessage.getValue("rulesqlbo.fielderror"));
			return _returnFaild;
		}
		
		//先删除参数明细
		if (!DmDao.deleteByWhere("fun_rule_param", "rule_id = '"+ ruleId +"'")) {
			//"SQL规则定义中的参数明细自动新增前的删除失败！"
			setMessage(JsMessage.getValue("rulesqlbo.delerror"));
			return _returnFaild;
		}
		
		//添加字段信息到参数明细表
		if (!insertParam(ruleId, userId, lsParam)) {
			//"SQL规则定义中的参数明细自动新增失败！"
			setMessage(JsMessage.getValue("rulesqlbo.newerror"));
			return _returnFaild;
		}
		
		return _returnSuccess;
	}
	
	/**
	 * 新增参数明细记录
	 * @param ruleId -- 规则ID
	 * @param userId -- 用户ID
	 * @param lsParam -- 字段信息
	 * @return
	 */
	private boolean insertParam(String ruleId, String userId, List<String[]> lsParam) {
		for (int i = 0, n = lsParam.size(); i < n; i++) {
			//字段名与类型
			String[] fields = lsParam.get(i);
			//字段序号
			String index = Integer.toString((i+1) * 10);

			Map<String,String> mpParam = FactoryUtil.newMap();
			mpParam.put("rule_id", ruleId);
			mpParam.put("param_name", fields[0]);
			mpParam.put("param_type", fields[1]);
			mpParam.put("param_no", index);
			mpParam.put("param_src", "db");
			mpParam.put("add_userid", userId);
			mpParam.put("add_date", DateUtil.getTodaySec());
			
			String paramId = DmDao.insert("fun_rule_param", mpParam);
			if (paramId.length() == 0) return false;
		}
		
		return true;
	}
	
	/**
	 * 解析结果集中的字段名与数据类型
	 * @param sql -- SQL语句
	 * @return
	 */
	private List<String[]> getParamList(String sql) {
		List<String[]> lsParam = FactoryUtil.newList();
		
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = PooledConnection.getInstance().getConnection();
			if (con == null) return lsParam;
			
			ps = con.prepareStatement(sql);
			//添加临时主键值参数
			ps.setString(1, "tmpid");
			rs = ps.executeQuery();
			//解析结果集字段信息
			lsParam = getParamList(rs);
		} catch(SQLException e) {
			e.printStackTrace();
			return lsParam;
		} finally {
			try {
				if (rs != null) rs.close();
				rs = null;
				if (ps != null) ps.close();
				ps = null;
				if (con != null) con.close();
				con = null;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return lsParam;
	}
	
	/**
	 * 解析结果集中的字段名与数据类型
	 * @param rs -- 结果集
	 * @return
	 */
	private List<String[]> getParamList(ResultSet rs) {
		List<String[]> lsDet = FactoryUtil.newList();
		try {
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnNum = rsmd.getColumnCount();
			
			for (int i = 1; i <= columnNum; i++) {
				//取字段名
				String name = rsmd.getColumnName(i).toLowerCase();
				//取字段类型
				String type = "string";
				if (rsmd.getColumnType(i) == java.sql.Types.DATE || 
						rsmd.getColumnType(i) == java.sql.Types.TIMESTAMP) {
					type = "date";
				} else if (rsmd.getColumnType(i) == java.sql.Types.INTEGER) {
					type = "int";
				} else if (rsmd.getColumnType(i) == java.sql.Types.DECIMAL || 
						rsmd.getColumnType(i) == java.sql.Types.DOUBLE || 
						rsmd.getColumnType(i) == java.sql.Types.FLOAT || 
						rsmd.getColumnType(i) == java.sql.Types.NUMERIC) {
					type = "double";
				}
				//保存字段名与数据类型
				lsDet.add(new String[]{name, type});
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return FactoryUtil.newList();
		}
		
		return lsDet;
	}
	
	/**
	 * 解析SQL中的自定义函数与外键值
	 * @param sql -- 来源SQL
	 * @return
	 */
	private String parseSql(String sql) {
		//解析SQL中的外键值，用临时值替代
		sql = sql.replaceFirst("\\{FKEYID\\}", "'tfid'");
		
		SqlParser parser = (SqlParser) SystemFactory.createSystemObject("SqlParser");
		//解析SQL中的自定义函数
		try {
			sql = parser.parse(sql);
		} catch (SQLParseException e) {
			e.printStackTrace();
			return "";
		}
		
		return sql;
	}
}
