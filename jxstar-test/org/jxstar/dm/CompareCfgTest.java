/*
 * CompareCfgTest.java 2010-12-25
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.dm;

import java.util.List;


import org.jxstar.dm.DmException;
import org.jxstar.dm.compare.CompareDB;
import org.jxstar.test.AbstractTest;

/**
 * 配置信息测试类。
 *
 * @author TonyTan
 * @version 1.0, 2010-12-25
 */
public class CompareCfgTest extends AbstractTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//CompareCfgBO compare = new CompareCfgBO();
		//compare.compareCfg();
		
		//OracleMetaData meta = new OracleMetaData();
		//List<Map<String,String>> ls1 = meta.getIndexMeta("fun_event", "default");
		//System.out.println("=============" + ls1.toString());
		
		CompareDB cfg = new CompareDB();
		try {
			List<String> ls = cfg.compareTable();
			System.out.println("=============" + ls.toString());
		} catch (DmException e) {
			e.printStackTrace();
		}
	}

}
