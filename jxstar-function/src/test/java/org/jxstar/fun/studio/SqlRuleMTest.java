/*
 * SqlRuleMTest.java 2011-2-22
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


import org.jxstar.dao.pool.PooledConnection;
import org.jxstar.fun.studio.RuleSqlBO;
import org.jxstar.test.AbstractTest;

/**
 * 测试生成SQL规则的参数明细。
 *
 * @author TonyTan
 * @version 1.0, 2011-2-22
 */
public class SqlRuleMTest extends AbstractTest {

	public static void main(String[] args) {
		RuleSqlBO bo = new RuleSqlBO();
		
		String ruleId = "jxstar22";
		//String srcSql = "select col_code, col_name, data_type, format_id, col_control, col_index from v_field_info where col_code = ?";
		//String srcSql = "select sys_user.user_id, sys_user.user_name, {JOINSTR}({JOINSTR}('[dept_id] like ''', sys_user.dept_id), '%''') as condition, wf_nodeattr.process_id, wf_nodeattr.node_id from sys_user, wf_nodeattr where sys_user.user_id = ? and wf_nodeattr.nodeattr_id = {FKEYID}";
		String userId = "administrator";
		
		bo.createSqlParam(ruleId, userId);
	}
	
	public void mytest() {
		SqlRuleMTest test = new SqlRuleMTest();
		String sql = "select portlet_title, type_id, type_code, type_name, object_id, object_name from v_plet_type where type_id = ''";

		test.createParam(sql);
	}
	
	public void createParam(String sql) {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = PooledConnection.getInstance().getConnection();
			
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			getParam(rs);
			
		} catch(SQLException e) {
			e.printStackTrace();
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
	}
	
	private void getParam(ResultSet rs) {
		StringBuilder sbName = new StringBuilder();
		StringBuilder sbType = new StringBuilder();
		StringBuilder sbValue = new StringBuilder();
		try {
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnNum = rsmd.getColumnCount();
			
			boolean has = rs.next();
			for (int i = 1; i <= columnNum; i++) {
				String name = rsmd.getColumnName(i).toLowerCase();
				sbName.append(name).append(";");
				if (has) sbValue.append(rs.getString(name)).append(";");
				
				//如果是日期类型的字段，转换为日期对象
				if (rsmd.getColumnType(i) == java.sql.Types.DATE || 
						rsmd.getColumnType(i) == java.sql.Types.TIMESTAMP) {
					sbType.append("date;");
				} else if (rsmd.getColumnType(i) == java.sql.Types.CHAR || 
						rsmd.getColumnType(i) == java.sql.Types.VARCHAR) {
					sbType.append("string;");
				} else if (rsmd.getColumnType(i) == java.sql.Types.INTEGER) {
					sbType.append("int;");
				} else if (rsmd.getColumnType(i) == java.sql.Types.DECIMAL || 
						rsmd.getColumnType(i) == java.sql.Types.DOUBLE || 
						rsmd.getColumnType(i) == java.sql.Types.FLOAT || 
						rsmd.getColumnType(i) == java.sql.Types.NUMERIC) {
					sbType.append("double;");
				} else {
					sbType.append("string;");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		System.out.println("============sbName=" + sbName.toString());
		System.out.println("============sbType=" + sbType.toString());
		System.out.println("============sbValue=" + sbValue.toString());
	}
}
