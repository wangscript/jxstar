/*
 * ConfigParser.java 2009-5-28
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.util.config;

import java.util.Map;

/**
 * 配置文件解析接口。
 * 
 * @author TonyTan
 * @version 1.0, 2009-5-28
 */
public interface ConfigParser {

	/**
	 * 解析对象的初始化方法.
	 * 
	 * @param sFileName - 初始配置文件名
	 */
	public void init(String sFileName);
	
	/**
	 * 解析对象通过该方法读取配置文件.
	 * 
	 * @return Map 如果配置信息是单层节点,则根据键值取到的对象为Map;
	 *             如果配置信息是多层节点,则根据键值取到的对象为List;
	 */
	public Map<String,Object> readConfig();
}
