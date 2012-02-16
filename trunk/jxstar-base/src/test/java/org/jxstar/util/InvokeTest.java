package org.jxstar.util;

public class InvokeTest {

	public static boolean hello(String name) {
		System.out.println("InvokeTest.hello = " + name);
		
		return name.length() == 0;
	}
}
