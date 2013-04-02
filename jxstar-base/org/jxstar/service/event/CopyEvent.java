/*
 * CopyEvent.java 2009-5-29
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.service.event;

import java.util.List;
import java.util.Map;


import org.jxstar.control.action.RequestContext;
import org.jxstar.dao.DaoParam;
import org.jxstar.dao.DaoUtil;
import org.jxstar.service.BoException;
import org.jxstar.service.BusinessEvent;
import org.jxstar.service.util.FunStatus;
import org.jxstar.service.util.ServiceUtil;
import org.jxstar.util.factory.FactoryUtil;
import org.jxstar.util.key.CodeCreator;
import org.jxstar.util.resource.JsMessage;
import org.jxstar.util.resource.JsParam;

/**
 * 业务记录复制事件。
 *
 * @author TonyTan
 * @version 1.0, 2009-5-29
 */
public class CopyEvent extends BusinessEvent {
	private static final long serialVersionUID = -3926852032696125924L;
	
	//保存复制新增的记录ID
	private List<String> _lsNewKeyId = FactoryUtil.newList(); 

	/**
	 * 执行复制事件
	 */
	public String copy(RequestContext requestContext) {
		try {
			init(requestContext);
		} catch (BoException e) {
			_log.showError(e);
			return _returnFaild;
		}
		
		String[] asKey = requestContext.getRequestValues(JsParam.KEYID);
		if (asKey == null || asKey.length == 0) {
			//找不到被复制记录的键值！
			setMessage(JsMessage.getValue("functionbm.copykeynull"));
			return _returnFaild;				
		}
		
		//获取复制记录条数
		String copynum = requestContext.getRequestValue("copynum");
		if (copynum == null || copynum.length() == 0) {
			copynum = "1";
		}
		int icopynum = Integer.parseInt(copynum);
		
		//取查询被复制记录数据的SQL
		String where = _funObject.getWhereSQL();
		if (where.length() > 0) {
			where = where + " and ";
		}
		String select = _funObject.getSelectSQL();
		String fullsql = select + " where " + where + _pkColName + " = ?";
		_log.showDebug("copy select sql=" + fullsql);
		
		//循环复制被选择的每条记录
		for (int i = 0; i < asKey.length; i++) {
			String sKeyID = asKey[i];
			
			try {
				if (!copyRow(fullsql, sKeyID, icopynum, requestContext)) {
					//复制记录失败！
					setMessage(JsMessage.getValue("functionbm.copyfaild"));
					return _returnFaild;
				}
			} catch (BoException e) {
				_log.showError(e);
				setMessage(e.getMessage());
				return _returnFaild;
			}
		}
		
		//把新增复制的主键值存入环境参数中
		if (!_lsNewKeyId.isEmpty()) {
			requestContext.setRequestValue(JsParam.KEYID, 
					_lsNewKeyId.toArray(new String[_lsNewKeyId.size()]));
		}
		
		return _returnSuccess;
	}

	/**
	 * 给指定的记录复制多条.
	 * 
	 * @param selSQL - 被复制的记录查询语句
	 * @param sCopyID - 被复制的记录主键值
	 * @param icopynum - 复制记录条数
	 * @param requestContext - 环境对象
	 * @return boolean
	 */
	private boolean copyRow(String selSQL, String sCopyID, int icopynum, 
			RequestContext requestContext) throws BoException {
		DaoParam param = _dao.createParam(selSQL);
		param.addStringValue(sCopyID).setDsName(_dsName);
		Map<String,String> mpCopy = _dao.queryMap(param);
		mpCopy = DaoUtil.mapAddTable(mpCopy, _tableName);
		if (mpCopy == null || mpCopy.isEmpty()) {
			//找不到被复制记录的键值！
			throw new BoException(JsMessage.getValue("functionbm.copykeynull"));
		}
		
		for (int i = 0; i < icopynum; i++) {
			//创建主键值
			String sKeyID = ServiceUtil.createPkValue(mpCopy, requestContext);
			if (sKeyID == null || sKeyID.length() == 0) {
				//新增记录时生成的主键值为空！
				throw new BoException(JsMessage.getValue("functionbm.newcopykeynull"));
			}
			mpCopy.put(_pkColName, sKeyID);
			
			//创建编码值
			if (_codeColName != null && _codeColName.length() > 0) {
				String sCode = CodeCreator.getInstance().createCode(_funID, mpCopy);
				if (sCode == null || sCode.length() == 0) {
					//复制记录时生成的单据编码为空！
					throw new BoException(JsMessage.getValue("functionbm.newcopycodenull"));
				}
				mpCopy.put(_codeColName, sCode);
			}
			
			//取复制列, 如果复制列为编码列, 则不处理复制列的值；复制多条只添加一个'-'
			String copyCol = _funObject.getElement("copy_col");
			if (i == 0 && copyCol != null && copyCol.length() > 0) {
				if (!copyCol.equals(_codeColName)) {
					String copyVal = (String) mpCopy.get(copyCol);
					copyVal += COPYFLAG;
					mpCopy.put(copyCol, copyVal);
				}
			}
			//取设置的业务状态值
			String audit0 = FunStatus.getValue(_funID, "audit0", "0");
			//取签字列
			String auditCol = _funObject.getElement("audit_col");
			if (auditCol != null && auditCol.length() > 0) {
				mpCopy.put(auditCol, audit0);
			}
			
			if (!ServiceUtil.insertRow(mpCopy, _userInfo, _funObject)) {
				//复制记录失败！
				throw new BoException(JsMessage.getValue("functionbm.copyfaild"));
			}
			
			//复制子表记录
			if (!ServiceUtil.copySubData(sCopyID, sKeyID, _userInfo, _funObject)) {
				throw new BoException(JsMessage.getValue("functionbm.subcopyfaild"));
			}
			
			//保存复制新增的主键ID
			_lsNewKeyId.add(sKeyID);
		}
		
		_log.showDebug("copy success, new keyid is " + sCopyID);		
		return true;
	}
}
