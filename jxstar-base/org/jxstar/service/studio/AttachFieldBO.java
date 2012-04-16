package org.jxstar.service.studio;

import java.util.Map;

import org.jxstar.control.action.RequestContext;
import org.jxstar.dao.DaoParam;
import org.jxstar.service.BusinessObject;
import org.jxstar.service.define.FunDefineDao;
import org.jxstar.util.resource.JsMessage;

/**
 * 实现表单中的附件字段控件。
 *
 * @author TonyTan
 * @version 1.0, 2011-11-22
 */
public class AttachFieldBO extends BusinessObject {
	private static final long serialVersionUID = 1L;

	/**
	 * 删除指定字段附件，同时清空业务表中附件字段的值
	 * @param requestContext -- 请求对象
	 * @return
	 */
	public String deleteAttach(RequestContext requestContext) {
		//上传附件的数据ID
		String dataId = requestContext.getRequestValue("dataid");
		//上传附件的功能ID
		String dataFunId = requestContext.getRequestValue("datafunid");
		//取附件相关的字段
		String attachField = requestContext.getRequestValue("attach_field");
		//取功能基础信息
		Map<String,String> mpFun = FunDefineDao.queryFun(dataFunId);
		//取功能表名
		String tableName = mpFun.get("table_name");
		//取主键字段名
		String keyName = mpFun.get("pk_col");
		if (dataId.length() == 0 || tableName.length() == 0 || attachField.length() == 0) {
			setMessage(JsMessage.getValue("systembo.attachbo.dataerror"));
			return _returnFaild;
		}
		
		AttachBO attachBO = new AttachBO();
		//查询是否有指定字段的附件记录，如果有则先删除
		String attachId = queryAttachId(tableName, attachField, dataId);
		if (attachId.length() > 0) {
			String ret = attachBO.deleteAttach(attachId);
			if (ret.equals(_returnFaild)) {
				setMessage(attachBO.getMessage());
				return _returnFaild;
			}
		}
		
		//忽略错误信息
		clearFieldValue(tableName, attachField, dataId, keyName);
		
		return _returnSuccess;
	}
	
	/**
	 * 查看指定字段附件
	 * @param requestContext -- 用来传递附件信息到前台
	 * @return
	 */
	public String downAttach(RequestContext requestContext) {
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
		
		//查询是否有指定字段的附件记录
		String attachId = queryAttachId(tableName, attachField, dataId);
		if (attachId.length() == 0) {
			setMessage(JsMessage.getValue("systembo.attachbo.noattach"));
			return _returnFaild;
		}
		
		AttachBO attachBO = new AttachBO();
		String ret = attachBO.downAttach(attachId, requestContext);
		if (ret.equals(_returnFaild)) {
			setMessage(attachBO.getMessage());
			return _returnFaild;
		}
		
		return _returnSuccess;
	}
	
	/**
	 * 保存指定字段附件，同时更新业务表中附件字段的值
	 * @param requestContext -- 请求对象
	 * @return
	 */
	public String saveAttach(RequestContext requestContext) {
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
		//查询是否有指定字段的附件记录，如果有则先删除
		String attachId = queryAttachId(tableName, attachField, dataId);
		if (attachId.length() > 0) {
			attachBO.deleteAttach(attachId);
		}
		
		//保存附件
		String ret = attachBO.saveAttach(requestContext);
		if (ret.equals(_returnFaild)) {
			setMessage(attachBO.getMessage());
			return _returnFaild;
		}
		
		//更新业务表中附件字段的值，忽略错误信息
		updateFieldValue(requestContext);

		return _returnSuccess;
	}
	
	//查询指定字段的附件记录
	private String queryAttachId(String tableName, String attachField, String dataId) {
		String sql = "select attach_id from sys_attach where table_name = ? and attach_field = ? and data_id = ?";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(tableName);
		param.addStringValue(attachField);
		param.addStringValue(dataId);
		
		Map<String, String> mpData = _dao.queryMap(param);
		if (mpData.isEmpty()) return "";
		
		return mpData.get("attach_id");
	}
	
	//更新业务表中附件字段的值
	private boolean updateFieldValue(RequestContext requestContext) {
		//上传附件的数据ID
		String dataId = requestContext.getRequestValue("dataid");
		//上传附件的功能ID
		String dataFunId = requestContext.getRequestValue("datafunid");
		//取附件名称
		String attachName = requestContext.getRequestValue("attach_name");
		//取附件相关的字段
		String attachField = requestContext.getRequestValue("attach_field");
		//取功能基础信息
		Map<String,String> mpFun = FunDefineDao.queryFun(dataFunId);
		//取功能表名
		String tableName = mpFun.get("table_name");
		//取主键字段名
		String keyName = mpFun.get("pk_col");
		
		String sql = "update " + tableName + " set " + attachField + " = ? where " + keyName + " = ?";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(attachName);
		param.addStringValue(dataId);
		
		return _dao.update(param);
	}
	
	//清除业务表中附件字段的值
	private boolean clearFieldValue(String tableName, String attachField, String dataId, String keyName) {
		String sql = "update " + tableName + " set " + attachField + " = '' where " + keyName + " = ?";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(dataId);
		
		return _dao.update(param);
	}
}
