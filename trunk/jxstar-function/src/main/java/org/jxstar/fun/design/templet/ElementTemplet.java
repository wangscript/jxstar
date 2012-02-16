/*
 * ElementTemplet.java 2009-10-31
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.fun.design.templet;

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

import org.jxstar.util.factory.FactoryUtil;

/**
 * 页面元素模板文件
 *
 * @author TonyTan
 * @version 1.0, 2009-10-31
 */
public class ElementTemplet {
	private static Map<String,Map<String,String>> _mpElement = FactoryUtil.newMap();
	private static Document _doc;
	
	private static ElementTemplet instance = new ElementTemplet();
	private ElementTemplet(){}
	
	public static ElementTemplet getInstance() {
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
	
	public void read(String fileName, String pageType) {
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
		
		_mpElement.put(pageType, mpe);
	}

	/**
	 * 根据元素名称取元素模板
	 * @param name
	 * @return
	 */
	public Map<String,String> getElementMap(String pageType) {
		if (pageType.indexOf("grid") >= 0) {
			pageType = "grid";
		} else if (pageType.indexOf("form") >= 0) {
			pageType = "form";
		}
		
		return _mpElement.get(pageType);
	}
}
