/*
 * Copyright(c) 2012 Donghong Inc.
 */
package org.jxstar.service.query;

import org.jxstar.test.AbstractTest;

/**
 * 
 *
 * @author TonyTan
 * @version 1.0, 2012-3-9
 */
public class ToolbarQueryTest extends AbstractTest {

	public static void main(String[] args) {
		ToolbarQuery query = new ToolbarQuery();
		String ret = query.createJson("jxstar90374", "tech_check", "chkgrid", "");
		System.out.println("..........执行完成=" + ret);//jxstar47877 jxstar90374
	}

}
