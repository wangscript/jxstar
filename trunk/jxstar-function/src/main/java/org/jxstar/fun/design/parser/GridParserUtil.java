/*
 * GridParserUtil.java 2009-9-29
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.fun.design.parser;

import java.util.List;
import java.util.Map;

import org.jxstar.fun.design.templet.ElementTemplet;
import org.jxstar.fun.studio.ComboDefine;
import org.jxstar.service.define.ColumnDefine;
import org.jxstar.service.define.FunDefineDao;
import org.jxstar.util.MapUtil;
import org.jxstar.util.StringUtil;
import org.jxstar.util.factory.FactoryUtil;

/**
 * GRID设计文件解析工具类。
 *
 * @author TonyTan
 * @version 1.0, 2009-9-29
 */
public class GridParserUtil {
	//字段长度信息
	private Map<String,String> _fieldLen = null;
	
	/**
	 * 解析字段信息
	 * @param funId
	 * @param tableName
	 * @param designData
	 * @return
	 */
	public String parse(String funId, String tableName, List<Map<String,String>> designData) {
		String retCol = "var cols = null;";
		
		//取字段长度信息
		_fieldLen = PageParserUtil.fieldLength(tableName);
		
		//取字段定义对象
		ColumnDefine colDefine = FunDefineDao.queryColDefine(funId);
		
		//取设计信息：字段编号、显示宽度、是否显示
		List<Map<String,String>> lsDesign = FunDefineDao.queryCol(funId);
		if (lsDesign.isEmpty()) return retCol;
		
		//如果有设计信息，则需要重新组织列的顺序
		if (designData != null && !designData.isEmpty()) {
			lsDesign = dealColData(lsDesign, designData);
		}
		
		//添加选项数据
		StringBuilder comboData = new StringBuilder();
		
		//字段模型数据
		StringBuilder table = new StringBuilder();
		table.append("var cols = [\r\n");
		for (int i = 0, n = lsDesign.size(); i < n; i++) {
			//该字段的设计信息
			Map<String,String> mpDesign = lsDesign.get(i);
			String code = mpDesign.get("col_code");
			String width = MapUtil.getValue(mpDesign, "col_width", "100");
			String hidden = MapUtil.getValue(mpDesign, "col_hidden", "false");
			
			//该字段的定义信息
			Map<String,String> mpColumn = colDefine.getColumnData(code);
			String title = mpColumn.get("col_name");
			String dataType = mpColumn.get("data_type");
			if (dataType != null && (dataType.equals("double") || dataType.equals("number"))) {
				dataType = "float";
			}
			String ctlType = mpColumn.get("col_control");
			String ctlName = mpColumn.get("control_name");
			String gridEdit = mpColumn.get("is_gridedit");
			String format = mpColumn.get("format_id");
			
			//添加选项数据
			if (ctlType.equals("combo") && comboData.indexOf("var "+ctlName+"Data") < 0) {
				if (comboData.length() > 0) comboData.append("\t");
				comboData.append("var "+ctlName+"Data = Jxstar.findComboData('"+ctlName+"');\r\n");
			}
			
			//该字段解析后的字符串
			StringBuilder sbRow = new StringBuilder();
			sbRow.append("\t");
			
			//如果是表格编辑且必填，则给标题添加*
			if (gridEdit.equals("1") && mpColumn.get("is_notnull").equals("1")) {
				title = "*"+title;
			}
			
			//字段名称
			sbRow.append("{col:{header:'"+title+"', width:"+width);
			sbRow.append(", sortable:true");
			
			//是否隐藏
			if (hidden.equals("true")) sbRow.append(", hidden:true");
			
			//处理缺省值
			String defdata = mpColumn.get("default_value").trim();
			if (gridEdit.equals("1")) {
				if (defdata.length() > 0) {
					sbRow = sbRow.append(", defaultval:'"+defdata+"'");
				} else {
					if (ctlType.equals("checkbox")) {
						sbRow = sbRow.append(", defaultval:'0'");
					}
				}
			}
			
			//解析数据或控件
			String fieldjs = "";
			if ((gridEdit.equals("1") || ctlType.equals("combo")) && hidden.equals("false")) {
				fieldjs = editorJs(funId, ctlType, ctlName, mpColumn);
			} else {
				fieldjs = fieldJs(ctlType, ctlName, format);
			}
			
			//解析控件自定义呈现
			String colId = mpColumn.get("col_id");
			fieldjs = customRenderJs(fieldjs, colId);
			
			//添加控件JS
			if (fieldjs != null && fieldjs.length() > 0) {
				sbRow.append(", " + fieldjs);
			}
			
			//字段信息
			sbRow.append("}, field:{name:'"+code.replace(".", "__")+"'");
			sbRow.append(",type:'"+dataType+"'}}");
			
			//行结束
			if (i < n-1) sbRow.append(",");
			sbRow.append("\r\n");
			
			//添加一个字段
			table.append(sbRow);
		}
		table.append("\t];");
		
		if (comboData.length() > 0) {
			comboData.append("\r\n\t");
		}
		
		table = comboData.append(table);
		
		return table.toString();
	}
	
	/**
	 * 解析字段控件的脚本，GRID编辑字段
	 * @param funid
	 * @param ctlType
	 * @param ctlName
	 * @param mpColumn
	 * @return
	 */
	private String editorJs(String funid, String ctlType, String ctlName, Map<String,String> mpColumn) {
		//字段扩展信息对象
		ComboDefine wincfg = new ComboDefine();
		//取元素模板文件
		Map<String,String> elementTpl = ElementTemplet.getInstance().
			getElementMap("grid");
		//模板元素名
		String elname = "editor_" + ctlType;
		//取模板中的控件定义
		String retJs = MapUtil.getValue(elementTpl, elname, "");
		
		//处理是否必填
		String isnotnull = MapUtil.getValue(mpColumn, "is_notnull", "0");
		if (isnotnull.equals("1")) {
			retJs = retJs.replace("allowBlank:true", "allowBlank:false");
		} else {
			//去掉缺省属性的JS代码
			retJs = retJs.replace(", allowBlank:true", "");
		}
		
		//是否可编辑
		String isedit = MapUtil.getValue(mpColumn, "is_edit", "1");
		
		//如果是表格编辑，必填则是蓝色、非必填则是深蓝色
		String gridEdit = mpColumn.get("is_gridedit");
		if (gridEdit.equals("1") && isedit.equals("1")) {
			String color = (isnotnull.equals("1")) ? "#0000ff" : "#3039b4";
			retJs = retJs.replace("editable:true", "editable:true, hcss:'color:"+ color +";'");
		}
		
		//处理是否编辑
		if (gridEdit.equals("0") || isedit.equals("0")) {
			retJs = retJs.replace("editable:true", "editable:false");
		}
		if (gridEdit.equals("1") && isedit.equals("1")) {
		//选择窗口输入栏只能选择，不能输入
			if (ctlType.equals("combowin") || ctlType.equals("selectwin")) {
				//如果设置为可以编辑，则不处理
				String colId = MapUtil.getValue(mpColumn, "col_id");
				if (wincfg.isReadOnly(colId)) {
					retJs = retJs.replace("myeditable:false", "editable:false");
				} else {
					retJs = retJs.replace("myeditable:false", "editable:true");
				}
			}
		}
		
		String fieldCode = mpColumn.get("col_code");
		//处理字段长度
		if (ctlType.equals("text") || ctlType.equals("area") || 
				ctlType.equals("number") || ctlType.equals("combowin") || ctlType.equals("selectwin")) {
			String code = StringUtil.getNoTableCol(fieldCode);
			String datalen = MapUtil.getValue(_fieldLen, code, "100");			
			retJs = retJs.replace("maxLength:100", "maxLength:"+datalen);
		}
		
		//处理数据样式
		String format = mpColumn.get("format_id");
		if (ctlType.equals("number")) {
			if (retJs.length() > 0) retJs += ",";
			//处理数字控件保留精度
			retJs = decimalPrecision(retJs, format);
			//处理数据控件显示精度
			retJs += numberRender(format);
		}
		if (ctlType.equals("date")) {
			retJs = dateRender(retJs, format);
		}
		
		//处理选项值
		if (ctlType.equals("combo")) {
			retJs = retJs.replaceAll("\\{name\\}", ctlName);
		}
		
		//处理选择控件的参数对象
		if (ctlType.equals("combowin") || ctlType.equals("selectwin")) {
			retJs = retJs.replaceAll("\\{funid\\}", funid);
			String config = wincfg.configJson(funid, fieldCode);
			retJs = retJs.replaceAll("\\{config\\}", config);
		}
		
		return retJs;
	}
	
	/**
	 * 解析字段数据的脚本，非GRID编辑字段
	 * @param ctlType -- 控件类型
	 * @param ctlName -- 控件名称
	 * @param format -- 数据样式
	 * @return
	 */
	private String fieldJs(String ctlType, String ctlName, String format) {
		//取元素模板文件
		Map<String,String> elementTpl = ElementTemplet.getInstance().
			getElementMap("grid");
		//模板元素名
		String elname = "render_" + ctlType;
		//取模板中的呈现定义
		String retJs = MapUtil.getValue(elementTpl, elname, "");
		
		//处理数据样式
		if (ctlType.equals("number")) {
			if (retJs.length() > 0) retJs += ",";
			retJs += numberRender(format);
			return retJs;
		}
		if (ctlType.equals("date")) {
			retJs = dateRender(retJs, format);
		}
		
		//处理选项值
		if (ctlType.equals("combo")) {
			retJs = retJs.replaceAll("\\{name\\}", ctlName);
		}
		
		return retJs;
	}
	
	/**
	 * 处理数字类型的格式
	 * @param format -- 数字格式：int, number2, number3...
	 * @return
	 */
	private String numberRender(String format) {
		String retJs = "";
		
		if (format.equals("int")) {
			retJs = "renderer:JxUtil.formatInt()";
		} else if (format.indexOf("number") >= 0) {
			char n = '2';
			if (format.length() > 6) n = format.charAt(6);

			retJs = "renderer:JxUtil.formatNumber("+n+")";
		} else {
			retJs = "renderer:JxUtil.formatNumber(2)";
		}
		
		return retJs;
	}
	
	/**
	 * 处理数字的小数位，必须在控件中加精度参数
	 * @param retJs -- 解析串
	 * @param format -- 数据格式
	 * @return
	 */
	private String decimalPrecision(String retJs, String format) {
		if (format.indexOf("number") >= 0) {
			char n = '2';
			if (format.length() > 6) n = format.charAt(6);

			retJs = retJs.replaceFirst("maxLength:", "decimalPrecision:"+ n +", maxLength:");
		}
		
		return retJs;
	}
	
	/**
	 * 处理日期类型的格式
	 * @param retJs -- 原控件JS
	 * @param format -- 日期格式：date, datetime, datemonth
	 * @return
	 */
	private String dateRender(String retJs, String format) {
		
		if (format.equals("datetime")) {
			retJs = retJs.replace("'Y-m-d'", "'Y-m-d H:i:s'");
		} else if (format.equals("datemin")) {
			retJs = retJs.replace("'Y-m-d'", "'Y-m-d H:i'");
		} else if (format.equals("datemonth")) {
			retJs = retJs.replace("'Y-m-d'", "'Y-m'");
		} else if (format.equals("dateyear")) {
			retJs = retJs.replace("'Y-m-d'", "'Y'");
		}
		
		return retJs;
	}
	
	/**
	 * 给字段添加自定义呈现JS：
	 * 如果js中有renderer:属性，则替换renderer函数；
	 * 如果js中没有renderer:属性，则添加renderer属性，并添加自定义函数。
	 * @param fieldJs -- 解析后的字段控件JS
	 * @param fieldId -- 字段定义ID
	 * @return
	 */
	private String customRenderJs(String fieldJs, String fieldId) {
		Map<String,String> mpColExt = FunDefineDao.queryColExt(fieldId);
		if (mpColExt.isEmpty()) {
			return fieldJs;
		}
		
		//取自定义函数JS
		String customjs = mpColExt.get("customjs");
		if (customjs == null || customjs.trim().length() == 0) {
			return fieldJs;
		}
		
		//取自定义呈现的JS
		String newJs;
		if (fieldJs.indexOf("renderer:") > -1) {
			String[] fieldJss = fieldJs.split("renderer:");
			newJs = fieldJss[0] + "renderer:" + customjs;
		} else {
			newJs = fieldJs + ",\r\nrenderer:" + customjs;
		}
		
		return newJs;
	}
	
	/**
	 * 按设计信息顺序组织列信息，并且把列的宽度、是否隐藏加上，字段名中的.转__
	 * @param columnData -- 字段信息
	 * @param designData -- 设计信息
	 * @return
	 */
	private List<Map<String,String>> dealColData(List<Map<String,String>> columnData, 
			List<Map<String,String>> designData) {
		List<Map<String,String>> lsData = FactoryUtil.newList();
		
		//保存已设计的字段名称
		StringBuilder sbfield = new StringBuilder();
		for (int i = 0, n = designData.size(); i < n; i++) {
			Map<String,String> mpdes = designData.get(i);
			
			String colname = mpdes.get("n").replace("__", ".");
			String colwidth = mpdes.get("w");
			String colhidden = mpdes.get("h");
			
			Map<String,String> mpcol = null;
			for (int j = 0, m = columnData.size(); j < m; j++) {
				mpcol = columnData.get(j);
				
				if (colname.equals(mpcol.get("col_code"))) {
					mpcol.put("col_width", colwidth);
					mpcol.put("col_hidden", colhidden);
					
					break;
				} else {
					mpcol = null;
				}
			}
			
			if (mpcol != null) {
				lsData.add(mpcol);
				sbfield.append(colname+";");
			}
		}
		
		//如果新加了字段，则字段列表会比设计列表长，则需要把新加的字段作为隐藏字段排列在后面
		int cs = columnData.size();
		if (cs > lsData.size()) {
			for (int j = 0; j < cs; j++) {
				Map<String,String> mpcol = columnData.get(j);
				String fn = mpcol.get("col_code");
				
				//如果该字段在设计信息中没有，则添加到设计信息中，并缺省为隐藏
				if (sbfield.indexOf(fn) < 0) {
					mpcol.put("col_hidden", "true");
					lsData.add(mpcol);
				}
			}
		}
		
		//System.out.println("column data=" + lsData.toString());
		
		return lsData;
	}
}
