/*
 * ReadDesignBO.java 2010-10-14
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.fun.design;

import java.util.List;
import java.util.Map;


import org.jxstar.dao.util.BigFieldUtil;
import org.jxstar.fun.design.parser.GridPageParser;
import org.jxstar.fun.design.parser.PageParser;
import org.jxstar.service.BusinessObject;
import org.jxstar.service.define.DefineName;
import org.jxstar.service.define.FunDefineDao;

/**
 * 读取表单设计信息，如果没有设计信息，则创建缺省表单设计信息。
 *
 * @author TonyTan
 * @version 1.0, 2010-10-14
 */
public class ReadDesignBO extends BusinessObject {
	private static final long serialVersionUID = 1L;
	
	//缺省FORM显示几列
	private static int COLUMN_NUM = 2;

	/**
	 * 读取表单设计信息，如果没有设计信息，则创建缺省表单设计信息。
	 * @param funcId -- 功能ID
	 * @param colnums -- 表单缺省为几列
	 * @return
	 */
	public String readFrom(String funcId, String colnums) {
		int cols = 0;
		if (colnums == null || colnums.length() == 0) {
			cols = COLUMN_NUM;
		} else {
			cols = Integer.parseInt(colnums);
		}
		
		//先从数据中读取
		String designPage = readDesignPage(funcId, "form");
		
		//如果没有则创建缺省设计信息
		if (designPage.length() == 0) {
			designPage = createDesign(funcId, cols);
		}
		//_log.showDebug(designPage);
		
		//返回给前台
		setReturnData(designPage);
		
		return _returnSuccess;
	}
	
	/**
	 * 表格的缺省设计信息就是解析后的页面
	 * @param funcId -- 功能ID
	 * @param realPath -- 物理路径
	 * @return
	 */
	public String readGrid(String funcId, String realPath) {
		PageParser parser = new GridPageParser();
		//设置系统路径
		parser.setRealPath(realPath);
		//解析表格页面
		String ret = parser.parsePage(funcId, "grid");
		if (ret.equals(_returnFaild)) {
			setMessage(parser.getMessage());
			return _returnFaild;
		}
		//返回给前台
		setReturnData(parser.getReturnData());
		
		return _returnSuccess;
	}
	
	/**
	 * 创建功能的缺省FORM设计文件：
	 * 缺省设计文件采用三列式；第一层为一个formitem布局，第二层为三个columnitem布局
	 * 第三层为fielditem字段；
	 * 
	 * @param funcId -- 功能ID
	 * @param colnums -- 表单缺省为几列
	 * @return
	 */
	private String createDesign(String funcId, int colnums) {
		StringBuilder sbDesign = new StringBuilder();
		
		sbDesign.append("<?xml version='1.0' encoding='utf-8'?>");
		sbDesign.append("<page state='default'>");
		sbDesign.append(createFormItem(funcId, colnums));
		sbDesign.append("</page>");

		return sbDesign.toString();
	}
	
	/**
	 * 创建formitem布局元素
	 * 
	 * @param funcId -- 功能ID
	 * @param colnums -- 表单缺省为几列
	 * @return
	 */
	private String createFormItem(String funcId, int colnums) {
		StringBuilder sbform = new StringBuilder();
		
		sbform.append("<formitem>");
		
		//创建columnitem布局元素
		sbform.append(createColumnItem(funcId, colnums));
		
		sbform.append("</formitem>");
		
		return sbform.toString();
	}
	
	/**
	 * 创建列布局元素，缺省创建colsnum列
	 * @param funcId -- 功能ID
	 * @param colsnum -- 创建n列
	 * @return
	 */
	private String createColumnItem(String funcId, int colsnum) {
		//取字段定义列表信息
		List<Map<String,String>> lsColumn = FunDefineDao.queryCol(funcId);
		if (lsColumn.isEmpty()) return "";
		
		//创建保存列布局的字符串
		StringBuilder[] sbcolumn = new StringBuilder[colsnum];
		for (int i = 0; i < colsnum; i++) {
			sbcolumn[i] = new StringBuilder();
		}
		
		//循环把所有字段放到各列中
		for (int i = 0, n = lsColumn.size(); i < n; i++) {
			Map<String,String> mpField = lsColumn.get(i);
			
			sbcolumn[i%colsnum].append(createFieldItem(mpField));
		}
		
		//组合各列设计信息
		StringBuilder allColumn = new StringBuilder();
		for (int i = 0; i < colsnum; i++) {
			String cols = columnItem(sbcolumn[i].toString());
			allColumn.append(cols);
		}
		
		return allColumn.toString();
	}
	
	/**
	 * 创建列布局元素
	 * @param fieldCont -- 字段设计信息
	 * @return
	 */
	private String columnItem(String fieldCont) {
		StringBuilder sbcolumn = new StringBuilder();
		
		sbcolumn.append("<columnitem>");

		sbcolumn.append(fieldCont);

		sbcolumn.append("</columnitem>");
		
		return sbcolumn.toString();
	}
	
	/**
	 * 创建字段元素
	 * @param mpField -- 字段信息
	 * @return
	 */
	private String createFieldItem(Map<String, String> mpField) {
		StringBuilder sbfield = new StringBuilder();
		
		String col_code = mpField.get("col_code");
		String col_name = mpField.get("col_name");
		String col_control = mpField.get("col_control");
		String col_index = mpField.get("col_index");
		//String control_name = mpField.get("control_name");
		//序号大于等于10000的控件缺省不显示
		String visible = Integer.parseInt(col_index)<10000 ? "true":"false";
		
		sbfield.append("<fielditem title='"+col_name+"' colcode='"+col_code+"' ");
		sbfield.append("visible='"+ visible +"' xtype='"+ col_control +"'/>");
		
		return sbfield.toString();
	}
	
	/**
	 * 取功能的页面设计信息
	 * @param funcId
	 * @param pageType
	 * @return
	 */
	private String readDesignPage(String funcId, String pageType){
		String sql = "select page_content from fun_design " +
				"where fun_id = '"+ funcId +"' and page_type = '"+ pageType +"' ";
		
		return BigFieldUtil.readStream(sql, "page_content", DefineName.DESIGN_NAME);
	}
}
