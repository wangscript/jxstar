/*
 * ConfigParserBase.java 2009-5-28
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.util.config;

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
 * 配置文件解析抽象类。
 * 
 * @author TonyTan
 * @version 1.0, 2009-5-28
 */
public abstract class ConfigParserBase implements ConfigParser {
	//配置文件名
	private String _fileName;
	//文档解析对象
	private Document _doc;
	
	public void init(String sFileName) {
		_fileName = sFileName;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		DocumentBuilder db = null;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return; 
		}

		try {
			_doc = db.parse(new File(_fileName));
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
	 * 
	 * @return
	 */
	public Map<String,Object> readConfig() {
		Map<String,Object> mpRet = FactoryUtil.newMap();
		if (_doc == null) return mpRet;

		//解析根节点
		Element root = _doc.getDocumentElement();
		
		NodeList ndList = root.getChildNodes();
		mpRet = parseNodeList(ndList);
		
		return mpRet;
	}
	
	 /**
	 * 解析节点集
	 * @param ndList
	 * @return
	 */
	protected abstract Map<String,Object> parseNodeList(NodeList ndList);
}
