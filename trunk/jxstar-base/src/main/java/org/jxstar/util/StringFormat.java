/*
 * StringFormat.java 2010-10-15
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.util;

import java.math.BigDecimal;
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
		} else if (style.indexOf("number") >= 0) {
			if (value.length() == 0) value = "0";			
			String num = style.substring(6, style.length());
			if (num.length() == 0) num = "2";
			
			strRet = StringFormat.StringToNumber(value, Integer.parseInt(num));
		} else if (style.indexOf("money") >= 0) {
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
	 * 将数字格式转为有小数的字符串，如：10000 --> 10000.00
	 * 
	 * @param value -- 数值字符串
	 * @param length -- 小数位数
	 * @return String
	 */
	public static String StringToNumber(String value, int length) {
		String ret = "";
		if (value == null || value.length() == 0) return value;

		if (!StringValidator.validValue(value, StringValidator.DOUBLE_TYPE))
			return value;

		//处理格式化字符
		if (value.indexOf(".") > -1) {
			//有小数位
			String[] strValue = value.split("\\.");
			
			if (strValue[0].length() == 1 && strValue[0].equals("-")) strValue[0] = "-0";
			if (strValue[0].length() == 0) strValue[0] = "0";
			String ditNum = getCurrencyByDigits(strValue[1], length);
			if (ditNum.length() > length) {
				BigDecimal bdValue = new BigDecimal(strValue[0]);
				bdValue = bdValue.add(new BigDecimal("1"));

				ret = bdValue.toString() + "." + ditNum.substring(1, ditNum.length());
			} else ret = strValue[0] + "." + ditNum;
		} else {
			//无小数位
			ret = addDotZero(value, length);
		}
		return ret;
	}

	/**
	 * 返回整数
	 * 
	 * @param value -- 数值字符串
	 * @return String
	 */
	public static String StringToInteger(String value) {
		String ret = "";
		if (value == null || value.length() == 0) return value;

		if (!StringValidator.validValue(value, StringValidator.DOUBLE_TYPE))
			return value;

		//处理格式化字符
		if (value.indexOf(".") > -1) {
			//有小数位
			String[] strValue = value.split("\\.");
			
			ret = strValue[0];
		} else {
			//无小数位
			ret = value;
		}
		return ret;
	}
	
	/**
	 * 添加指定长度0作为小数位值。
	 * 
	 * @param value -- 数值字符串
	 * @param length -- 小数位数
	 * @return String
	 */
	private static String addDotZero(String value, int length) {
		if (value == null || value.length() == 0) return value;
		if (length == 0) return value;

		StringBuilder sbRet = new StringBuilder(value);
		sbRet.append(".");

		for (int i = 0; i < length; i++) {
			sbRet.append("0");
		}

		return sbRet.toString();
	}

	/**
	 * 将数字格式转为货币格式字符串，如：10000 --> 10,000.00
	 * 
	 * @param value	--数值字符串
	 * @param length -- 小数位数
	 * @return 
	 */
	public static String StringToCurrency(String value, int length) {
		String ret = "";
		int num = 3;		//定义逗号分隔位数
		if (value == null || value.length() == 0) return value;

		if (!StringValidator.validValue(value, StringValidator.DOUBLE_TYPE))
			return value;

		//处理格式化字符
		if (value.indexOf(".") > -1) {
			//有小数位
			String[] strValue = value.split("\\.");
			ret = getCurrencyByInteger(strValue[0], num) + "." + getCurrencyByDigits(strValue[1], length);
		} else {
			//无小数位
			if (value.indexOf("-") == -1) {
				ret = getCurrencyByInteger(value, num);
			} else {
				ret = "-" + getCurrencyByInteger(value.substring(1, value.length()), num);
			}
		}

		return ret;
	}

	/**
	 * 构建指定长度的小数值，是小数点后的值
	 * 
	 * @param value -- 小数点后的值
	 * @param length -- 小数位数
	 * @return
	 */
	private static String getCurrencyByDigits(String value, int length) {
		String ret = new String(value);
		if (value.length() > length) {
			ret = value.substring(0, length);

			String strtmp = value.substring(length, length + 1);
			String strint = String.valueOf(Integer.parseInt(ret));
			
			int zeroCnt = ret.length() - strint.length();

			if (Integer.parseInt(strtmp) >= 5) {

				BigDecimal bdValue = new BigDecimal(strint);
				bdValue = bdValue.add(new BigDecimal("1"));

				ret = bdValue.toString();
			} else {
				ret = strint;
			}
			if (ret.length() <= strint.length()) {
				ret = addBeforeZero(ret, ret.length() + zeroCnt);
			}
			
		} else {
			for (int i = 0, n = length - value.length(); i < n; i++) {
				ret += "0";
			}
		}

		return ret;
	}

	/**
	 * 给数字字符串添加,分隔符，组成金额字符串。
	 * 
	 * @param value -- 数值字符串
	 * @param num -- 分隔位数，默认3位
	 * 
	 * @return String
	 */
	private static String getCurrencyByInteger(String value, int num) {
		StringBuilder ret = new StringBuilder();

		String strTmp = null;
		int startPos = 0, endPos = 0;
		int startOffset = 0;
		int totalLength = value.length();

		for (int i = 0, n = (totalLength/num);i <= n; i++){
			if (i == 0) {
				startPos = (num * i);
				startOffset = totalLength - (num * n);
				endPos = startOffset;
				if (startOffset == 0) continue;
			} else {
				startPos = startOffset + (num * (i - 1));
				endPos = startOffset + (num * i);
			}

			strTmp = value.substring(startPos, endPos);

			ret.append(strTmp + ',');
		}

		if (ret.length() == 0) return "";

		return ret.toString().substring(0, ret.toString().length() - 1);
	}

	/**
	 * 数字转换为字符串，处理科学计数法e的问题
	 * @param num -- 数字值
	 * @return
	 */
	public static String doubleFormat(double num){
		 String format = "###";
		 if(Double.toString(num).indexOf(".") > -1){
		 	format = "###.00";
		 }
		 	
		 DecimalFormat decformat = new DecimalFormat(format);
		 return decformat.format(num);
	}
	
	/**
	 * 长度不够的数字前面加0
	 * 
	 * @param value -- 字符串
	 * @param len -- 字符长度
	 * @return String
	 */
	public static String addBeforeZero(String value, int len) {
		int icnt = len - value.length();

		if (icnt <= 0) return value;
		StringBuilder strRet = new StringBuilder(value);
		for (int i = 0; i < icnt; i++) {
			strRet.insert(0, '0');
		}
		
		return strRet.toString();
	}
}
