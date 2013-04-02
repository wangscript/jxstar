/*
 * JsMessage.java 2008-4-13
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.util.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.jxstar.util.log.Log;

/**
 * 读取返回给前台信息资源文件的工具, 资源文件工具都要实现init()方法.
 * 
 * @author TonyTan
 * @version 1.0, 2008-4-13
 */
public class JsMessage {
	//系统日志对象
	private static Log _log = Log.getInstance();
	//邦定资源文件
	private static String _filename = null;
	//邦定资源文件对象
	private static ResourceBundle _resource = null;
	
	/**
	 * 初始化资源文件对象
	 * 
	 * @param sFileName
	 */
	public static void init(String sFileName) {
		if (sFileName == null || sFileName.length() == 0) {
			sFileName = "jxstar";
		}
		
		Locale locale = Locale.getDefault();
		_filename = sFileName + "_" + locale.getLanguage() + 
								"_" + locale.getCountry() + ".properties";
		
		try {
			_resource = new PropertyResourceBundle(
					new FileInputStream(new File(_filename)));
		} catch (FileNotFoundException e) {
			_log.showError(e);
		} catch (IOException e) {
			_log.showError(e);
		}
	}//对象资源文件值
	
	/**
	 * 取资源文件中信息.
	 * 
	 * @param key - 健值
	 * @return String
	 */
	public static String getValue(String key) {
		try {
			//有时由于没有设置正确国家地区，就找不到资源文件
			if (_resource == null) {
				return "not find "+_filename+"!";
			} else {
				return _resource.getString(key);
			}
		} catch (MissingResourceException e) {
			return "nokey='" + key + "'";
		}
	}
	
	/**
	 * 取资源文件中信息, 可以带一个参数.
	 * 如根据健值取到的字符串为: hello,{0}!,参数将替换{0}字符串.
	 * 
	 * @param key - 健值
	 * @param param - 参数值
	 * @return String
	 */
	public static String getValue(String key, Object ... params) {
		String value = getValue(key);
		
		return MessageFormat.format(value, params);
	}
}
