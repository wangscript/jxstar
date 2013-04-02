/*
 * ArrayUtil.java 2009-5-28
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.util;

import java.util.List;
import java.util.Map;

import org.jxstar.util.factory.FactoryUtil;

/**
 * 常用功能工具对象。
 * 
 * @author TonyTan
 * @version 1.0, 2009-5-28
 */
public class ArrayUtil {
	
	/**
	 * 把List转换为Json数组，如果为空，则返回[]
	 * @param lsData
	 * @return
	 */
	public static String listToJson(List<Map<String,String>> lsData) {
		if (lsData == null || lsData.isEmpty()) {
			return "[]";
		}
		
		StringBuilder sbJson = new StringBuilder();
		for (int i = 0, n = lsData.size(); i < n; i++) {
			Map<String,String> mpData = lsData.get(i);
			if (mpData.isEmpty()) continue;
			
			sbJson.append(MapUtil.toJson(mpData)).append(",");
		}
		String json = "[]";
		if (sbJson.length() > 0) {
			json = "[" + sbJson.substring(0, sbJson.length()-1) + "]";
		}
		
		return json;
	}
	
	/**
	 * 把字符串数组转换成普通的字符串.
	 * @param astr - 字符串数组
	 * @return
	 */
	public static String arrayToString(String[] astr) {
		return arrayToString(astr, ",");
	}
	
	/**
	 * 把字符串数组转换成普通的字符串, fill来分隔，最后不是截取fill的长度，只截去最后1个字符.
	 * 
	 * @param astr - 字符串数组
	 * @return
	 */
	public static String arrayToString(String[] astr, String fill){
		if (astr == null || astr.length == 0) {
			return "";
		}
		if (fill == null) fill = "";
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0, n = astr.length; i < n; i++) {
			sb.append(astr[i]+fill);
		}
		String tmps = "";
		if (sb.length() > 0) {
			tmps = sb.substring(0, sb.length()-1);
		}
		return tmps;
	}
	
	/**
	 * 向数组最后添加一个字符串。
	 * @param amain - 数组1
	 * @param str - 字符串
	 * @return String[]
	 */
	public static String[] arrayAddString(String[] amain, String str) {
		return arrayAddArray(amain, new String[]{str});
	}
	
	/**
	 * 两个数组拼凑成一个数组。
	 * @param amain - 数组1
	 * @param asub - 数组2
	 * @return String[]
	 */
	public static String[] arrayAddArray(String[] amain, String[] asub) {
		if (amain == null) {
			if (asub == null) {
				return new String[0]; 
			} else {
				return asub;
			}
		} else {
			if (asub == null) {
				return amain; 
			} else {
				String[] aret = new String[amain.length + asub.length];
				System.arraycopy(amain, 0, aret, 0, amain.length);
				System.arraycopy(asub, 0, aret, amain.length, asub.length);
				return aret;
			}
		}
	}

	/**
	 * 取GRID查询语句中的字段名称。
	 * 
	 * @param sSelSQL select语句
	 * @return String[] 字段名中的.用__替换
	 */
	public static String[] getGridCol(String sSelSQL) {		
		String[] asRet = getColArrayBySQL(sSelSQL);
		//字段名中的.用__替换
		for (int i = 0, n = asRet.length; i < n; i++) {
			asRet[i] = asRet[i].replace(".", "__");
		}
		
		return asRet;
	}
	
	/**
	 * 从SELECT语句中提取带表名的字段名数组
	 * 约定SQL的格式为：select field1, field2, ... from table_name where ....
	 * 
	 * @param sSelSQL select语句
	 * @return String[] 字段名中的.用__替换
	 */
	public static String[] getColArrayBySQL(String sSelSQL) {		
		if (sSelSQL == null || sSelSQL.length() == 0) {
			return new String[0];
		}
		
		String sql = sSelSQL.toLowerCase();
		if (sql.indexOf("select ") < 0) {
			return new String[0];
		}

		//获取from前面部分
		String fromSql = sql.substring(7);
		if (fromSql.indexOf(" from ") < 0) {
			return new String[0];
		} 
		String select  = fromSql.substring(0, fromSql.indexOf(" from ")).trim();
		
		if (select == null || select.length() <= 1) {
			return new String[0];
		}

		//分解字段列表
		String[] asCol = select.split(",");
		String[] asRet = new String[asCol.length];
		int asindex = 0;
		for (int i = 0, n = asCol.length; i < n; i++) {
			if (asCol.length == 0) continue;

			asindex = asCol[i].indexOf(" as ");
			if (asindex > -1) {
				asRet[i] = asCol[i].trim().substring(asindex + 3, asCol[i].trim().length()).trim();
			} else {
				asRet[i] = asCol[i].trim();
			}
		}

		return asRet;
	}
	
	/**
	 * 将数组转换成List.
	 * 
	 * @param astr - 字符串数组
	 * @return
	 */
	public static List<String> arrayToList(String[] astr){
		List<String> lsRet = FactoryUtil.newList();
		if (astr == null) {
			return lsRet;
		}
		
		for (int i = 0; i < astr.length;  i ++){
			lsRet.add(astr[i]);
		}
		
		return lsRet;
	}
	
	/**
	 * 将List转换成字符串，用fill参数值来分隔.
	 * 
	 * @param ls - List对象
	 * @param fill -- 分隔符
	 * @return
	 */
	public static String listToString(List<String> ls, String fill){
		String[] astr = listToArray(ls);
		
		return arrayToString(astr, fill);
	}
	
	/**
	 * 将List转换成字符串数组.
	 * 
	 * @param ls - List对象
	 * @return
	 */
	public static String[] listToArray(List<String> ls){
		if (ls == null) return null;
		
		int len = ls.size();
		String[] asRet = new String[len];
		
		asRet = ls.toArray(asRet);
		return asRet;
	}
}
