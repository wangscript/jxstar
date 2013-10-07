/*
 * Copyright(c) 2013 Donghong Inc.
 */
package org.jxstar.service.studio;

import java.util.Map;

import org.jxstar.control.action.RequestContext;
import org.jxstar.dao.DaoParam;
import org.jxstar.service.BusinessObject;
import org.jxstar.service.define.FunDefineDao;
import org.jxstar.service.util.SysUserUtil;
import org.jxstar.util.DateUtil;
import org.jxstar.util.FileUtil;
import org.jxstar.util.MapUtil;
import org.jxstar.util.config.SystemVar;
import org.jxstar.util.key.KeyCreator;
import org.jxstar.util.resource.JsMessage;

/**
 * 拍照的图片上传作为当前记录的附件。
 *
 * @author TonyTan
 * @version 1.0, 2013-6-19
 */
public class CameraBO extends BusinessObject {
	private static final long serialVersionUID = 1L;

	/**
	 * 保存附件，把拍照的临时文件保存到附件存放路径
	 * @param requestContext -- 请求对象，参数有：
	 * 					dataid table_name attach_field datafunid pic_file
	 * @return
	 */
	public String uploadAttach(RequestContext requestContext) {
		//上传附件的数据ID
		String dataId = requestContext.getRequestValue("dataid");
		//业务表名
		String tableName = requestContext.getRequestValue("table_name");
		//取附件相关的字段
		String attachField = requestContext.getRequestValue("attach_field");
		if (dataId.length() == 0 || tableName.length() == 0 || attachField.length() == 0) {
			setMessage(JsMessage.getValue("systembo.attachbo.dataerror"));
			return _returnFaild;
		}
		
		AttachBO attachBO = new AttachBO();
		AttachFieldBO fieldBO = new AttachFieldBO();
		
		//查询是否有指定字段的附件记录，如果有则先删除
		String attachId = fieldBO.queryAttachId(tableName, attachField, dataId);
		if (attachId.length() > 0) {
			attachBO.deleteAttach(attachId);
		}
		
		//保存附件
		if (!saveAttach(requestContext)) {
			return _returnFaild;
		}
		
		//更新业务表中附件字段的值，忽略错误信息
		if (!fieldBO.updateFieldValue(requestContext)) {
			setMessage(JsMessage.getValue("systembo.attachbo.upfield"));
			return _returnFaild;
		}

		return _returnSuccess;
	}
	
	//保存附件记录，并把附件转移到附件目录中
	private boolean saveAttach(RequestContext requestContext) {
		//上传附件的数据ID
		String dataId = requestContext.getRequestValue("dataid");
		//上传附件的功能ID
		String dataFunId = requestContext.getRequestValue("datafunid");
		if (dataId.length() == 0 || dataFunId.length() == 0) {
			setMessage(JsMessage.getValue("systembo.attachbo.dataerror"));
			return false;
		}
		//新增附件记录
		if (!insertRecord(dataId, dataFunId, requestContext)) {
			setMessage("新增附件记录失败！");
			return false;
		}
		
		//取保存文件的完整路径，在insertRecord方法中赋值的
		String filePath = requestContext.getRequestValue("save_path");
		String srcFile = requestContext.getRequestValue("pic_file");
		String fileName = FileUtil.getFileName(srcFile);
		String destFile = filePath + "/" + fileName;
		_log.showDebug("srcfile=" + srcFile + ";destfile=" + destFile);
		//拷贝文件
		boolean ret = FileUtil.moveFile(srcFile, destFile, true);
		if (!ret) {
			setMessage("拷贝附件到指定目录出错！");
			return false;
		}
		return true;
	}
	
	/**
	 * 新增附件记录。
	 * @param dataId -- 数据ID
	 * @param dataFunId -- 数据功能ID
	 * @param requestContext
	 * @return
	 */
	private boolean insertRecord(String dataId, String dataFunId, RequestContext requestContext) {
		//取新增附件ID
		String attachId = KeyCreator.getInstance().createKey("sys_attach");
		//取附件相关的字段
		String attachField = requestContext.getRequestValue("attach_field");
		//取功能基础信息
		Map<String,String> mpFun = FunDefineDao.queryFun(dataFunId);
		//取功能表名
		String tableName = mpFun.get("table_name");
		//取功能名称
		String funName = mpFun.get("fun_name");
		//保存附件到指定文件目录
		String systemPath = SystemVar.getValue("upload.file.path", "D:/ATTACHDOC");
		
		//取附件名
		String srcFile = requestContext.getRequestValue("pic_file");
		String fileName = FileUtil.getFileName(srcFile);
		
		//没有附件名称，在这里赋值
		requestContext.setRequestValue("attach_name", fileName);
		
		//保存附件路径
		String attachPath = systemPath+"/"+tableName+"/";
		requestContext.setRequestValue("save_path", attachPath);
		attachPath += fileName;
		String contentType = "image/pjpeg";
		//取用户信息
		Map<String,String> mpUser = requestContext.getUserInfo();
		String userId = MapUtil.getValue(mpUser, "user_id");
		//跨域上传附件时，没有用户信息，但会传递用户ID
		if (userId.length() == 0) {
			userId = requestContext.getRequestValue("user_id");
			if (userId.length() > 0) {
				mpUser = SysUserUtil.getUserById(userId);
			}
		}
		String userName = MapUtil.getValue(mpUser, "user_name");
		
		StringBuilder sbsql = new StringBuilder();
		sbsql.append("insert into sys_attach (");
		sbsql.append("attach_id, table_name, data_id, attach_name, content_type, attach_field, fun_id,"); 
		sbsql.append("fun_name, attach_path, upload_date, upload_user, add_userid, add_date");
		sbsql.append(") values (?, ?, ?, ?, ?, ?, ?,  ?, ?, ?, ?, ?, ?)");
		
		DaoParam param = _dao.createParam(sbsql.toString());
		param.addStringValue(attachId);
		param.addStringValue(tableName);
		param.addStringValue(dataId);
		param.addStringValue(fileName);
		param.addStringValue(contentType);
		param.addStringValue(attachField);
		param.addStringValue(dataFunId);
		
		param.addStringValue(funName);
		param.addStringValue(attachPath);
		param.addDateValue(DateUtil.getTodaySec());
		param.addStringValue(userName);
		param.addStringValue(userId);
		param.addDateValue(DateUtil.getTodaySec());
		
		return _dao.update(param);
	}
}
