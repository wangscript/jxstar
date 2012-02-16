/*
 * TreeTest.java 2009-11-20
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.web.element;


import org.jxstar.service.query.TreeQuery;
import org.jxstar.test.AbstractTest;

/**
 * 
 *
 * @author TonyTan
 * @version 1.0, 2009-11-20
 */
public class TreeTest extends AbstractTest {
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//ComboData comboData = new ComboData();
		TreeQuery tool = new TreeQuery();
		tool.createJson("adminstrator", "sys_module", "10");
		System.out.println(tool.getReturnData());
//		
//		String maxVal = "0000"+1;
//		String treeID = maxVal.substring(maxVal.length()-4, maxVal.length());
//		System.out.println(treeID);
/*		
		String sql = "select * from (select fun_tree.table_name,fun_tree.node_name,fun_tree.node_level," +
				"fun_tree.node_other,fun_tree.self_where,fun_tree.self_order,fun_tree.tree_title," +
				"fun_tree.self_funid,fun_tree.has_level,fun_tree.right_funid,fun_tree.right_where," +
				"fun_tree.right_layout,fun_tree.right_target,fun_tree.db_name,fun_tree.show_data," +
				"fun_tree.fun_id,fun_tree.is_tree,fun_tree.node_id,fun_tree.team_id,fun_tree.tree_id," +
				"fun_tree.relat_col,fun_tree.team_name,fun_tree.is_defteam,fun_tree.tree_level," +
				"fun_tree.child_field,fun_tree.prop_prefix,fun_tree.relat_select from fun_tree " +
				"where  (fun_tree.fun_id = ?) ) t1 limit 0, 50";
		String[] astrCol = ArrayUtil.getGridCol(sql);
		System.out.println("gridjson allsql:" + ArrayUtil.arrayToString(astrCol));*/
	}
	
}
