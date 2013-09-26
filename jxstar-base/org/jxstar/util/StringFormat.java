/*
 * StringFormat.java 2010-10-15
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.util;

import java.text.DecimalFormat;

/**
 * 格式化字符串对象
 * 
 * @author TonyTan
 * @version 1.0, 2010-10-15
 */
public class StringFormat {
	/**
	 * 根据样式格式化内容
	 * 
	 * @param value		内容
	 * @param style		样式
	 * 
	 * @return String
	 */
	public static String getDataValue(String value, String style) {
		if (value == null) value = "";
		if (style == null || style.length() == 0) return value;
		
		String strRet = value;
		style = style.toLowerCase();
		
		if (style.equals("text")) {
			strRet = value;
		} else if (style.equals("int")) {
			if (value.length() == 0) value = "0";
			strRet = StringFormat.StringToInteger(value);
		} else if (style.indexOf("number") == 0) {
			if (value.length() == 0) value = "0";			
			String num = style.substring(6, style.length());
			if (num.length() == 0) num = "2";
			
			strRet = StringFormat.StringToNumber(value, Integer.parseInt(num));
		} else if (style.indexOf("money") == 0) {
			if (value.length() == 0) value = "0";
			String num = style.substring(5, style.length());
			if (num.length() == 0) num = "2";
			
			strRet = StringFormat.StringToCurrency(value, Integer.parseInt(num));
		} else if (style.equals("datetime")) {
			if (value.length() > 0) 
				strRet = StringFormat.StringToDateTime(strRet);
		} else if (style.equals("date")) {
			if (value.length() > 0) 
				strRet = StringFormat.StringToDate(strRet);
		} else if (style.equals("datemonth")) {
			if (value.length() > 0) 
				strRet = StringFormat.StringToMonth(strRet);
		}else if (style.equals("datemin")){
			if (value.length() > 0) 
				strRet = StringFormat.StringToDateMin(value);
		} else if (style.equals("dateyear")) {
			if (value.length() > 0) 
				strRet = StringFormat.StringToYear(strRet);
		}

		return strRet;
	}

	/**
	 * 把日期字符串转换为年月日字符串：yyyy-mm-dd
	 * 
	 * @param value -- 日期字符串
	 * @return
	 */
	public static String StringToDate(String value) {
		if (value == null || value.length() == 0) return value;

		//if (!StringValidator.validValue(value, StringValidator.DATE_TYPE))
		//	return value;
		
		String ret[] = value.split(" ");

        if (ret.length == 2) {
            value = ret[0];
        }

		String strValue[] = value.split("-");
		if (strValue.length < 3) return value;
		
		return strValue[0] +"-"+ strValue[1] +"-"+ strValue[2];
	}

	/**
	 * 把日期字符串转换为年月字符串：yyyy-mm
	 * 
	 * @param value -- 日期字符串
	 * @return
	 */
	public static String StringToMonth(String value) {
		if (value == null || value.length() == 0) return value;

		//if (!StringValidator.validValue(value, StringValidator.DATE_TYPE))
		//	return value;

		String strValue[] = value.split("-");
		if (strValue.length < 2) return value;
		
		return strValue[0] +"-"+ strValue[1];
	}
	
	/**
	 * 把日期字符串转换为年字符串：yyyy
	 * 
	 * @param value -- 日期字符串
	 * @return
	 */
	public static String StringToYear(String value) {
		if (value == null || value.length() == 0) return value;

		//if (!StringValidator.validValue(value, StringValidator.DATE_TYPE))
		//	return value;

		String strValue[] = value.split("-");		
		return strValue[0];
	}
	
	/**
	 * 把日期时间字符串转换为：yyyy-mm-dd hh:mi
	 * @param value -- 日期字符串
	 * @return
	 */
	public static String StringToDateMin(String value){
		if (value == null || value.length() == 0) return value;
		String ret[] = value.split(" ");

		if (ret.length == 2) {
			String strTmp[] = ret[1].split(":");
			
			int n = strTmp.length;
			if (n > 1){
				value = (new StringBuilder(ret[0] + " " + strTmp[0] + ":" + strTmp[1])).toString();
			}else if ( n == 1){
				value = (new StringBuilder(ret[0] + " " + strTmp[0] + ":00")).toString();
			}else{
				value = (new StringBuilder(ret[0] + " 00:00")).toString();
			}
		}else{
			value = (new StringBuilder(ret[0] + " 00:00")).toString();
		}

		return value;
	}

	/**
	 * 把日期时间字符串中最后的[.0]两个字符去掉
	 * @param value -- 日期字符串
	 * @return
	 */
	public static String StringToDateTime(String value) {
		String es = value.substring(value.length()-2, value.length());
		if (es.equals(".0")) {
			value = value.substring(0, value.length()-2);
		}

		return value;
	}

	/**
	 * 将数字格式转为有小数的字符串，如：10000 --> 10000.00，但会去掉尾部的0，包括.0
	 * 
	 * @param value -- 数值字符串
	 * @param length -- 小数位数
	 * @return String
	 */
	public static String StringToNumber(String value, int length) {
		if (value == null || value.length() == 0) return value;

		if (!StringValidator.validValue(value, StringValidator.DOUBLE_TYPE))
			return value;
		
		StringBuilder sb = new StringBuilder("###");
		sb = fillDotNum(sb, '#', length);
		DecimalFormat df = new DecimalFormat(sb.toString());
		return df.format(Double.parseDouble(value));
	}

	/**
	 * 返回整数
	 * 
	 * @param value -- 数值字符串
	 * @return String
	 */
	public static String StringToInteger(String value) {
		if (value == null || value.length() == 0) return value;

		if (!StringValidator.validValue(value, StringValidator.DOUBLE_TYPE))
			return value;

		DecimalFormat df = new DecimalFormat("###");
		return df.format(Double.parseDouble(value));
	}

	/**
	 * 将数字格式转为货币格式字符串，如：10000 --> 10,000.00，但会去掉尾部的0，包括.0
	 * 
	 * @param value	--数值字符串
	 * @param length -- 小数位数
	 * @return 
	 */
	public static String StringToCurrency(String value, int length) {
		if (value == null || value.length() == 0) return value;

		if (!StringValidator.validValue(value, StringValidator.DOUBLE_TYPE))
			return value;

		StringBuilder sb = new StringBuilder("###,###");
		sb = fillDotNum(sb, '#', length);
		
		DecimalFormat df = new DecimalFormat(sb.toString());
		return df.format(value);
	}

	/**
	 * 数字转换为字符串，处理科学计数法e的问题
	 * @param num -- 数字值
	 * @return
	 */
	public static String doubleFormat(double num){
		//预留足够的小数位
		DecimalFormat decformat = new DecimalFormat("###.#########");
		return decformat.format(num);
	}
	
	//添加连续的小数位格式化字符，#表示无数字则不显示，0表示无数字显示0，如：
	//###.### 2345.34 --> 2345.34  23.34567 --> 23.346
	//###.000 2345.34 --> 2345.340 23.34567 --> 23.346
	private static StringBuilder fillDotNum(StringBuilder sb, char ch, int num) {
		if (num > 0) {
			sb.append('.');
			for (int i = 0; i < num; i++) {
				sb.append(ch);
			}
		}
		return sb;
	}
}
