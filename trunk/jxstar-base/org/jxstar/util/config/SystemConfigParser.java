/*
 * SystemConfigParser.java 2009-5-28
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.util.config;

import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.jxstar.util.factory.FactoryUtil;

/**
 * 配置文件解析工具类, 数据以节点方式存放的.
 * 
 * @author TonyTan
 * @version 1.0, 2009-5-28
 */
public class SystemConfigParser extends ConfigParserBase {
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
			
			sNodeName = ndTmp.getNodeName();
			//System.out.println("<" + sNodeName + ">");
			//是否有孙节点
			boolean hasson = ndTmp.getChildNodes().item(1).
										getChildNodes().getLength() > 1;
			if (hasson) {
				//多节点解析,如DataSource的解析
				mpRet.put(sNodeName, parseNode2List(ndTmp));				
			} else {
				//单节点解析,如Ftp的解析
				mpRet.put(sNodeName, parseNode2Map(ndTmp));
			}
			//System.out.println("</" + sNodeName + ">");
		}
		
		return mpRet;
	}

	/**
	 * 多节点解析
	 */
	private List<Map<String,Object>> parseNode2List(Node node) {
		List<Map<String,Object>> lsRet = FactoryUtil.newList();

		Node ndSub = null;
		NodeList ndList = node.getChildNodes();
		for (int j = 0; j < ndList.getLength(); j++) {
			ndSub = ndList.item(j);
			if (ndSub.toString() == null) continue;
			if (ndSub.getNodeValue() != null) continue;
			if (ndSub.getFirstChild() == null) continue;
			
			//String sNodeName = ndSub.getNodeName();
			//System.out.println("	<" + sNodeName + ">");
			
			Node ndk = null;
			NodeList nlk = ndSub.getChildNodes();
			Map<String,Object> mpk = FactoryUtil.newMap();
			for (int k = 0; k < nlk.getLength(); k++) {
				ndk = nlk.item(k);
				if (ndk.toString() == null) continue;
				if (ndk.getNodeValue() != null) continue;
				
				String sNodeValue = "";
				if (ndk.getFirstChild() != null) {
					sNodeValue = ndk.getFirstChild().getNodeValue().trim();
				}
				mpk.put(ndk.getNodeName(), sNodeValue);
				
				//System.out.println("		" + ndk.getNodeName() + "=" + sNodeValue);
			}
			lsRet.add(mpk);
			
			//System.out.println("	</" + sNodeName + ">");
		}
		
		return lsRet;
	}
	
	/**
	 * 单节点解析
	 */
	private Map<String,Object> parseNode2Map(Node node) {
		Map<String,Object> mpRet = FactoryUtil.newMap();
		
		Node ndSub = null;
		NodeList ndList = node.getChildNodes();
		for (int i = 0; i < ndList.getLength(); i++) {
			ndSub = ndList.item(i);
			if (ndSub.toString() == null) continue;
			if (ndSub.getNodeValue() != null) continue;
			
			String sNodeValue = "";
			if (ndSub.getFirstChild() != null) {
				sNodeValue = ndSub.getFirstChild().getNodeValue().trim();
			}
			mpRet.put(ndSub.getNodeName(), sNodeValue);
			
			//System.out.println("	" + ndSub.getNodeName() + "=" + sNodeValue);
		}		
		
		return mpRet;
	}
}
