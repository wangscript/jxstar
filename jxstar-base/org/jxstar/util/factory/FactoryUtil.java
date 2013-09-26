/*
 * FactoryUtil.java 2009-5-29
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.util.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 创建简单集合对象的工厂对象。
 * 
 * @author TonyTan
 * @version 1.0, 2009-5-28
 */
public class FactoryUtil {	
	/**
	 * 创建List<V>对象。
	 * 
	 * @return
	 */
	public static <V> List<V> newList() {
		return new ArrayList<V>();
	}
	
	/**
	 * 创建HashMap<K, V>对象。
	 * @return
	 */
	public static <K, V> Map<K, V> newMap() {
		return new HashMap<K,V>();
	}
	
	/**
	 * 创建支持高并发的ConcurrentHashMap对象。
	 * @param <K>
	 * @param <V>
	 * @return
	 */
	public static <K, V> Map<K, V> newConMap() {
		return new ConcurrentHashMap<K,V>();
	}
}
