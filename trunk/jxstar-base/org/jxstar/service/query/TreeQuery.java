/*
 * TreeQuery.java 2009-11-18
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.service.query;

import java.util.List;
import java.util.Map;

import org.jxstar.control.action.RequestContext;
import org.jxstar.dao.DaoParam;
import org.jxstar.dao.DmDaoUtil;
import org.jxstar.service.BoException;
import org.jxstar.service.BusinessObject;
import org.jxstar.service.util.SysDataUtil;
import org.jxstar.util.ArrayUtil;
import org.jxstar.util.MapUtil;
import org.jxstar.util.factory.FactoryUtil;

/**
 * 查询多级树型数据对象：显示完本级树数据后，还继续检查是否有下级树数据；
 * 点击树节点传递参数有：当前用户ID、当前功能ID、当前节点ID、当前树序号；
 * 
 * 现在针对单级树优化查找是否有下级节点的算法：
 * 原来是每个节点都去检查是否有下级；
 * 现在改为为直接查找所有节点的下级，如：dept_id like '1001%' and dept_level = 3；
 * 再根据查到下级值，有下级值的记录标记为非叶子节点，其它的为叶子节点，如：distinct(substring(dept_id, 1, 8))
 * 用distinct可以减少对比数量，提高效率；
 *
 * @author TonyTan
 * @version 1.0, 2009-11-18
 */
public class TreeQuery extends BusinessObject {
	private static final long serialVersionUID = 1L;
	//根节点ID
	private static final String ROOT_ID = "10";
	//取树定义字段
	private static String _field_tree = DmDaoUtil.getFieldSql("fun_tree");
	//是否只有一个树定义
	private boolean _isOnlyTree = true;
	
	//取树形定义组，支持一个功能可以显示多颗树
	public String queryTeam(RequestContext request) {
		String funId = request.getRequestValue("tree_funid");
		
		List<Map<String,String>> lsTeam = treeTeam(funId);
		String json = ArrayUtil.listToJson(lsTeam);
		setReturnData(json);
		
		return _returnSuccess;
	}

	/**
	 * 取树数据的JSON对象
	 * @param request
	 * @return
	 */
	public String queryTree(RequestContext request) {
		String userId = request.getRequestValue("user_id");
		String funId = request.getRequestValue("tree_funid");
		String parentId = request.getRequestValue("node");
		String treeNo = request.getRequestValue("tree_no");
		String teamId = request.getRequestValue("team_id");//树形组代号，如：A、B、C
		
		String whereSql = request.getRequestValue("where_sql");
		String whereType = request.getRequestValue("where_type");
		String whereValue = request.getRequestValue("where_value");
		
		_log.showDebug("tree_funid=" + funId + ";tree_no=" + treeNo + ";nodeId=" + parentId);
		
		List<Map<String,String>> lsData = queryTreeData(
				userId, funId, parentId, treeNo, 
				whereSql, whereType, whereValue,
				teamId);
		
		String json = ArrayUtil.listToJson(lsData);
		//_log.showDebug("----------------------json=" + json);
		setReturnData(json);
		
		return _returnSuccess;
	}
	
	/**
	 * 查询树形数据，含本级树与下级树数据
	 * @param userId -- 当前用户ID
	 * @param funId -- 当前树形功能ID
	 * @param parentId -- 当前点击节点ID，根为ROOT_ID
	 * @param treeNo -- 当前树序号，根为空
	 * @param whereSql -- 扩展where，一般只用于第一层
	 * @param whereType -- 扩展where，一般只用于第一层
	 * @param whereValue -- 扩展where，一般只用于第一层
	 * @param teamId -- 树形组代号，如：A、B、C
	 * @return
	 */
	public List<Map<String,String>> queryTreeData(String userId, 
			String funId, String parentId, String treeNo,
			String whereSql, String whereType, String whereValue,
			String teamId) {
		if (teamId == null) teamId = "";
		List<Map<String,String>> lsRet = FactoryUtil.newList();
		
		//如果父节点是根节点,则取空字符串
		if (parentId.equals(ROOT_ID)) parentId = "";
		
		//只有一个树定义；后面的子方法中要用到
		_isOnlyTree = onlyTree(funId, teamId);
		
		//取本级树定义
		Map<String,String> mpTree = treeDefine(funId, treeNo, teamId);
		if (mpTree == null || mpTree.isEmpty()) {
			_log.showWarn("don't find tree define...");
			return lsRet;
		}
		
		//取树类型
		String treeType = MapUtil.getValue(mpTree, "tree_type", "0");
		
		//先查询本级树定义是否有数据
		_log.showDebug(".................check current tree data");
		//如果是树
		if (treeType.equals("0")) {
			lsRet = treeData(userId, parentId, mpTree, whereSql, whereType, whereValue, "0");
			lsRet = addTreeLeafOther(userId, parentId, whereSql, whereType, whereValue, lsRet, mpTree, teamId);
		} else {
			//只有根节点，才加载本级节点数据
			if (parentId.length() == 0) {
				lsRet = subTreeData(userId, parentId, mpTree, false);
				lsRet = addNodeLeaf(userId, lsRet, mpTree, teamId);
			}
		}
		//如果只有一个树定义，则不执行下面的查询
		if (!_isOnlyTree) {
			//取下级树定义信息
			Map<String,String> subTree = subTreeDef(funId, treeNo, teamId);
			
			if (!subTree.isEmpty()) {
				//然后查询是否有下级树数据
				_log.showDebug(".................check sub tree data");
				List<Map<String,String>> lsSubNode = 
					subTreeData(userId, parentId, subTree, false);
				
				String subTreeType = MapUtil.getValue(subTree, "tree_type", "0");
				//如果是树
				if (subTreeType.equals("0")) {
					lsSubNode = addTreeLeafOther(userId, parentId, whereSql, whereType, whereValue, lsSubNode, subTree, teamId);
				} else {
					lsSubNode = addNodeLeaf(userId, lsSubNode, subTree, teamId);
				}
				lsRet.addAll(lsSubNode);
			}
		}
		
		//如果没有树数据，且是根节点查询，则构建一个空节点
		if (parentId.length() == 0 && lsRet.isEmpty()) {
			lsRet.add(createRoot(mpTree));
		}
		
		return lsRet;
	}
	
	/**
	 * 查询下级树的数据
	 * @param userId -- 当前用户ID
	 * @param parentId -- 当前点击的父节点ID
	 * @param mpTree -- 下级树定义
	 * @param isCnt -- 是否只统计数量
	 * @return
	 */
	private List<Map<String,String>> subTreeData(String userId, 
			String parentId, Map<String,String> mpTree, boolean isCnt) {
		List<Map<String,String>> lsData = FactoryUtil.newList();
		//树定义为空
		if (mpTree.isEmpty()) return lsData;
		
		//取关联查询字段
		String relatcol = MapUtil.getValue(mpTree, "relat_col");
		
		StringBuilder treesql;
		if (isCnt) {
			treesql = getSelectCnt(mpTree);
		} else {
			treesql = getSelectSql(mpTree);
		}
		
		StringBuilder sbwhere = new StringBuilder();
		//关联查询特殊处理
		if (relatcol.length() > 0) {
			if (relatcol.indexOf("[parentid]") >= 0) {
				sbwhere.append(relatcol.replace("[parentid]", parentId));
			} else {
				sbwhere.append(" (" + relatcol + " = ?) ");
			}
		}
		
		//添加定义的where子句
		String where = mpTree.get("self_where");
		if (where.length() > 0) {
			if (sbwhere.length() > 0) sbwhere.append(" and ");
			sbwhere.append(" (" + parseWhere(where, userId) + ") ");
		}
		
		//添加数据权限控制where子句
		String treeFunId = mpTree.get("self_funid");
		try {
			String dataWhere = SysDataUtil.getDataWhere(userId, treeFunId);
			if (dataWhere.length() > 0) {
				if (sbwhere.length() > 0) sbwhere.append(" and ");
				sbwhere.append(" and " + dataWhere);
			}
		} catch (BoException e) {
			_log.showError(e);
		}
		
		//添加关联字段的where子句
		if (sbwhere.length() > 0) {
			treesql.append(" where ").append(sbwhere);
		}
		
		//添加order子句
		String order = mpTree.get("self_order");
		if (!isCnt && order.length() > 0) {
			treesql.append(" order by " + order);
		}
		if (!isCnt) {
			_log.showDebug("sub tree data sql=" + treesql.toString());
			_log.showDebug("sub tree data id=" + parentId);
		}
		
		DaoParam param = _dao.createParam(treesql.toString());
		param.setUseParse(true);
		param.setDsName(mpTree.get("db_name"));
		
		//如果是替代方式，则不用添加参数了
		if (relatcol.length() > 0 && relatcol.indexOf("[parentid]") < 0) {
			param.addStringValue(parentId);
		}
		
		return _dao.query(param);
	}
	
	/**
	 * 查询本级树的数据
	 * @param userId -- 当前用户ID
	 * @param parentId -- 当前点击的父节点ID
	 * @param mpTree -- 本级树定义
	 * @param whereSql -- 外部扩展的where
	 * @param whereType -- 外部扩展的where
	 * @param whereValue -- 外部扩展的where
	 * @param type -- 0 表示查询树形数据，1 表示统计数量， 2 表示取第2级数据
	 * @return
	 */
	private List<Map<String,String>> treeData(String userId, 
			String parentId, Map<String,String> mpTree,
			String whereSql, String whereType, String whereValue, String type) {
		List<Map<String,String>> lsRet = FactoryUtil.newList();
		//树定义为空
		if (mpTree.isEmpty()) return lsRet;
		//计算当前要查询数据的级别，如果parentID=1001，则级别为2
		String level = getLevel(parentId);
		
		String pkcol = mpTree.get("node_id");
		String levelcol = mpTree.get("node_level");
		
		StringBuilder treesql;
		if (type.equals("1")) {
			treesql = getSelectCnt(mpTree);
		} else if (type.equals("2")) {
			treesql = getSelectParent(mpTree, Integer.parseInt(level));
			//在下下级中查找
			int ilevel = Integer.parseInt(level) + 1;
			level = Integer.toString(ilevel);
		} else {
			treesql = getSelectSql(mpTree);
		}
		
		//添加父节点IDwhere子句
		treesql.append(" where " + pkcol + " like ? ");
		
		//添加级别列的where子句
		if (levelcol.length() > 0 && level.length() > 0) {
			treesql.append(" and " + levelcol + " = ? ");
		}
		//添加定义的where子句
		String where = mpTree.get("self_where");
		if (where.length() > 0) {
			treesql.append(" and (" + parseWhere(where, userId) + ") ");
		}
		
		//添加数据权限控制where子句
		String treeFunId = mpTree.get("self_funid");
		try {
			String dataWhere = SysDataUtil.
				getTreeDataWhere(userId, treeFunId, Integer.parseInt(level));
			if (dataWhere.length() > 0) {
				treesql.append(" and " + dataWhere);
			}
		} catch (BoException e) {
			_log.showError(e);
		}
		
		//添加扩展wheresql
		if (whereSql.length() > 0) {
			treesql.append(" and (" + whereSql + ")");
		}
		
		//添加order子句
		String order = mpTree.get("self_order");
		if (type.equals("0") && order.length() > 0) {
			treesql.append(" order by " + order);
		}
		
		//定义查询参数
		String param0 = parentId + "%";
		String param1 = "string";
		if (levelcol.length() > 0 && level.length() > 0) {
			param0 += ";" + level;
			param1 += ";int";
		}
		
		//添加扩展参数
		if (whereType.length() > 0) {
			param0 += ";" + whereValue;
			param1 += ";" + whereType;
		}
		if (type.equals("0")) {
			_log.showDebug("tree data sql=" + treesql.toString());
			_log.showDebug("tree data param value=" + param0);
			_log.showDebug("tree data param type=" + param1);
		}
		
		DaoParam param = _dao.createParam(treesql.toString());
		param.setUseParse(true);
		param.setDsName(mpTree.get("db_name"));
		param.setValue(param0).setType(param1);
		lsRet = _dao.query(param);
		
		return lsRet;
	}
	
	/**
	 * 检查树中的节点，是否含子数据，此方法被addTreeLeafOther替代
	 * @param userId -- 当前用户ID
	 * @param lsData -- 需要检查是否有子节点的数据
	 * @param mpTree -- 本级树定义
	 * @return
	 */
	/*
	private List<Map<String,String>> addTreeLeaf(String userId, 
			List<Map<String,String>> lsData, Map<String,String> mpTree) {
		if (lsData.isEmpty()) return lsData;
		if (mpTree.isEmpty()) return lsData;
		//不检查下级
		String notcheck = MapUtil.getValue(mpTree, "not_check", "0");
		if (notcheck.equals("1")) return lsData;
		
		//取下级树定义信息
		String funId = mpTree.get("fun_id");
		String treeNo = mpTree.get("tree_no");
		
		Map<String,String> subTree = null;
		if (!_isOnlyTree) {
			subTree = subTreeDef(funId, treeNo);
		}
		
		for (int i = 0, n = lsData.size(); i < n; i++) {
			Map<String,String> mpData = lsData.get(i);
			String dataId = mpData.get("id");
			
			//取本级树中是否有下级数据
			boolean phas = hasChild(treeData(userId, dataId, mpTree, "", "", "", "1"));
			
			//设置是否有下级
			if (!phas) {
				mpData.put("leaf", "true");
				
				//检查下级树是否有数据
				if (subTree != null && !subTree.isEmpty()) {
					boolean has = hasChild(subTreeData(userId, dataId, subTree, true));
					mpData.put("leaf", has?"false":"true");
				}
			} else {
				mpData.put("leaf", "false");
			}
		}
		
		return lsData;
	}*/
	
	/**
	 * 检查树中的节点，是否含子数据：调整算法，根据父ID，直接取到所有下下级记录；
	 * @param userId -- 当前用户ID
	 * @param parentId -- 父ID
	 * @param whereSql -- 外部扩展的where
	 * @param whereType -- 外部扩展的where
	 * @param whereValue -- 外部扩展的where
	 * @param lsData -- 需要检查是否有子节点的数据
	 * @param mpTree -- 本级树定义
	 * @param teamId -- 树形组代号
	 * @return
	 */
	private List<Map<String,String>> addTreeLeafOther(String userId, String parentId,
			String whereSql, String whereType, String whereValue, 
			List<Map<String,String>> lsData, Map<String,String> mpTree,
			String teamId) {
		if (lsData.isEmpty()) return lsData;
		if (mpTree.isEmpty()) return lsData;
		//不检查下级
		String notcheck = MapUtil.getValue(mpTree, "not_check", "0");
		if (notcheck.equals("1")) return lsData;
		
		//取下级树定义信息
		String funId = mpTree.get("fun_id");
		String treeNo = mpTree.get("tree_no");
		
		Map<String,String> subTree = null;
		if (!_isOnlyTree) {
			subTree = subTreeDef(funId, treeNo, teamId);
		}
		
		//取到所有有下级节点的ID
		List<Map<String,String>> lsRet = treeData(userId, parentId, mpTree, whereSql, whereType, whereValue, "2");
		List<String> lsNodeId = mapToList(lsRet, "node_id");
		//_log.showDebug(".........hasSubNode:" + lsNodeId.toString());
		
		for (int i = 0, n = lsData.size(); i < n; i++) {
			Map<String,String> mpData = lsData.get(i);
			String dataId = mpData.get("id");
			
			//取本级树中是否有下级数据
			boolean phas = lsNodeId.contains(dataId);
			
			//设置是否有下级
			if (!phas) {
				mpData.put("leaf", "true");
				
				//检查下级树是否有数据
				if (subTree != null && !subTree.isEmpty()) {
					boolean has = hasChild(subTreeData(userId, dataId, subTree, true));
					mpData.put("leaf", has?"false":"true");
				}
			} else {
				mpData.put("leaf", "false");
			}
		}
		
		return lsData;
	}
	
	//把Map转换到List中
	private List<String> mapToList(List<Map<String,String>> lsData, String field) {
		List<String> lsRet = FactoryUtil.newList();
		for (Map<String,String> mp : lsData) {
			lsRet.add(mp.get(field));
		}
		return lsRet;
	}
	
	/**
	 * 检查节点类型的树，是否含子数据
	 * @param userId -- 当前用户ID
	 * @param lsData -- 需要检查是否有子节点的数据
	 * @param mpTree -- 子级树定义
	 * @param teamId -- 树形组代号
	 * @return
	 */
	private List<Map<String,String>>  addNodeLeaf(String userId, 
			List<Map<String,String>> lsData, Map<String,String> mpTree,
			String teamId) {
		if (lsData.isEmpty()) return lsData;
		if (mpTree.isEmpty()) return lsData;
		//不检查下级
		String notcheck = MapUtil.getValue(mpTree, "not_check", "0");
		if (notcheck.equals("1")) return lsData;
		
		//取当前树信息
		String funId = mpTree.get("fun_id");
		String treeNo = mpTree.get("tree_no");
		
		//检查是否有下级树，没有下级树则都修改为叶子节点
		Map<String,String> subTree = null;
		if (!_isOnlyTree) {
			subTree = subTreeDef(funId, treeNo, teamId);
		}
		
		//如果有下级树，则需要检查下级树中是否含数据
		for (int i = 0, n = lsData.size(); i < n; i++) {
			Map<String,String> mpData = lsData.get(i);
			
			if (subTree == null || subTree.isEmpty()) {
				mpData.put("leaf", "true");
			} else {
				String dataId = mpData.get("id");
				
				boolean has = hasChild(subTreeData(userId, dataId, subTree, true));
				mpData.put("leaf", has?"false":"true");
			}
		}
		
		return lsData;
	}
	
	/**
	 * 根据本级树序号，取下级树定义
	 * @param funId -- 功能ID
	 * @param treeNo -- 本级树序号
	 * @param teamId -- 树形组代号
	 * @return
	 */
	private Map<String,String> subTreeDef(String funId, String treeNo, String teamId) {
		Map<String,String> mpRet = FactoryUtil.newMap();
		if (treeNo.length() == 0) return mpRet;
		
		String sql = "select "+ _field_tree +" from fun_tree " +
				"where fun_id = ?";
		if (teamId.length() > 0) {
			sql += " and team_id = ?";
		}
		sql += " order by tree_no";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(funId);
		if (teamId.length() > 0) {
			param.addStringValue(teamId);
		}
		List<Map<String,String>> lsData = _dao.query(param);
		
		if (lsData.isEmpty()) return mpRet;
		
		boolean has = false;
		for (int i = 0, n = lsData.size(); i < n; i++) {
			Map<String,String> mpData = lsData.get(i);
			if (has) return mpData;
			
			String tree_no = mpData.get("tree_no");
			if (tree_no.equals(treeNo)) {
				has = true;
			}
		}
		
		return mpRet;
	}
	
	/**
	 * 取本级树形定义信息
	 * @param funId -- 功能ID
	 * @param treeNo -- 本级树序号
	 * @param teamId -- 树形组代号
	 * @return
	 */
	private Map<String,String> treeDefine(String funId, String treeNo, String teamId) {
		StringBuilder sb = new StringBuilder("select "+ _field_tree);
		sb.append(" from fun_tree where fun_id = ? ");
		if (treeNo != null && treeNo.length() > 0) {
			sb.append(" and tree_no = ? ");
		}
		if (teamId.length() > 0) {
			sb.append(" and team_id = ?");
		}
		sb.append(" order by tree_no");
		DaoParam param = _dao.createParam(sb.toString());
		param.addStringValue(funId);
		
		if (treeNo != null && treeNo.length() > 0) {
			param.addStringValue(treeNo);
		}
		if (teamId.length() > 0) {
			param.addStringValue(teamId);
		}
		
		return _dao.queryMap(param);
	}
	
	/**
	 * 只有一个树定义
	 * @param funId
	 * @param teamId -- 树形组代号
	 * @return
	 */
	private boolean onlyTree(String funId, String teamId) {
		String sql = "select count(*) as cnt from fun_tree where fun_id = ?";
		if (teamId.length() > 0) {
			sql += " and team_id = ?";
		}
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(funId);
		if (teamId.length() > 0) {
			param.addStringValue(teamId);
		}
		
		Map<String,String> mp = _dao.queryMap(param);
		String cnt = mp.get("cnt");
		return Integer.parseInt(cnt) < 2;
	}
	
	/**
	 * 取查询树形数据的select字段
	 * @param mpTree
	 * @return
	 */
	private StringBuilder getSelectSql(Map<String,String> mpTree) {
		//取树型节点的附加显示值字段名，如果有多个字段名则以’,’分隔。
		String othercol = mpTree.get("node_other");
		//主键字段
		String pkcol = mpTree.get("node_id");
		//节点文本的字段
		String nodeName = mpTree.get("node_name");
		
		//select子句
		StringBuilder treesql = new StringBuilder("select ");
		treesql.append(pkcol + " as id, ");
		
		//构建节点文本字段
		String[] names = nodeName.split(" as ");
		if (names.length > 1) {
			treesql.append(names[0] + " as text, ");
		} else {
			treesql.append(nodeName + " as text, ");
		}
		
		//添加附加值字段
		if (othercol.length() > 0) {
			treesql.append(othercol + ", ");
		}
		//如果过滤SQL中含'号，则必须替换
		String rwhere = mpTree.get("right_where");
		if (rwhere.indexOf("'") >= 0) {
			rwhere = rwhere.replaceAll("'", "''");
		}
		//添加树定义信息
		treesql.append(" '"+ mpTree.get("node_style") +"' as cls, ");
		treesql.append(" '"+ mpTree.get("tree_no") +"' as tree_no, ");
		treesql.append(" '"+ mpTree.get("tree_title") +"' as tree_title, ");
		treesql.append(" '"+ mpTree.get("node_level") +"' as node_level, ");
		treesql.append(" '"+ rwhere +"' as right_where, ");
		treesql.append(" '"+ mpTree.get("table_name") +"' as table_name, ");
		treesql.append(" '"+ mpTree.get("has_level") +"' as has_level, ");
		
		//缺省都不是叶子节点
		treesql.append(" 'true' as leaf ");
		
		treesql.append(" from " + mpTree.get("table_name") + " ");
		
		return treesql;
	}
	
	/**
	 * 构建一个根节点，用于返回树定义信息
	 * @param mpTree
	 * @return
	 */
	private Map<String,String> createRoot(Map<String,String> mpTree) {
		Map<String,String> mpRet = FactoryUtil.newMap();
		
		mpRet.put("id", "10");
		mpRet.put("text", "");
		mpRet.put("leaf", "false");
		mpRet.put("tree_no", mpTree.get("tree_no"));
		mpRet.put("tree_title", mpTree.get("tree_title"));
		mpRet.put("node_level", mpTree.get("node_level"));
		mpRet.put("right_where", mpTree.get("right_where"));
		mpRet.put("table_name", mpTree.get("table_name"));
		
		return mpRet;
	}
	
	/**
	 * 取查询树形数据的select统计数据SQL
	 * @param mpTree
	 * @return
	 */
	private StringBuilder getSelectCnt(Map<String,String> mpTree) {
		StringBuilder treesql = new StringBuilder("select count(*) as cnt ");
		treesql.append(" from " + mpTree.get("table_name") + " ");
		return treesql;
	}
	
	/**
	 * 取查询树形数据的某个级别的ID
	 * @param mpTree
	 * @param level
	 * @return
	 */
	private StringBuilder getSelectParent(Map<String,String> mpTree, int level) {
		String pkcol = mpTree.get("node_id");
		String table = mpTree.get("table_name");
		//取父ID长度
		int len = level*4;
		
		StringBuilder treesql = new StringBuilder("select distinct({SUBSTR}("+ pkcol +", 1, "+ len +")) as node_id ");
		treesql.append(" from " + table + " ");
		return treesql;
	}
	
	/**
	 * 记录数是否大于0
	 * @param lsData
	 * @return
	 */
	private boolean hasChild(List<Map<String,String>> lsData) {
		if (lsData.isEmpty()) return false;
		
		String cnt = lsData.get(0).get("cnt");
		
		return Integer.parseInt(cnt) > 0;
	}
	
	/**
	 * 根据父ID计算级别
	 * @param parentId -- 父ID
	 * @return
	 */
	private String getLevel(String parentId) {
		//点击根节点时，返回的级别
		if (parentId == null || parentId.length() == 0 || 
				parentId.equals(ROOT_ID)) {
			return "1";
		}
		//特殊树数据，第一级数据长度可能小于4
		if (parentId.length() < 4) {
			return "2";
		}
		//系统默认树形数据4为一级
		return Integer.toString(parentId.length()/4+1);
	}
	
	/**
	 * 解析树形过滤SQL中的常量，方便扩展自定义函数。
	 * @param selfWhere
	 * @param userId
	 * @return
	 */
	private String parseWhere(String selfWhere, String userId) {
		return selfWhere.replaceAll("\\{CURUSERID\\}", userId);
	}
	
	//取树型组定义信息
	private List<Map<String,String>> treeTeam(String funId) {
		String sql = "select team_id, tree_title as team_title from fun_tree where fun_id = ? order by tree_no";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(funId);
		List<Map<String,String>> lsTree = _dao.query(param);
		
		List<Map<String,String>> lsTeam = FactoryUtil.newList();
		Map<String,String> mpTeam = FactoryUtil.newMap();
		for (Map<String,String> mpTree : lsTree) {
			String team_id = MapUtil.getValue(mpTree, "team_id");
			String team_title = MapUtil.getValue(mpTree, "team_title");
			
			if (team_id.length() == 0 || mpTeam.containsKey(team_id)) continue;
			mpTeam.put(team_id, team_title);
			lsTeam.add(mpTree);
		}
		
		return lsTeam;
	}
}
