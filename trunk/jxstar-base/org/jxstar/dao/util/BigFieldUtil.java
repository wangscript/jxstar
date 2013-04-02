/*
 * BigFieldUtil.java 2010-10-18
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.dao.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.jxstar.dao.DaoUtil;
import org.jxstar.dao.transaction.TransactionException;
import org.jxstar.dao.transaction.TransactionManager;
import org.jxstar.dao.transaction.TransactionObject;
import org.jxstar.util.factory.SystemFactory;
import org.jxstar.util.log.Log;

/**
 * 大字段操作类。
 *
 * @author TonyTan
 * @version 1.0, 2010-10-18
 */
public class BigFieldUtil {
	private static Log _log = Log.getInstance();
	

	/**
	 * 从数据集中取大字段的值转为字符串
	 * @param sql -- 查询sql
	 * @param fieldName -- 大字段名
	 * @param dsName -- 数据源名
	 * @return
	 */
	public static String readStream(String sql, String fieldName, String dsName) {
		//检验参数的有效性
		if (sql == null || sql.length() == 0) return "";
		if (fieldName == null || fieldName.length() == 0) return "";
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		InputStream ins = null; 
		ByteArrayOutputStream bos = null;
		
		TransactionManager tranMng = (TransactionManager) SystemFactory.
										createSystemObject("TransactionManager");
		TransactionObject tranObj = tranMng.getTransactionObject();
		String dbType = DBTypeUtil.getDbmsType(dsName);
		try {
			try {
				conn = tranObj.getConnection(dsName);
			} catch (TransactionException e) {
				_log.showError(e);
				return "";
			}
			if (conn == null) return "";
			
			ps = conn.prepareStatement(sql);
			if (ps == null) return "";
			
			rs = ps.executeQuery();
			if (rs == null) return "";
			
			//移动到第一条记录
			if (!rs.next()) return "";
			
			if (dbType.indexOf("db2") >= 0) {
				Blob blob = (Blob) rs.getBlob(fieldName);
				
			    //Blob对象转化为InputStream流 
				ins = blob.getBinaryStream();
			} else {
				ins = rs.getBinaryStream(fieldName);
			}
			if (ins == null) return "";
			
			_log.showDebug("read binary stream length=" + ins.available());
			
			//创建目标输出流
			bos = new ByteArrayOutputStream();
			
			//取流中的数据
			int len = 0;
			byte[] buf = new byte[256];
			while ((len = ins.read(buf, 0, 256)) > -1) {
				bos.write(buf, 0, len);
			}
			
			//如果不执行提交方法，在非事务环境中会存在连接泄露
			tranObj.commit();
			
			//目标流转为字符串
			return new String(bos.toByteArray(), "utf-8");
		} catch (Exception e) {
			DaoUtil.closeTranObj(tranObj);
			_log.showError(e);
		} finally {
			try {
				if (ins != null) {ins.close(); ins = null;}
				if (bos != null) {bos.close(); bos = null;}
				
				if (rs != null) {rs.close(); rs = null;}
				if (ps != null) {ps.close(); ps = null;}
			} catch (Exception e) {
				_log.showError(e);
			}
		}
		
		return "";
	}
	
	/**
	 * 把字符串做为流保存到数据表中
	 * @param sql -- SQL语句，形式如update tablename set field = ? where pk = ''
	 * @param data -- 数据内容
	 * @param dsName -- 数据源名
	 * @return
	 */
	public static boolean updateStream(String sql, String data, String dsName) {
		//检验参数的有效性
		if (sql == null || sql.length() == 0) return false;
		if (data == null || data.length() == 0) return false;
		
		InputStream ins = null;
		Connection conn = null;
		PreparedStatement ps = null;
		
		TransactionManager tranMng = (TransactionManager) SystemFactory.
										createSystemObject("TransactionManager");
		TransactionObject tranObj = tranMng.getTransactionObject();
		try {
			conn = tranObj.getConnection(dsName);
			if (conn == null) return false;
			
			ps = conn.prepareStatement(sql);
			if (ps == null) return false;
			
			//字符串转为流
			ins = new ByteArrayInputStream(data.getBytes("utf-8")); 
			//_log.showDebug("update binary stream length=" + ins.available());
			
			ps.setBinaryStream(1, ins, ins.available());
			
			int iret = ps.executeUpdate();
			
			if (iret >= 0) {
				tranObj.commit();
			} else {
				tranObj.rollback();
				return false;
			}
		} catch (Exception e) {
			DaoUtil.closeTranObj(tranObj);
			_log.showError(e);
		} finally {
			try {
				if (ins != null) {ins.close(); ins = null;}
				if (ps != null) {ps.close(); ps = null;}
			} catch (Exception e) {
				_log.showError(e);
			}
		}
		
		return true;
	}
}
