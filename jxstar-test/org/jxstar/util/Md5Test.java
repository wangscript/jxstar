/*
 * Md5Test.java 2010-11-23
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.util;

import org.jxstar.security.Password;

/**
 * 密码生成测试类。
 *
 * @author TonyTan
 * @version 1.0, 2010-11-23
 */
public class Md5Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String pass = "888";
		System.out.println(Password.md5(pass));
		
		String old = "123456789abcdefghijklmnopqrstuvwxyz";
		String en = Password.encrypt(old);
		System.out.println(en);
		System.out.println(Password.decrypt(en));
		System.out.println(Md5Test.md5(old));
	}

	public static String md5(String source) {
		byte[] src = source.getBytes();
	
		String s = null;
		char hexDigits[] = {					// 用来将字节转换成 16 进制表示的字符
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',  'E', 'F'}; 
	
		try {
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
			md.update(src);
			byte tmp[] = md.digest();			// MD5 的计算结果是一个 128 位的长整数，
			// 用字节表示就是 16 个字节
			char str[] = new char[16 * 2]; 
			// 所以表示成 16 进制需要 32 个字符
			int k = 0;							// 表示转换结果中对应的字符位置
			for (int i = 0; i < 16; i++) {					// 从第一个字节开始，对 MD5 的每一个字节
															// 转换成 16 进制字符的转换
				byte byte0 = tmp[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];	// 取字节中高 4 位的数字转换, 
				// >>> 为逻辑右移，将符号位一起右移
				str[k++] = hexDigits[byte0 & 0xf];			// 取字节中低 4 位的数字转换
			} 
			s = new String(str);							// 换后的结果转换为字符串
		}catch(Exception e){
			e.printStackTrace();
		}
		return s;
	}
	//0A:11:3E:F6:B6:18:20:DA:A5:61:1C:87:0E:D8:D5:EE
	//0A113EF6B61820DAA5611C870ED8D5EE
}
