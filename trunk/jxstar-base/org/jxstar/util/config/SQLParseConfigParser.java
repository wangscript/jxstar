/*
 * SQLParseConfigParser.java 2009-5-28
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.util.config;

import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.jxstar.util.factory.FactoryUtil;

/**
 * SQL语句中的函数解析的配置文件, 解析工具对象.
 * 
 * @author TonyTan
 * @version 1.0, 2009-5-28
 */
public class SQLParseConfigParser extends ConfigParserBase {
	/**
	 * 解析节点集
	 * @param ndList
	 * @return
	 */
	protected Map<String,Object> parseNodeList(NodeList ndList) {
		Map<String,Object> mpRet = FactoryUtil.newMap();
		if (ndList == null) return mpRet;
		
		Node ndTmp = null;
		String sNodeName = "";
		for (int i = 0; i < ndList.getLength(); i++) {
			ndTmp = ndList.item(i);
			if (ndTmp.toString() == null) continue;
			if (ndTmp.getNodeValue() != null) continue;
			if (ndTmp.getFirstChild() == null) continue;
			
			Map<String,Object> mpSub = FactoryUtil.newMap();
			
			Node ndTmpSub = null;
			NodeList ndTmpList = ndTmp.getChildNodes();
			for (int j = 0; j < ndTmpList.getLength(); j++) {
				ndTmpSub = ndTmpList.item(j);
				if (ndTmpSub.toString() == null) continue;
				if (ndTmpSub.getNodeValue() != null) continue;
				
				String sNodeValue = "";
				if (ndTmpSub.getFirstChild() != null) {
					sNodeValue = ndTmpSub.getFirstChild().getNodeValue().trim();
				}
				mpSub.put(ndTmpSub.getNodeName(), sNodeValue);
				//函数ID作为键值
				if (ndTmpSub.getNodeName().equals("functionid")) {
					sNodeName = sNodeValue;
				}
				
				//System.out.println("	" + ndTmpSub.getNodeName() + "=" + sNodeValue);
			}					
			mpRet.put(sNodeName, mpSub);

			//System.out.println("	<" + sNodeName + ">");
		}
		
		return mpRet;
	}
}