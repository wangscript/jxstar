/*
 * ComboDefine.java 2010-1-3
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.fun.studio;

import java.util.Map;

import org.jxstar.dao.DaoParam;
import org.jxstar.fun.design.parser.PageParserUtil;
import org.jxstar.service.BusinessObject;
import org.jxstar.service.define.DefineName;
import org.jxstar.service.define.FunDefineDao;
import org.jxstar.util.MapUtil;

/**
 * 构建选择数据窗口配置信息，生成页面JS文件时需用到。
 *
 * @author TonyTan
 * @version 1.0, 2010-1-3
 */
public class ComboDefine extends BusinessObject {
	private static final long serialVersionUID = 1L;

	/**
	 * 根据功能ID与字段代码找到字段扩展定义信息。
	 * @param funid
	 * @param fieldCode
	 * @return
	 */
	public String configJson(String funid, String fieldCode) {
		//查找字段定义信息
		String sqlCol = "select col_id, control_name from fun_col where " +
				"(col_control = 'combowin' or col_control = 'selectwin') and fun_id = ? and col_code = ?";
		DaoParam paramCol = _dao.createParam(sqlCol);
		paramCol.setDsName(DefineName.DESIGN_NAME);
		paramCol.addStringValue(funid).addStringValue(fieldCode);
		Map<String,String> mpCol = _dao.queryMap(paramCol);
		if (mpCol.isEmpty()) {
			_log.showWarn("not find combowin type field: funid={0} fieldcode={1}!", 
					funid, fieldCode);
			return "null";
		}
		String colId = mpCol.get("col_id");
		String ctlCode = mpCol.get("control_name");
		
		//查找选择控件的功能ID
		Map<String,String> mpCtl = PageParserUtil.selectWinCtl(ctlCode);
		if (mpCtl.isEmpty()) {
			_log.showWarn("not find combowin control: control_code={0}!", ctlCode);
			return "null";
		}
		String selFunId = mpCtl.get("fun_id");
		String layoutPage = mpCtl.get("layout_page");
		
		//查找字段定义扩展信息
		Map<String,String> mpExt = FunDefineDao.queryColExt(colId);
		
		if (mpExt.isEmpty()) {
			mpExt.put("is_same", "1");
			mpExt.put("is_showdata", "1");
			mpExt.put("is_readonly", "1");
			mpExt.put("is_moreselect", "0");
		}
		
		//构建JSON对象
		StringBuilder sbJson = new StringBuilder();
		sbJson.append("{");
		sbJson.append("pageType:'combogrid', ");
		sbJson.append("nodeId:'"+ selFunId +"', ");
		sbJson.append("layoutPage:'"+ layoutPage +"', ");
		sbJson.append("sourceField:'"+ MapUtil.getValue(mpExt, "source_cols") +"', ");
		sbJson.append("targetField:'"+ MapUtil.getValue(mpExt, "target_cols") +"', ");
		sbJson.append("whereSql:\""+ MapUtil.getValue(mpExt, "where_sql") +"\", ");
		sbJson.append("whereValue:'"+ MapUtil.getValue(mpExt, "where_value") +"', ");
		sbJson.append("whereType:'"+ MapUtil.getValue(mpExt, "where_type") +"', ");
		sbJson.append("isSame:'"+ MapUtil.getValue(mpExt, "is_same") +"', ");
		sbJson.append("isShowData:'"+ MapUtil.getValue(mpExt, "is_showdata") +"', ");
		sbJson.append("isMoreSelect:'"+ MapUtil.getValue(mpExt, "is_moreselect") +"',");
		sbJson.append("isReadonly:'"+ MapUtil.getValue(mpExt, "is_readonly") +"',");
		sbJson.append("fieldName:'"+ fieldCode +"'");
		sbJson.append("}");
		
		return sbJson.toString();
	}
	
	/**
	 * 选择字段是否为只能选择不能修改
	 * @param colId -- 字段ID
	 * @return
	 */
	public boolean isReadOnly(String colId) {
		Map<String,String> mpExt = FunDefineDao.queryColExt(colId);
		if (mpExt.isEmpty()) return true;
		
		String ret = MapUtil.getValue(mpExt, "is_readonly", "1");
		return ret.equals("1");
	}
}
