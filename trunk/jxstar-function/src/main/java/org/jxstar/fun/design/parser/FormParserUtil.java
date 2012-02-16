/*
 * FormParserUtil.java 2010-10-15
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.fun.design.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jxstar.fun.design.templet.ElementTemplet;
import org.jxstar.fun.studio.ComboDefine;
import org.jxstar.service.define.ColumnDefine;
import org.jxstar.service.define.FunDefineDao;
import org.jxstar.util.MapUtil;
import org.jxstar.util.StringUtil;
import org.jxstar.util.StringValidator;
import org.jxstar.util.factory.FactoryUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * FORM设计文件解析工具类。
 *
 * @author TonyTan
 * @version 1.0, 2010-10-15
 */
public class FormParserUtil {
	
	//取元素模板文件
	private Map<String,String> _elementTpl = null;
	//字段长度信息
	private Map<String,String> _fieldLen = null;
	
	//当前功能ID
	private String _funId = "";
	
	//用于保存当前FORM的选项数据
	private StringBuilder _comboData = new StringBuilder();
	
	//控件行高标准值
	private static final String LINE_HEIGHT = "30";
	
	/**
	 * 解析指定功能的form设计文件
	 * @param funid -- 功能ID
	 * @param tableName -- 表名
	 * @param designFile -- 设计信息
	 * @return
	 */
	public String parse(String funid, String tableName, String designFile) {
		_funId = funid;
		
		//取元素模板文件
		_elementTpl = ElementTemplet.getInstance().getElementMap("form");
		
		//取字段长度信息
		_fieldLen = PageParserUtil.fieldLength(tableName);
		
		//创建解析DOM对象
		Document designDom = createDom(designFile);
		
		//解析根节点
		Element root = designDom.getDocumentElement();
		NodeList lsNode = root.getChildNodes();
		
		//解析页面设计信息后的文件
		String pageJs = parserFormContent(lsNode);
		
		//字段信息变量
		String varItems = _elementTpl.get("var_items").trim();
		varItems = "\t"+varItems.replace("{formitems}", pageJs);
		
		//选项数据 + 字段信息
		pageJs = _comboData.toString() + varItems;
		
		return pageJs;
	}
	
	/**
	 * 解析设计信息的FORM信息；
	 * 如果两个相联的formitem的title参数相同，需要保存到一个fieldset中，处理方式：
	 * 
	 * 
	 * @param lsNode -- formitem设计元素
	 * @return
	 */
	private String parserFormContent(NodeList lsNode) {
		//本page的信息
		StringBuilder sbPage = new StringBuilder();
		
		//保存数量大于1的标题、开始位置、数量
		Map<String,int[]> mpTitle = parserTitle(lsNode);
		//标志开始同名标题，用于解决标题相邻，但不是相邻位置的开始
		boolean is = false;
		
		//解析formitem元素
		for (int i = 0, n = lsNode.getLength(); i < n; i++) {
			Node node = lsNode.item(i);
			String tagName = node.getNodeName();
			if (tagName == null || !tagName.equals("formitem")) continue;
			
			Element element = (Element) node;
			
			//取anchor属性值
			String anchor = element.getAttribute("anchor").trim();
			if (anchor.length() == 0 || 
					!StringValidator.validValue(anchor, StringValidator.DOUBLE_TYPE)) {
				anchor = "100";
			}
			
			//解析fieldset相关属性
			String title = element.getAttribute("title").trim();
			boolean collapsed = element.getAttribute("collapsed").trim().equalsIgnoreCase("true");
			boolean collapsible = element.getAttribute("collapsible").trim().equalsIgnoreCase("true");
			//如果form设置了title说明是fieldset控件
			StringBuilder sbset = new StringBuilder();
			if (title.length() > 0) {
				sbset.append("\r\n\t\t\t");
				sbset.append("border:true,\r\n\t\t\t");
				sbset.append("xtype:'fieldset',\r\n\t\t\t");
				sbset.append("title:'"+ title +"',\r\n\t\t\t");
				sbset.append("collapsible:"+ collapsible +",\r\n\t\t\t");
				sbset.append("collapsed:"+ collapsed +",");
			}
			
			//取columnitem元素
			NodeList lsColumn = element.getChildNodes();
			if (lsColumn.getLength() == 0) continue;
			
			//解析本Form信息
			String columnJs = parserColumnContent(lsColumn);
			
			//解析单个formitem元素
			String formJs = _elementTpl.get("formitem").trim();
			
			//解析其中的参数，{fieldset}参数暂时不解析
			formJs = formJs.replace("{anchor}", anchor+'%');
			formJs = formJs.replace("{columnitems}", columnJs);
			
			//如果是同名标题
			if (mpTitle.containsKey(title)) {
				//取同名开始位置、同名标题的数量
				int[] indexs = mpTitle.get(title);
				
				//如果当前标题是开始位置
				if (indexs[0] == i) {
					is = true;
					sbPage.append("{" + sbset + "\r\n\t\t\titems:[");
				}
				//如果同名标题数量小于2，则结束fieldset
				if (indexs[1] < 2) {
					is = false;
					formJs = formJs.replace("{fieldset}", "");
					sbPage.append(formJs + "]\r\n\t\t},");
					mpTitle.remove(title);
				} else {
					//没有开始标志的标题不是相邻的同名标题
					if (is == false) {
						formJs = formJs.replace("{fieldset}", sbset);
						sbPage.append(formJs + ",");
					} else {
						formJs = formJs.replace("{fieldset}", "");
						sbPage.append(formJs + ",");
						indexs[1]--;
					}
				}
			} else {
				formJs = formJs.replace("{fieldset}", sbset);
				sbPage.append(formJs + ",");
			}
		}
		//最后去掉1个结尾字符
		String sret = sbPage.substring(0, sbPage.length()-1);
		
		return sret;
	}
	
	/**
	 * 统计formitem中的标题，只保存相邻标题相同的标题、开始位置、相同数量
	 * @param lsNode
	 * @return 返回Map对象，key -- 标题；val[index, num] -- 标题开始位置、相同数量
	 */
	private Map<String,int[]> parserTitle(NodeList lsNode) {
		//解析formitem中的标题与位置
		List<String> lsTitle = FactoryUtil.newList();
		List<Integer> lsIndex = FactoryUtil.newList();
		for (int i = 0, n = lsNode.getLength(); i < n; i++) {
			Node node = lsNode.item(i);
			String tagName = node.getNodeName();
			if (tagName == null || !tagName.equals("formitem")) continue;
			
			Element element = (Element) node;
			
			//解析formitem的标题
			String title = element.getAttribute("title").trim();
			if (title.length() > 0) {
				lsIndex.add(i);
				lsTitle.add(title);
			}
		}
		//System.out.println("-------------lsTitle=" + lsTitle);
		//System.out.println("-------------lsIndex=" + lsIndex);
		Map<String,int[]> mpTitle = FactoryUtil.newMap();
		
		//统计相邻标题的相同数量
		String ptitle = ""; int pindex = 0; int pnum = 0;
		for (int i = 0, n = lsTitle.size(); i < n; i++) {
			String title = lsTitle.get(i);
			if (ptitle.length() > 0) {
				//如果下一个标题与上一个标题相同，则累计数量
				if (ptitle.equals(title)) {
					pnum++;
					continue;
				} else {
				//如果不相同，且累计数量超过1个，则保存
					if (pnum > 1) {
						mpTitle.put(ptitle, new int[]{pindex, pnum});
					}
				}
			}
			
			pnum = 1;
			ptitle = title;
			pindex = lsIndex.get(i);
		}
		//处理最后一批
		if (pnum > 1) {
			mpTitle.put(ptitle, new int[]{pindex, pnum});
		}
		//System.out.println("-------------mpTitle=" + mpTitle);
		
		return mpTitle;
	}
	
	/**
	 * 解析一个FORM的列信息
	 * @param lsNode -- columnitem设计元素
	 * @return
	 */
	private String parserColumnContent(NodeList lsNode) {
		//本Form的列信息
		StringBuilder sbForm = new StringBuilder();
		
		//解析columnitem元素
		for (int i = 0, n = lsNode.getLength(); i < n; i++) {
			Node node = lsNode.item(i);
			String tagName = node.getNodeName();
			if (tagName == null || !tagName.equals("columnitem")) continue;
			
			Element element = (Element) node;
			
			//取列的宽度
			String colwidth = element.getAttribute("colwidth");
			if (colwidth.length() == 0 && 
					!StringValidator.validValue(colwidth, StringValidator.DOUBLE_TYPE)) {
				colwidth = "0.33";
			}
			
			//取fielditem元素
			NodeList lsField = element.getChildNodes();
			if (lsField.getLength() == 0) continue;
			
			//解析本列字段
			String fieldJs = parserFieldContent(lsField);
			
			//解析单个columnitem元素
			String columnJs = _elementTpl.get("columnitem").trim();
			
			//解析其中的参数
			columnJs = columnJs.replace("{colwidth}", colwidth);
			columnJs = columnJs.replace("{fielditems}", fieldJs);
			
			//拼接字符串
			sbForm.append(columnJs + ",");
		}
		//最后去掉三个结尾字符
		String sret = sbForm.substring(0, sbForm.length()-1);
		
		return sret;
	}
	
	/**
	 * 解析一列的字段信息
	 * @param lsNode -- fielditem设计元素
	 * @return
	 */
	private String parserFieldContent(NodeList lsNode) {
		//本列字段信息
		StringBuilder sbColumn = new StringBuilder();
		sbColumn.append("\r\n");
		
		//取字段定义对象
		ColumnDefine colDefine = FunDefineDao.queryColDefine(_funId);
		
		//解析fielditem元素
		for (int i = 0, n = lsNode.getLength(); i < n; i++) {
			Node node = lsNode.item(i);
			String tagName = node.getNodeName();
			if (tagName == null || !tagName.equals("fielditem")) continue;
			
			Element element = (Element) node;
			
			//字段解析后的JS
			String fieldJs = "{xtype:'emptybox'}";
			
			//取字段信息
			String xtype = element.getAttribute("xtype");
			String colcode = element.getAttribute("colcode");
			
			String anchor = element.getAttribute("anchor");
			if (anchor.length() == 0 || 
					!StringValidator.validValue(anchor, StringValidator.INTEGER_TYPE)) {
				anchor = "100";
			}
			
			String height = element.getAttribute("height");
			if (height.length() == 0 || 
					!StringValidator.validValue(height, StringValidator.INTEGER_TYPE)) {
				height = LINE_HEIGHT;
			}
			
			if (colcode != null && colcode.length() > 0) {
				Map<String,String> mpColumn = colDefine.getColumnData(colcode);
				
				if (mpColumn != null && !mpColumn.isEmpty()) {
					//如果设计信息有控件类型则取设计信息中的，设计中可以改变控件类型
					if (xtype != null && xtype.length() > 0) {
						mpColumn.put("col_control", xtype);
					}
					//设置字段控件显示宽度
					mpColumn.put("anchor", anchor);
					//设置字段控件显示高度，就只有area控件使用
					mpColumn.put("height", height);
					
					//解析字段控件
					fieldJs = fieldJs(mpColumn);
					
					//取字段控件信息
					String ctlType = mpColumn.get("col_control");
					String ctlName = mpColumn.get("control_name");
					
					//添加选项数据
					if (ctlType.equals("combo")) {
						if (_comboData.length() > 0) _comboData.append("\t");
						_comboData.append("var "+ctlName+"Data = Jxstar.findComboData('"+ctlName+"');\r\n");
					}
				}
			}

			//拼接字符串
			sbColumn.append("\t\t\t\t\t" + fieldJs + ",\r\n");
		}
		//最后去掉三个结尾字符
		String sret = sbColumn.substring(0, sbColumn.length()-3);
		
		return sret;
	}
	
	/**
	 * 解析字段控件的脚本
	 * 
	 * @param mpColumn
	 * @return
	 */
	private String fieldJs(Map<String,String> mpColumn) {
		String ctlType = mpColumn.get("col_control");
		String ctlName = mpColumn.get("control_name");
		String colCode = mpColumn.get("col_code");
		
		//取数据长度
		String code = StringUtil.getNoTableCol(colCode);
		String datalen = MapUtil.getValue(_fieldLen, code, "100");	
		
		//取模板中的控件定义
		String retJs = _elementTpl.get(ctlType).trim();
		
		//处理字段长度
		if (ctlType.equals("text") || ctlType.equals("area") || 
				ctlType.equals("number") || ctlType.equals("combowin") || ctlType.equals("selectwin")) {
			retJs = retJs.replace("maxLength:100", "maxLength:"+datalen);
		}
		
		//处理字段控件显示宽度
		String anchor = mpColumn.get("anchor");
		retJs = retJs.replace("{anchor}", anchor+'%');
		
		//处理控件显示高度，只处理area控件
		if (ctlType.equals("area")) {
			String height = mpColumn.get("height");
			int rows = (Integer.parseInt(height)+15)/Integer.parseInt(LINE_HEIGHT);
			if (rows < 2) rows = 2;
			retJs = retJs.replace("height:48", "height:"+(rows*24));
		}
		
		//处理字段名
		String colName = mpColumn.get("col_name");
		retJs = retJs.replace("{col_name}", colName);
		
		//处理字段代码
		retJs = retJs.replace("{col_code}", colCode.replace(".", "__"));
		
		//处理是否必填
		String isnotnull = MapUtil.getValue(mpColumn, "is_notnull", "0");
		if (isnotnull.equals("1")) {
			retJs = retJs.replace("allowBlank:true", 
				"allowBlank:false, labelStyle:'color:#0000FF;', labelSeparator:'*'");
		} else {
			//去掉缺省属性的JS代码
			retJs = retJs.replace(", allowBlank:true", "");
		}
		
		//字段扩展信息对象
		ComboDefine wincfg = new ComboDefine();
		
		//处理是否编辑
		String isedit = MapUtil.getValue(mpColumn, "is_edit", "1");
		if (isedit.equals("0")) {
			retJs = retJs.replace("readOnly:false", "readOnly:true");
			retJs = retJs.replace("disabled:false", "disabled:true");
		} else {
		//选择窗口输入栏只能选择，不能输入
			if (ctlType.equals("combowin") || ctlType.equals("selectwin")) {
				//如果设置为可以编辑，则不处理
				String colId = MapUtil.getValue(mpColumn, "col_id");
				if (wincfg.isReadOnly(colId)) {
					retJs = retJs.replace("editable:true", "editable:false");
				}
			} 
			//去掉缺省属性的JS代码
			retJs = retJs.replace(", readOnly:false", "");
		}
		
		//处理缺省值
		String defdata = mpColumn.get("default_value").trim();
		if (defdata.length() > 0) {
			retJs = retJs.replace("defaultval:''", "defaultval:'"+defdata+"'");
		} else {
			if (ctlType.equals("checkbox")) {
				retJs = retJs.replace("defaultval:''", "defaultval:'0'");
			} else {
				//去掉缺省属性的JS代码
				retJs = retJs.replace(", defaultval:''", "");
			}
		}
		
		//处理数据样式
		String format = mpColumn.get("format_id");
		//处理数字控件样式
		if (ctlType.equals("number")) {
			//处理数据校验
			retJs = retJs.replace("xtype:'numberfield'", "xtype:'numberfield', " + numberFormat(format));
		}
		//处理日期控件样式
		if (ctlType.equals("date")) {
			retJs = dateRender(retJs, format);
		}
		
		//处理选项值
		if (ctlType.equals("combo")) {
			retJs = retJs.replaceAll("\\{name\\}", ctlName);
		}
		
		//处理选择控件的参数对象
		if (ctlType.equals("combowin") || ctlType.equals("selectwin")) {
			retJs = retJs.replaceAll("\\{funid\\}", _funId);
			String config = wincfg.configJson(_funId, colCode);
			retJs = retJs.replaceAll("\\{config\\}", config);
		}
		
		return retJs;
	}
	
	/**
	 * 处理数字类型的格式
	 * @param format -- 数组格式：int, number2, number3...
	 * @return
	 */
	private String numberFormat(String format) {
		String retJs = "";
		
		if (format.equals("int")) {
			retJs = "allowDecimals:false";
		} else if (format.indexOf("number") >= 0) {
			char n = '2';
			if (format.length() > 6) n = format.charAt(6);
			boolean isInt = StringValidator.validValue(""+n, "int");
			if (!isInt) n = '2';

			retJs = "decimalPrecision:"+n;
		} else {
			retJs = "decimalPrecision:2";
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
			retJs = retJs.replace("format:'Y-m-d'", "format:'Y-m-d H:i:s'");
		} else if (format.equals("datemin")) {
			retJs = retJs.replace("format:'Y-m-d'", "format:'Y-m-d H:i'");
		} else if (format.equals("datemonth")) {
			retJs = retJs.replace("format:'Y-m-d'", "format:'Y-m'");
		} else if (format.equals("dateyear")) {
			retJs = retJs.replace("format:'Y-m-d'", "format:'Y'");
		}
		
		return retJs;
	}
	
	/**
	 * 根据字符串创建DOM对象
	 * @param designFile -- 文件内容
	 * @return
	 */
	private Document createDom(String designFile) {
		Document retDom = null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		DocumentBuilder db = null;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return retDom; 
		}

		try {
			ByteArrayInputStream bins = new ByteArrayInputStream(designFile.getBytes("utf-8"));
			
			retDom = db.parse(bins);
		} catch (SAXException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		return retDom;
	}
	
}
