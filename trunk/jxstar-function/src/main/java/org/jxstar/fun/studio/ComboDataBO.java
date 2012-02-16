/*
 * ComboData.java 2009-10-20
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.fun.studio;

import java.util.List;
import java.util.Map;


import org.jxstar.dao.DaoParam;
import org.jxstar.service.BusinessObject;
import org.jxstar.service.define.DefineName;
import org.jxstar.util.FileUtil;
import org.jxstar.util.resource.JsMessage;

/**
 * 构建选项控件数据的JSON对象。
 *
 * @author TonyTan
 * @version 1.0, 2009-10-20
 */
public class ComboDataBO extends BusinessObject {
	private static final long serialVersionUID = 1L;

	/**
	 * 生成所有选项控件的数据JSON对象文件。
	 * @return
	 */
	public String createJson(String realPath) {
		String sql = "select control_code, value_data, display_data from funall_control "+
					 "where control_type = 'combo' order by control_code, control_index ";
		
		DaoParam param = _dao.createParam(sql);
		param.setDsName(DefineName.DESIGN_NAME);
		List<Map<String,String>> lsctl = _dao.query(param);
		
		//控件代号
		String curCode = "";
		//一个控件的数据
		StringBuilder sbItem = new StringBuilder();
		//所有控件的数据
		StringBuilder sbJson = new StringBuilder("ComboData = {\r");
		for (int i = 0, n = lsctl.size(); i < n; i++) {
			Map<String,String> mpctl = lsctl.get(i);
			
			String ctlCode = mpctl.get("control_code");
			String ctlValue = mpctl.get("value_data");
			String ctlDisplay = mpctl.get("display_data");
			
			if (ctlCode.length() == 0) continue;
			
			//开始一个新的控件
			if (!curCode.equals(ctlCode)) {
				//如果不是第一个控件
				if (i > 0) {
					String item = sbItem.substring(0, sbItem.length()-1);
					sbJson.append(item).append("],\r");
					
					//清除原控件数据
					sbItem = sbItem.delete(0, sbItem.length());
				}
				
				sbItem.append("\t'" + ctlCode + "':[");
				//当前控件代号
				curCode = ctlCode;
			}
			sbItem.append("['").append(ctlValue).append("','").append(ctlDisplay).append("'],");
		}
		//最后一个控件
		if (sbItem.length() > 0) {
			String item = sbItem.substring(0, sbItem.length()-1);
			sbJson.append(item).append("]\r};");
		} else {
			sbJson.append("};");
		}
		
		//数据生成文件
		String fileName = realPath + "/public/locale/combo-lang-zh.js";
		String content = sbJson.toString();
		if (!FileUtil.saveFileUtf8(fileName, content)) {
			setMessage(JsMessage.getValue("combodata.fcerror"));
			return _returnFaild;
		}
		
		return _returnSuccess;
	}
}
