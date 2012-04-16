/*
 * StringValidator.java 2010-10-15
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.util;

import java.util.regex.Pattern;


/**
 * 字符串格式效验类
 * 
 * @author TonyTan
 * @version 1.0, 2010-10-15
 */
public class StringValidator {
	
	//整数
	private static String _regInt = "^\\-?[0-9]+$";
	
	//浮点
	private static String _regDouble = "^\\-?[0-9]*\\.?[0-9]*$";

	//一般日期[2004-1-30]
	private static String _regDate = "^([0-9]{4}\\-[0,1]?[0-9]{1}\\-[0-3]?[0-9]{1})";

	//其它日期[1/30/2004]
	private static String _otherDate = "^([0,1]?[0-9]{1}\\/[0-3]?[0-9]{1}\\/[0-9]{4})";
	
	//数据类型
	public static String DOUBLE_TYPE = "double";
	public static String INTEGER_TYPE = "int";
	public static String DATE_TYPE = "date";
	public static String OTHER_DATE_TYPE = "other_date";

	/**
	 * 判断数据有效性
	 * 
	 * @param value -- 数据值
	 * @param type -- 数据类型，支持：int, double, date, other_date
	 * @return boolean
	 */
	public static boolean validValue(String value, String type) {
		boolean ret = false;

		try {
			if (value == null || value.length() == 0) return true;
			if (type == null || type.length() == 0) return true;
			
			String regex = regex(type);
			if (regex == null || regex.length() == 0) return true;
			
			Pattern p = Pattern.compile(regex);
			//只匹配字符串开始部分
			ret = p.matcher(value).lookingAt();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return ret;
	}
	
	/**
	 * 取匹配的正则表达式
	 * @param type -- 数据类型，支持：int, double, date, other_date
	 * @return
	 */
	private static String regex(String type) {
	    if (type.equalsIgnoreCase("int")) {
	        return _regInt;
	    } else if (type.equalsIgnoreCase("double")){
	        return _regDouble;
	    } else if (type.equalsIgnoreCase("date")){
            return _regDate;
        } else if (type.equalsIgnoreCase("other_date")){
            return _otherDate;
        }
	    return "";
	}
}