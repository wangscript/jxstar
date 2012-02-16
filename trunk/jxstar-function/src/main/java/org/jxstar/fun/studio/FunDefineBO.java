/*
 * NodeDefine.java 2009-11-10
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.fun.studio;

import java.util.List;
import java.util.Map;

import org.jxstar.dao.DaoParam;
import org.jxstar.dao.DaoUtil;
import org.jxstar.dao.util.BigFieldUtil;
import org.jxstar.service.BoException;
import org.jxstar.service.BusinessObject;
import org.jxstar.service.define.DefineName;
import org.jxstar.service.define.FunctionDefine;
import org.jxstar.service.define.FunctionDefineManger;
import org.jxstar.service.util.ServiceUtil;
import org.jxstar.util.DateUtil;
import org.jxstar.util.FileUtil;
import org.jxstar.util.key.KeyCreator;
import org.jxstar.util.resource.JsMessage;

/**
 * 功能节点定义信息。
 *
 * @author TonyTan
 * @version 1.0, 2009-11-10
 */
public class FunDefineBO extends BusinessObject {
	private static final long serialVersionUID = 1L;

	/**
	 * 新建功能记录
	 * @param funId -- 功能ID
	 * @param moduleId -- 模块ID
	 * @return
	 */
	public String createFun(String funId, String moduleId, String userId) {
		if (funId == null || funId.length() == 0 ||
				moduleId == null || moduleId.length() == 0 ||
				userId == null || userId.length() == 0) {
			setMessage(JsMessage.getValue("nodedefine.cfparamerror"), 
					"funid="+ funId +";moduleid="+ moduleId +";userid="+ userId);
			return _returnFaild;
		}
		
		String insertSql = "insert into fun_base(fun_id, module_id, reg_type, add_date, add_userid) " + 
					 "values(?, ?, 'main', ?, ?)";
		DaoParam param = _dao.createParam(insertSql);
		param.setDsName(DefineName.DESIGN_NAME);
		param.addStringValue(funId);
		param.addStringValue(moduleId);
		param.addDateValue(DateUtil.getTodaySec());
		param.addStringValue(userId);
		
		if (!_dao.update(param)) {
			setMessage(JsMessage.getValue("nodedefine.cferror"));
			return _returnFaild;
		}
		
		return _returnSuccess;
	}
	
	/**
	 * 复制一个新的功能
	 * @param oldFunId -- 原功能ID
	 * @param copyFunId -- 复制后的功能ID
	 * @param mpUser -- 用户信息
	 * @return
	 */
	public String copyFun(String oldFunId, String copyFunId, Map<String,String> mpUser) {		
		//取功能定义对象
		FunctionDefineManger funManger = FunctionDefineManger.getInstance();
		FunctionDefine funObject = funManger.getDefine("sys_fun_base");
		
		//取查询被复制记录数据的SQL
		String select = funObject.getSelectSQL();
		DaoParam param = _dao.createParam(select + " where fun_id = ?");
		
		String dsName = funObject.getElement("ds_name");
		String tableName = funObject.getElement("table_name");
		
		//取被复制的记录值
		param.addStringValue(oldFunId).setDsName(dsName);
		Map<String,String> mpCopy = _dao.queryMap(param);
		mpCopy = DaoUtil.mapAddTable(mpCopy, tableName);
		if (mpCopy == null || mpCopy.isEmpty()) {
			//找不到被复制记录的键值！
			setMessage(JsMessage.getValue("functionbm.copykeynull"));
			return _returnFaild;
		}
		//**********设置新的功能ID，与标准复制功能就这点不同**********/
		mpCopy.put("fun_base.fun_id", copyFunId);
		
		try {
			if (!ServiceUtil.insertRow(mpCopy, mpUser, funObject)) {
				//复制记录失败！
				setMessage(JsMessage.getValue("functionbm.copyfaild"));
				return _returnFaild;
			}
			//复制子表记录
			if (!ServiceUtil.copySubData(oldFunId, copyFunId, mpUser, funObject)) {
				setMessage(JsMessage.getValue("functionbm.subcopyfaild"));
				return _returnFaild;
			}
			
			//复制设计文件
			copyFunDesign(oldFunId, copyFunId, "grid");
			copyFunDesign(oldFunId, copyFunId, "form");
		} catch (BoException e) {
			setMessage(e.getMessage());
			_log.showError(e);
			return _returnFaild;
		}
		
		return _returnSuccess;
	}
	
	/**
	 * 构建JSON对象。
	 * @return
	 */
	public String createJson(String realPath) {
		String sql = "select fun_id, fun_name, layout_page, grid_page, form_page, table_name, "+
					 "pk_col, fk_col, audit_col, subfun_id, show_form, first_field from fun_base " +
					 "where reg_type not in ('nouse') " + 
					 "order by module_id, fun_index ";

		DaoParam param = _dao.createParam(sql);
		param.setDsName(DefineName.DESIGN_NAME);
		List<Map<String,String>> lsfun = _dao.query(param);
		
		//功能ID
		String funid = "";
		//一条功能数据
		StringBuilder sbItem = new StringBuilder();
		//所有功能数据
		StringBuilder sbJson = new StringBuilder("NodeDefine = {\r");
		for (int i = 0, n = lsfun.size(); i < n; i++) {
			Map<String,String> mpfun = lsfun.get(i);
			
			funid = mpfun.get("fun_id");
			if (funid.length() == 0) continue;
			
			sbItem.append("'"+funid+"':{");
			sbItem.append("nodeid:'"+funid+"', ");
			sbItem.append("nodetitle:'"+mpfun.get("fun_name")+"', ");
			sbItem.append("layout:'"+mpfun.get("layout_page")+"', ");
			sbItem.append("gridpage:'"+mpfun.get("grid_page")+"', ");
			sbItem.append("formpage:'"+mpfun.get("form_page")+"', ");
			sbItem.append("tablename:'"+mpfun.get("table_name")+"', ");
			sbItem.append("pkcol:'"+mpfun.get("pk_col").replace(".", "__")+"', ");
			sbItem.append("fkcol:'"+mpfun.get("fk_col").replace(".", "__")+"', ");
			sbItem.append("auditcol:'"+mpfun.get("audit_col").replace(".", "__")+"', ");
			sbItem.append("subfunid:'"+mpfun.get("subfun_id")+"', ");
			sbItem.append("showform:'"+mpfun.get("show_form")+"', ");
			sbItem.append("first:'"+mpfun.get("first_field")+"'");
			
			
			if (i < n-1) {
				sbItem.append("},\r");
			} else {
				sbItem.append("}\r");
			}
			
			sbJson.append(sbItem);
			//清除原控件数据
			sbItem = sbItem.delete(0, sbItem.length());
		}
		sbJson.append("};");
		
		//数据生成文件
		String fileName = realPath + "/public/data/NodeDefine.js";
		String content = sbJson.toString();
		if (!FileUtil.saveFileUtf8(fileName, content)) {
			setMessage(JsMessage.getValue("nodedefine.gdfcerror"));
			return _returnFaild;
		}
		
		return _returnSuccess;
	}
	
	
	/**
	 * 创建一个功能的定义对象。
	 * @param funId
	 * @return
	 */
	public String createNode(String funId) {
		if (funId == null || funId.length() == 0) return "";
		
		String sql = "select fun_id, fun_name, layout_page, grid_page, form_page, table_name, "+
					 "pk_col, fk_col, audit_col, subfun_id from fun_base where fun_id = ? ";

		DaoParam param = _dao.createParam(sql);
		param.addStringValue(funId);
		Map<String,String> mpfun = _dao.queryMap(param);
		if (mpfun.isEmpty()) {
			_log.showWarn("no function define funid:" + funId);
			setMessage(JsMessage.getValue("nodedefine.nofunid"));
			return _returnFaild;
		}
		
		StringBuilder sbItem = new StringBuilder();
		sbItem.append("{nodeid:'"+funId+"', ");
		sbItem.append("nodetitle:'"+mpfun.get("fun_name")+"', ");
		sbItem.append("layout:'"+mpfun.get("layout_page")+"', ");
		sbItem.append("gridpage:'"+mpfun.get("grid_page")+"', ");
		sbItem.append("formpage:'"+mpfun.get("form_page")+"', ");
		sbItem.append("tablename:'"+mpfun.get("table_name")+"', ");
		sbItem.append("pkcol:'"+mpfun.get("pk_col").replace(".", "__")+"', ");
		sbItem.append("fkcol:'"+mpfun.get("fk_col").replace(".", "__")+"', ");
		sbItem.append("auditcol:'"+mpfun.get("audit_col").replace(".", "__")+"', ");
		sbItem.append("subfunid:'"+mpfun.get("subfun_id")+"'}");

		setReturnData(sbItem.toString());
		
		return _returnSuccess;
	}
	
	/**
	 * 复制功能设计文件
	 * @param funId -- 原功能ID
	 * @param newFunId -- 新增功能ID
	 * @param pageType -- 页面类型, grid|form
	 * @return
	 */
	private boolean copyFunDesign(String funId, String newFunId, String pageType) {
		//取原功能的设计信息
		String sql = "select page_content from fun_design where fun_id = '"+ funId +"' and page_type = '"+ pageType +"'";
		String pageContent = BigFieldUtil.readStream(sql, "page_content", DefineName.DESIGN_NAME);
		if (pageContent == null || pageContent.length() == 0) return true; 
		
		//创建新的设计文件记录
		String designId = KeyCreator.getInstance().createKey("fun_design");
		String isql = "insert into fun_design(design_id, fun_id, page_type) values(?, ?, ?)";
		DaoParam param = _dao.createParam(isql);
		param.setDsName(DefineName.DESIGN_NAME);
		param.addStringValue(designId);
		param.addStringValue(newFunId);
		param.addStringValue(pageType);
		_dao.update(param);
		
		//保存新的设计文件
		String usql = "update fun_design set page_content = ? where design_id = '"+ designId +"'";
		return BigFieldUtil.updateStream(usql, pageContent, DefineName.DESIGN_NAME);
	}
}
