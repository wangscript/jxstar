/*
 * FileUtilTest.java 2009-10-25
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.util;

import org.jxstar.util.FileUtil;


/**
 * 
 *
 * @author TonyTan
 * @version 1.0, 2009-10-25
 */
public class FileUtilTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String path = "d:/aaaa/cc/dd/ee/aa/ff/bb.txt";
		
		String name = FileUtil.getFileName(path);
		System.out.println("================name=" + name);
	}

	public static void saveUtf8() {
		String fileName = "d:/aaaa/cc/dd/ee/aa/ff/bb.txt";
		String  content = "aaaaaaaaaaaaaaaa";
		
		FileUtil.saveFileUtf8(fileName, content);
	}
}
