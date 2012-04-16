/*
 * DmTemplet.java 2010-12-17
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.dm.util;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


import org.jxstar.dao.util.DBTypeUtil;
import org.jxstar.util.factory.FactoryUtil;

/**
 * 数据库生成SQL语句的配置模板，在系统启动加载模板文件，支持多种数据库的文件。
 *
 * @author TonyTan
 * @version 1.0, 2010-12-17
 */
public class DmTemplet {
	private static Map<String,Map<String,String>> _mpElement = FactoryUtil.newMap();
	private static Document _doc;
	
	private static DmTemplet instance = new DmTemplet();
	private DmTemplet(){}
	
	public static DmTemplet getInstance() {
		return instance;
	}
	
	private void init(String fileName) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		DocumentBuilder db = null;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return; 
		}

		try {
			_doc = db.parse(new File(fileName));
		} catch (SAXException e1) {
			e1.printStackTrace();
			_doc = null;
			return; 
		} catch (IOException e1) {
			e1.printStackTrace();
			_doc = null;
			return; 
		}
	}
	
	/**
	 * 加载指定数据库类型的模板文件
	 * @param fileName -- 模板文件名
	 * @param dbType -- 数据库类型
	 * @return
	 */
	public void read(String fileName, String dbType) {
		//初始化文档对象
		init(fileName);
		if (_doc == null) return;
		
		//解析参数值
		Element root = _doc.getDocumentElement();
		//模板文件内容
		Map<String,String> mpe = FactoryUtil.newMap();

		NodeList ndList = root.getElementsByTagName("element");
		for (int i = 0; i < ndList.getLength(); i++) {
			Element node = (Element) ndList.item(i);
			
			String name = node.getAttribute("name");
			String value = node.getTextContent();
			mpe.put(name, value);
		}
		
		_mpElement.put(dbType, mpe);
	}

	/**
	 * 根据数据库类型取元素模板
	 * @param dbType -- 数据库类型
	 * @return
	 */
	public Map<String,String> getElementMap(String dbType) {
		return _mpElement.get(dbType);
	}
	
	/**
	 * 取指定元素的SQL模板
	 * @param name -- 元素名称
	 * @param dsName -- 数据源
	 * @return
	 */
	public String getElement(String name, String dsName) {
		String dbType = DBTypeUtil.getDbmsType(dsName);
		
		Map<String,String> mpele = _mpElement.get(dbType);
		if (mpele == null || mpele.isEmpty()) return "";
		
		String value = mpele.get(name);
		if (value == null) return "";
		
		return value;
	}
}
