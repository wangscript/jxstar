/*
 * MapUtil.java 2009-5-28
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.util;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 从MAP中读取数据的工具类.
 * 
 * @author TonyTan
 * @version 1.0, 2009-5-28
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class MapUtil {
	
	/**
	 * 从map中取double类型数值
	 * @param mp
	 * @param param
	 * @return
	 */
	public static double getDouble(Map mp, String param) {
		return getDouble(mp, param, "0.00");
	}
	
	public static double getDouble(Map mp, String param, String defval) {
		String value = getValue(mp, param, defval);
		return Double.parseDouble(value);
	}
	
	/**
	 * 从map中取int类型数值
	 * @param mp
	 * @param param
	 * @return
	 */
	public static int getInt(Map mp, String param) {
		return getInt(mp, param, "0");
	} 
	
	public static int getInt(Map mp, String param, String defval) {
		String value = getValue(mp, param, defval);
		return Integer.parseInt(value);
	}

	/**
	 * 从map中取字符串值, 如果找不到对象, 则返回空串.
	 * 
	 * @param mp - MAP对象
	 * @param param - 参数名
	 * @return String
	 */
    public static String getValue(Map mp, String param) {
		return getValue(mp, param, "");
	}
	
	/**
	 * 从map中取字符串值, 如果找不到对象, 则返回缺省值.
	 * 如果值是数组，则把数组值拼成字符串。
	 * 
	 * @param mp - MAP对象
	 * @param param - 参数名
	 * @param defval - 缺省值
	 * @return
	 */
	public static String getValue(Map mp, String param, String defval) {
		if (mp == null) return defval;
		if (param == null) return defval;
		
		String s;
		Object obj = mp.get(param);
		if (obj == null) return defval;
		
		//如果是数组，把字符拼成字符串
		if (obj instanceof String[]) {
			s = ArrayUtil.arrayToString((String[]) obj);
		} else {
			s = (String) obj;
			s = s.trim();
		}
		
		if (s.length() == 0) return defval;
		
		return s;
	}
	
	/**
	 * 返回参数数组值.
	 * 
	 * @param mp - MAP对象
	 * @param name - 参数名称
	 * @return String[]
	 */
	public static String[] getValues(Map mp, String name) {
		String[] asRet = null;
		Object obj = mp.get(name);
		
		if (obj instanceof String) {
			asRet = new String[]{(String) obj};
		} else if (obj instanceof String[]) {
			asRet = (String[]) obj;
		} else {
			asRet = new String[0];
		}
		
		return asRet;
	}
	
	/**
	 * 返回所有参数的参数名数组.
	 * 
	 * @param mp - MAP对象
	 * @return String[]
	 */
    public static String[] getParameterNames(Map mp) {
		if (mp == null || mp.isEmpty()) {
			return new String[0];
		}
		
		Set<String> key = mp.keySet();
		String[] asRet = key.toArray(new String[key.size()]);

		return asRet;
	}	
	
	/**
	 * 判断是否有记录, 如: select count(*) as cnt from tabl 查询的结果集中,
	 * 如果cnt的值大于0, 则返回true, 否则返回false;
	 * 
	 * @param mp - 查询结果集
	 * @return boolean
	 */
	public static boolean hasRecord(Map<String,String> mp) {
		return hasRecodNum(mp) > 0;
	}
	
	/**
	 * 取记录条数, 如: select count(*) as cnt from tabl 查询的结果集中,
	 * cnt字段的值。
	 * 
	 * @param mp - 查询结果集
	 * @return 记录数
	 */
	public static int hasRecodNum(Map<String,String> mp) {
		if (mp == null || mp.isEmpty()) return 0;
		
		String sCnt = mp.get("cnt");
		if (sCnt == null || sCnt.length() == 0) {
			sCnt = mp.get(mp.keySet().iterator().next());
			if (sCnt == null || sCnt.length() == 0) {
				sCnt = "0";
			}
		}
		
		return Integer.parseInt(sCnt);
	}
	
	/**
	 * map数据转换为json格式
	 * @param mpData
	 * @return
	 */
	public static String toJson(Map<String,String> mpData) {
		if (mpData == null || mpData.isEmpty()) return "{}";
		
		Iterator<String> itr = mpData.keySet().iterator();
		StringBuilder sbOne = new StringBuilder("{");
		while(itr.hasNext()) {
			String key = itr.next();
			String value = mpData.get(key);
			
			if (value != null && (value.equals("true") || value.equals("false"))) {
				sbOne.append("'"+ key +"':"+ value +",");
			} else {
				sbOne.append("'"+ key +"':'"+ StringUtil.strForJson(value) +"',");
			}
		}
		return sbOne.substring(0, sbOne.length()-1) + "}";
	}
	
	/**
	 * 输出Map对象中值，用于测试配置文件中值是否正确.
	 * 
	 * @param mp
	 * @return
	 */	
	public static String toString(Map mp) {
		return toString(mp, null);
	}
	
	/**
	 * 输出Map对象中值，用于测试配置文件中值是否正确.
	 * 
	 * @param mp
	 * @return
	 */
	private static String toString(Map mp, StringBuilder sb) {
		if (mp == null || mp.isEmpty()) return "map is empty...";
		if (sb == null) {
			sb = new StringBuilder();
		}
		
		Iterator<String> itr = mp.keySet().iterator();
		while(itr.hasNext()) {
			String sName = (String)itr.next();
			Object obj = mp.get(sName);
			
			if (obj instanceof String) {
				sb.append("	" + sName + "=" + obj + "\r\n");
			} else if (obj instanceof String[]) {
				String[] objs = (String[]) obj;
				for (String val : objs) {
					sb.append("	" + sName + "=" + val + "\r\n");
				}
			} else if (obj instanceof Map) {
				sb.append("	<" + sName.toString() + ">\r\n");
				toString((Map) obj, sb);
				sb.append("	</" + sName.toString() + ">\r\n");
			} else if (obj instanceof List) {
				List ls = (List) obj;
				for (int i = 0; i < ls.size(); i++) {
					Object lsObj = ls.get(i);
					if (lsObj instanceof Map) {
						sb.append("	<" + sName.toString() + "_ls>\r\n");
						toString((Map)lsObj, sb);
						sb.append("	</" + sName.toString() + "_ls>\r\n");
					} else {
						sb.append("list value=" + obj.toString() + "\r\n");
					}
				}
			}
		}
		
		return sb.toString();
	}
}
