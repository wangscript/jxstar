/*
 * SystemUtil.java 2011-1-13
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 系统工具类。
 *
 * @author TonyTan
 * @version 1.0, 2011-1-13
 */
public class SystemUtil {
	
	/**
	* 获取当前操作系统名称.
	* return 操作系统名称 例如:windows xp,linux 等.
	*/
	public static String getOSName() {
		return System.getProperty("os.name").toLowerCase();
	}
	
	/**
	* @return  本机主机名
	*/
	public static String getHostName() {
		InetAddress ia = null;
		try {
			ia = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} 
		if (ia == null ) {
			return "some error..";
		}
		else 
			return ia.getHostName();
	}
	
	/**
	* @return  本机IP 地址
	*/
	public static String getIPAddress() {
		InetAddress ia = null;
		try {
			ia = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} 
		if (ia == null ) {
			return "some error..";
		}
		else 
			return ia.getHostAddress();
	}
	
	/**
	* 测试用的main方法.
	*/
	public static void main(String[] argc) {
		String os = getOSName();
		System.out.println("OS Tyepe:"+os);
		System.out.println("HostName:"+getHostName());
		System.out.println("IPAddress:"+getIPAddress());
		
	}
}
