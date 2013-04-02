/*
 * StringUtil.java 2010-12-17
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * 字段串常用工具类。
 *
 * @author TonyTan
 * @version 1.0, 2010-12-17
 */
public class StringUtil {
	//回车换行符
	public static final String ENTER = "\r\n";
	
	/**
	 * 给where子句添加括弧
	 * @param where
	 * @return
	 */
	public static String addkf(String where) {
		if (where == null || where.length() == 0) return "";
		
		return "(" + where.trim() + ")";
	}
	
	/**
	 * 把字符串转换为数值，如果是非数值字符，则转为0
	 * @param value -- 数值
	 * @return
	 */
	public static double tonum(String value) {
		if (value == null || value.length() == 0) return 0;
		
		double d = 0;
		try {
			d = Double.parseDouble(value);
		} catch(Exception e) {
			d = 0;
		}
		
		return d;
	}
	
	/**
	 * 判断一个字符串是否是数字
	 * @param str
	 * @return
	 */
	public static boolean isNum(String str){
		return str.matches("^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$");
	}

	/**
	 * 转换字符串中的特殊符号，适用JSON描述，前三个用得比较少，忽略。
	 *      "\b": '\\b',
            "\t": '\\t',
            "\f": '\\f',
            "\n": '\\n',
            "\r": '\\r',
            '"' : '\\"',
            "\\": '\\\\'
	 * @param strVal -- 需要处理的值
	 * @return
	 */
	public static String strForJson(String strVal) {
		StringBuilder sbstr = new StringBuilder();
		for (int i = 0, n = strVal.length(); i < n; i++) {
			char ch = strVal.charAt(i);
			if (ch == '\\') {
				sbstr.append("\\\\");
			} else if (ch == '\r') {
				sbstr.append("\\r");
			} else if (ch == '\n') {
				sbstr.append("\\n");
			} else if (ch == '\'') {
				sbstr.append("\\'");
			} else if (ch == '"') {
				sbstr.append("\\'");
			} else {
				sbstr.append(ch);
			}
		}
		
		return sbstr.toString();
	}
	
	/**
	 * 处理最后一个是空值时将不分隔的问题，
	 * 如："a,b,"分隔后只有两个值，应该有三个值，最后一个是空值
	 * @param value -- 要分隔的值
	 * @param regex -- 分隔字符
	 * @return
	 */
	public static String[] split(String value, String regex) {
		if (value == null || value.length() == 0) return null;
		if (regex == null || regex.length() == 0) return new String[]{value};
		
		//取最后的字符是否为分隔字符
		int spos = value.length() - regex.length();
		if (value.substring(spos).equals(regex)) {
			value += "tempnull";
		}
		
		String[] values = value.split(regex);
		
		//最后一个值设置为空
		int len = values.length;
		if (values[len-1].equals("tempnull")) {
			values[len-1] = "";
		}
		
		return values;
	}
	
	/**
	 * 字符串的编码格式转换，一般是utf-8与gbk之间的转换
	 * @param value -- 要转换的字符串
	 * @param oldCharset -- 原编码格式
	 * @param newCharset -- 新编码格式
	 * @return
	 */
	public static String convEncoding(byte[] value, String oldCharset, String newCharset) {
		OutputStreamWriter outWriter = null;
		ByteArrayInputStream byteIns = null;
		ByteArrayOutputStream byteOuts = new ByteArrayOutputStream();
		InputStreamReader inReader = null;
	
		char cbuf[] = new char[1024];
		int retVal = 0;
		try {
			byteIns = new ByteArrayInputStream(value);
			inReader = new InputStreamReader(byteIns,
					oldCharset);
			outWriter = new OutputStreamWriter(byteOuts,
					newCharset);
			while ((retVal = inReader.read(cbuf)) != -1) {
				outWriter.write(cbuf, 0, retVal);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (inReader != null) inReader.close();
				if (outWriter != null) outWriter.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	
		String temp = null;
		try {
			temp = new String(byteOuts.toByteArray(), newCharset);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return temp;
	}
	
	/**
	 * 给URL参数值解码
	 * 
	 * @param String
	 * @return String
	 */
	public static String decodeURLParam(String astrParam) {
        String ret = "";
        try {
            ret = URLDecoder.decode(astrParam, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return ret;
        }
        return ret;
	}
	/**
	 * 给URL参数值编码
	 * 
	 * @param String
	 * @return String
	 */
	public static String encodeURLParam(String astrParam) {
        String ret = "";
        try {
            ret = URLEncoder.encode(astrParam, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return ret;
        }
        return ret;
	}
	/**
	 * 取去掉表名的字段名.
	 * 
	 * @param aColName - 待表名的字段名, 如:tbl.col, 返回col
	 * @return String
	 */
	public static String getNoTableCol(String colName){
		if (colName == null) return colName;
		
		if (colName.indexOf(".") >= 0){
			colName = colName.substring(colName.indexOf(".") + 1, colName.length());
		}
		
		return colName;
	}
	/**
	 * 二进制转字符串 
	 * 
	 * @param byte[] b
	 * @return String
	 */
	public static String replaceURLCode(String strParamValue) {
		if (strParamValue == null) return "";
		
		strParamValue = strParamValue.replaceAll("\\%", "%25");
		strParamValue = strParamValue.replaceAll("\\'", "%27");
		strParamValue = strParamValue.replaceAll("\\&", "%26"); 
		
		return strParamValue;
	}
	/**
	 * 用填充字符串拼多次, 组成新的字符串, 最后一个间隔符将被去掉.
	 * 如SQL语句中添加“?,”，添加多个后，最后一个“,”去掉。
	 * 
	 * @param fill - 填充字符串
	 * @param num - 填充次数
	 * @return
	 */
	public static String getFillString(String fill, int num) {
		if (fill == null || fill.length() == 0) {
			return "";
		}
		if (num <= 0) {
			return "";
		}
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < num; i++) {
			sb.append(fill);
		}
		
		String tmp = sb.toString();
		return tmp.substring(0, tmp.length() - 1);
	}
	/**
	 * 字节转换为16进制字符串
	 * @param bytes -- 字节值
	 * @return
	 */
	public static String byte2hex(byte[] bytes) {
		if (bytes == null || bytes.length == 0) return "";
		
		StringBuilder hex = new StringBuilder();
	    for (int i = 0; i < bytes.length; i++) {
		    int val = ((int) bytes[i]) & 0xff;
		    if (val < 16)
		    	hex.append("0");
		    hex.append(Integer.toHexString(val));
	    }
	    return hex.toString().toUpperCase();
	}
	
}
