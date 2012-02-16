/*
 * FormPageParserTest.java 2009-10-31
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.design;


import org.jxstar.dao.util.BigFieldUtil;
import org.jxstar.fun.design.ReadDesignBO;
import org.jxstar.fun.design.templet.ElementTemplet;
import org.jxstar.fun.design.templet.PageTemplet;
import org.jxstar.service.define.DefineName;
import org.jxstar.test.AbstractTest;

/**
 * form页面生成类
 *
 * @author TonyTan
 * @version 1.0, 2009-10-31
 */
public class FormPageParserTest extends AbstractTest {
	public static void main(String[] args) {
	    test2();
	}
	
	public static void test2() {
	    String page = readDesignPage("ph_dayreport", "form");
	    System.out.println(".........page=" + page);
	}
	
    private static String readDesignPage(String funcId, String pageType){
        String sql = "select page_content from fun_design " +
                "where fun_id = '"+ funcId +"' and page_type = '"+ pageType +"' ";
        
        return BigFieldUtil.readStream(sql, "page_content", DefineName.DESIGN_NAME);
    }
	
	public static void test1() {
	       PageTemplet pageTpl = PageTemplet.getInstance();
	        pageTpl.read(path+"/WEB-INF/tpl/form-page-tpl.txt", "form");
	        //System.out.println(pageTpl.content());
	        
	        ElementTemplet elTpl = ElementTemplet.getInstance();
	        elTpl.read(path+"/WEB-INF/tpl/form-element-tpl.xml", "form");
	        //System.out.println(elTpl.element("columnModel"));
	        
	        //GridPageParser parse = new GridPageParser();
	        //parse.parseGrid("sys_fun_col");
	        ReadDesignBO read = new ReadDesignBO();
			read.readGrid("car_app", path);
			System.out.println(read.getReturnData());
	}
	
}
