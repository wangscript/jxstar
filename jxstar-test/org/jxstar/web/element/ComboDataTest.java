/*
 * ComboDataTest.java 2009-10-20
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.web.element;

import java.util.Map;


import org.jxstar.dao.BaseDao;
import org.jxstar.test.AbstractTest;

/**
 * 
 *
 * @author TonyTan
 * @version 1.0, 2009-10-20
 */
public class ComboDataTest extends AbstractTest {

	/**
	 * @param args
	 */
	@SuppressWarnings("rawtypes")
	public static void main(String[] args) {
		//String realPath = "D:\\Tomcat6\\webapps\\jxstar\\";
		// TODO Auto-generated method stub
		//ComboData comboData = new ComboData();
		//NodeDefine comboData = new NodeDefine();
		//comboData.createJson(realPath);
		//System.out.println(comboData.getReturnData());
		
		//TreeData treeData = new TreeData();
		//treeData.createJson(realPath);
		//System.out.println("=========treeData=" + treeData.getReturnData());
		
		BaseDao dao = BaseDao.getInstance();
		Map mp = dao.queryMap(dao.createParam("select * from run_mal_record where mal_id = 'MAO070864e1010700aa'"));
		System.out.println("=========treeData=" + mp.toString());
	}

}
