/*
 * RegexTest.java 2009-12-13
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.util;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.jxstar.dao.BaseDao;
import org.jxstar.dao.DaoParam;
import org.jxstar.service.rule.SqlRuleBO;
import org.jxstar.util.ArrayUtil;
import org.jxstar.util.factory.FactoryUtil;

/**
 * 
 *
 * @author TonyTan
 * @version 1.0, 2009-12-13
 */
public class RegexTest {

	/**
	 * @param args
	 */
	@SuppressWarnings("rawtypes")
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		RegexTest test = new RegexTest();
		String sql = "select {CURDATE}, {CURDATETIME}, {CURUSER} from card";
		System.out.println(test.parseConstant(sql));
		
		Method[] ms = SqlRuleBO.class.getMethods();
		System.out.println(ms[0]);
		System.out.println(ms[1]);

		
		String[] a = new String[]{"", ""};
		System.out.println(a.getClass());
		
		Map<String,String> mp = FactoryUtil.newMap();
		System.out.println(mp.getClass().getSuperclass().getInterfaces()[0]);
		System.out.println(mp instanceof Map);
		
		List<String> lsKeyId = FactoryUtil.newList();
		lsKeyId.add("aaaaa");
		lsKeyId.add("bbbbb");
		System.out.println(ArrayUtil.arrayToString(lsKeyId.toArray(new String[lsKeyId.size()])));
		
		BaseDao _dao = BaseDao.getInstance();
		DaoParam param = _dao.createParam("select * from doss");
		List ls = _dao.query(param);
		System.out.println("query:" + ls.isEmpty());
	}

	/**
	 * 解析SQL中的常量值。
	 * @param sql
	 * @param userInfo
	 * @return
	 */
	private String parseConstant(String sql) {
		String regex = "\\{[^}]+\\}";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(sql);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String tag = m.group();
			//取常量的值
			String value = tag.toLowerCase();
			//如果还含{，说明没有解析
			if (value.indexOf("{") >= 0) {
				m.appendReplacement(sb, value);
			} else {
				m.appendReplacement(sb, addChar(value));
			}
		}
		m.appendTail(sb);
		
		return sb.toString();
	}
	
	/**
	 * 字符串两头加上'
	 * @param str
	 * @return
	 */
	private String addChar(String str) {
		StringBuilder sb = new StringBuilder();
		sb.append("'").append(str).append("'");
		
		return sb.toString();
	}
}
