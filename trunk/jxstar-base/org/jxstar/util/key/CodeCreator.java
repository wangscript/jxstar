/*
 * CodeCreator.java 2010-11-4
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.util.key;

import java.util.Map;

import org.jxstar.dao.BaseDao;
import org.jxstar.dao.DaoParam;
import org.jxstar.service.define.DefineDataManger;
import org.jxstar.util.DateUtil;
import org.jxstar.util.MapUtil;
import org.jxstar.util.StringUtil;
import org.jxstar.util.config.SystemVar;
import org.jxstar.util.factory.FactoryUtil;
import org.jxstar.util.log.Log;

/**
 * 编码规则为：编码前缀+编码扩展+流水号；
 * 编码前缀：在功能注册信息中注册，也可以通过参数给值；
 * 编码扩展：缺省采用yyyyMM值，也可以通过参数给值；
 * 流水号：缺省采用六位0做填充，流水号采用六位，也可以通过参数给值；
 * 
 * 支持在系统设置表中统一设置编码的缺省值：
 * code.rule.format -- 编码扩展值，可以设置日期格式字符串，如：yyyyMM, yyyyMMdd等；
 * code.rule.serial -- 流水号位数与填充值，可以设置连续的几个0，如：000000；
 * 
 * 支持在功能自定义编码表中设置特殊编码规则，可以设置编码的扩展值取记录的字段值，修改日期格式，可以修改流水号的长度。
 * 
 * 特殊设计：
 * 1、在多线程高并发的情况下，由于业务操作是在一个事务中，出现最大值还没有更新，就要查询同一个表的最大值，这样会造成生成重复的编码；
 * 2、如果采用不缓存值，每次取一个编码来处理，则会每取一个编码就需要提交事务，不能采用公共事务，这样在性能测试时效率很低，是采用公共事务的1/10；
 * 3、最终确定设计方案，每次取一个超大的编码最大值，每次取新编码时都更新数据库中的最大值，这即解决了多线程不会取重复编码的问题又解决了性能问题与断码的问题；
 * 4、数据表中编码最大值更新要采用累加的方式，这样在多线程多事务时不会丢失累加值。
 * 
 * 此方案是一个比较优秀的序列号生成方案，此类在20个并发线程10万次请求测试通过。
 * 
 * 2012-04-30
 * 1、当两个会话同时更新sys_tablecode表的同一条记录时，如果事务时间长，会出现死锁的情况，现在改为独立connection，
 * 可以避免死锁的问题，但会造成在性能测试时连接数不够用；
 * 2、如果是在集群环境下，会出现编码重复的问题，而且可能会出现死锁，现在把每次分配编码数量调整为10个，可以解决此问题，
 * 但如果重启服务，则会出现断码的情况。
 * 
 * 2012-09-03
 * 1、针对链接数不够的问题，改回不取独立connection，由于每次取10个编码才去更新最大值，不会死锁，
 * 在性能测试时，但会存在外部事务没提交，update最大值没有提交， 会生成重复主键，重复数量都是10的倍数；
 *    
 * 最终结论：还是采用独立connection的方式，但取最大值时不取独立conn，在做性能测试时必须添加时间间隔50ms以上。
 * 
 * @author TonyTan
 * @version 1.0, 2010-11-4
 */
public class CodeCreator {
	private static CodeCreator _instance = new CodeCreator();
	
	private static Log _log = Log.getInstance();
	private static BaseDao _dao = BaseDao.getInstance();
	
	//每次取编码数量
	private static final int POOL_SIZE = 10;
	//缓存不同表的键对象
	private static Map<String, KeyInfo> _keyList = FactoryUtil.newMap();
	
	private CodeCreator() {}
	
	public static CodeCreator getInstance() {
		return _instance;
	}
	
	/**
	 * 根据功能ID,生成单据编码.
	 * 
	 * @param funId - 功能ID
	 * @return
	 */
	public String createCode(String funId) {		
		return createCode(funId, null);
	}
	
	/**
	 * 根据功能ID生成单据编码，系统新增类中使用。
	 * @param funId -- 功能ID
	 * @param mpValue -- 当前新增记录的编码
	 * @return
	 */
	public String createCode(String funId, Map<String,String> mpValue) {
		if (funId == null || funId.length() == 0) {
			_log.showWarn("new funcode param funid is null!!");
			return "";
		}
		
		//扩展格式
		String format = "";
		//编码扩展值
		String extValue = "";
		//编码流水号掩码
		String serialCode = "";
		//自定义规则
		Map<String, String> mpRule = null;
		
		//先检查该功能是否定义了编码规则
		String sql = "select code_ext, code_no, code_length from sys_coderule where fun_id = ?";
		DaoParam sparam = _dao.createParam(sql);
		sparam.addStringValue(funId);
		mpRule = _dao.queryMap(sparam);
		
		boolean custRule = false;
		//如果编码扩展掩码不为空，且(为“:日期格式”或者“有当前记录值”)
		format = MapUtil.getValue(mpRule, "code_ext");
		if (format.length() > 0 && (format.charAt(0) == ':' || mpValue != null)) {
			custRule = true;
		}
		
		//采用功能自定义编码规则
		if (custRule) {
			String code_len = MapUtil.getValue(mpRule, "code_length", "0");
			serialCode = mpRule.get("code_no");
			
			_log.showDebug("custom code rule code_ext={0} code_no={1} code_length={2}", 
					format, serialCode, code_len);
			
			extValue = getCodeExtend(format, mpValue);
			_log.showDebug("custom code rule extvalue={0}", extValue);
			
			//处理编码长度，如果扩展值+序列号的长度不够，则添加序列号的长度
			String tmpVal = extValue + serialCode;
			int len = Integer.parseInt(code_len);
			if (len > tmpVal.length()) {
				//需要添加的字符数
				int n = len - tmpVal.length();
				StringBuilder sbtmp = new StringBuilder();
				for (int i = 0; i < n; i++) {
					sbtmp.append(serialCode.charAt(0));
				}
				serialCode = sbtmp.append(serialCode).toString();
				_log.showDebug("custom code rule: serialcode add length after={0}", serialCode);
			}
		} else {
		//取系统通用编码规则
			//编码扩展格式，缺省yyyyMM
			format = SystemVar.getValue("code.rule.format", "yyyyMM");
			if (format.length() > 0) {
				extValue = DateUtil.getDateValue(format);
			}
			
			//编码流水号掩码，缺省000000
			serialCode = SystemVar.getValue("code.rule.serial", "000000");
		}
		
		return createCode(funId, extValue, serialCode);
	}
	
	/**
	 * 根据功能ID,生成单据编码.
	 * 
	 * @param funId - 功能ID
	 * @param extValue - 编码扩展值，缺省值为yyyymm
	 * @param serialCode -- 流水号掩码，缺省000000
	 * @return
	 */
	public String createCode(String funId, String extValue, String serialCode) {
		//取功能定义信息
		DefineDataManger manger = DefineDataManger.getInstance();
		Map<String,String> mpFun = manger.getFunData(funId);
		//编码前缀
		String prefix = mpFun.get("code_prefix");
		//取表名
		String tableName = mpFun.get("table_name");
		
		return createTableCode(tableName, prefix, extValue, serialCode);
	}
	
	/**
	 * 根据功能ID,生成单据编码.
	 * 
	 * @param tableName - 数据表
	 * @param tableName - 编码前缀
	 * @param extend - 编码扩展值，缺省值为yyyymm
	 * @param serialCode -- 流水号掩码，缺省000000
	 * @return
	 */
	public String createTableCode(String tableName, String prefix, String extend, String serialCode) {
		if (tableName == null || tableName.length() == 0) {
			_log.showWarn("tablename param is null!!");
			return "";
		}
		
		if (prefix == null) prefix = "";
		
		if (extend == null) extend = "";
		
		if (serialCode == null) serialCode = "";
		
		//编码前段值
		String codeValue = prefix + extend;
		//取流水号
		String serialNo = createSerialNo(tableName, codeValue, serialCode);
		
		//新编码
		String newCode = codeValue + serialNo;
		_log.showDebug("=====new code value=" + newCode);
		
		return newCode;
	}
	
	/**
	 * 取功能编码的扩展值
	 * @param format -- 扩展值格式码或字段名称，如果是日期格式则用":"开头
	 * @param mpValue -- 当前记录值
	 * @return
	 */
	private String getCodeExtend(String format, Map<String,String> mpValue) {
		String retVal = "";
		if (format == null || format.length() == 0) {
			return retVal;
		}
		
		format = format.trim();
		if (format.charAt(0) == ':') {
			retVal = DateUtil.getDateValue(format.substring(1));
		} else {
			retVal = MapUtil.getValue(mpValue, format);
			//如果没有取到值，则采用无表名的字段取值
			if (retVal.length() == 0) {
				String field = StringUtil.getNoTableCol(format);
				retVal = MapUtil.getValue(mpValue, field);
			}
		}
		
		return retVal;
	}
	
	/**
	 * 构建新建流水号
	 * @param tableName -- 表名
	 * @param extValue -- 编码扩展值，缺省采用年月（yyyymm）
	 * @param serialCode -- 流水号掩码，缺省000000
	 * @return
	 */
	private synchronized String createSerialNo(String tableName, String extValue, String serialCode) {
		KeyInfo keyInfo;
		
		//如果编码扩展信息为空，为方便生成编码，设置为null值
		if (extValue.length() == 0) {
			extValue = "null";
		}
		
		tableName = tableName.toLowerCase();
		String keyname = tableName+";"+extValue;
		if (_keyList.containsKey(keyname)) {
			keyInfo = _keyList.get(keyname);
		} else {
			//如果缓存对象数量超过1000，则清除所有缓存对象
			if (_keyList.size() > 1000) {
				_keyList.clear();
			}
			
			keyInfo = new KeyInfo(POOL_SIZE, tableName, extValue);
			_keyList.put(keyname, keyInfo);
		}
		
		//取新的序号
		int serial = keyInfo.getNextKey();
		String key = Integer.toString(serial);
		
		//如果掩码长度小于2，则直接返回流水号
		if (serialCode.length() < 2) {
			return key;
		}
		
		//如果流水号长度大于或等于掩码长度，则给出错误提示但继续有效
		if (key.length() > serialCode.length()) {
			_log.showWarn("codeno too length!! table={0} serial={1} key={2}!!", tableName, serialCode, key);
			return key;
		}
		
		//返回指定长度序号字符串，长度不足的在前面补零
		String code = serialCode + key;
		return code.substring(key.length(), code.length());
	}
	
	/**
	 * 主键唯一序号生成类
	 * 
	 * @author TonyTan
	 * @version 1.0, 2010-11-2
	 */
	private class KeyInfo {
		private int keyMax;
		private int keyMin;
		private int nextKey;
		private int poolSize;
		private String keyName;
		private String keyExtend;
		
		public KeyInfo(int poolSize, String keyName, String keyExtend) {
			this.poolSize = poolSize;
			this.keyName = keyName;
			this.keyExtend = keyExtend;
			
			retrieveFromDB();
		}
		
		public int getNextKey() {
			if (nextKey > keyMax) {
				retrieveFromDB();
			}

			return nextKey++;
		}
		
		private synchronized void retrieveFromDB() {
			//从数据库中取上次分配的最大值
			int dbmax = 0;
			String ssql = "select max_value from sys_tablecode where table_name = ? and code_ext = ?";
			DaoParam sparam = _dao.createParam(ssql);
			sparam.addStringValue(keyName);
			sparam.addStringValue(keyExtend);
			Map<String, String> mpMax = _dao.queryMap(sparam);
			
			if (!mpMax.isEmpty()) {
				dbmax = Integer.parseInt(mpMax.get("max_value"));
				
				//每次取新号时，更新当前最大值；采用累加poolSize的方式可以解决多线程多事务累加不丢失的问题。
				String usql = "update sys_tablecode set max_value = max_value + ? where table_name = ? and code_ext = ?";
				DaoParam uparam = _dao.createParam(usql);
				uparam.setUseTransaction(false);
				uparam.addIntValue(Integer.toString(poolSize));
				uparam.addStringValue(keyName);
				uparam.addStringValue(keyExtend);
				if (!_dao.update(uparam)) {
					_log.showWarn("get next code no update error!! tablename={0} extvalue={1}!!", keyName, keyExtend);
				}
			} else {
				//新建一条记录
				String usql = "insert into sys_tablecode(max_value, table_name, code_ext) values(?, ?, ?)";
				DaoParam uparam = _dao.createParam(usql);
				uparam.setUseTransaction(false);
				uparam.addIntValue(Integer.toString(poolSize));
				uparam.addStringValue(keyName);
				uparam.addStringValue(keyExtend);
				if (!_dao.update(uparam)) {
					_log.showWarn("get next code no update error!! tablename={0} extvalue={1}!!", keyName, keyExtend);
				}
			}
			
			keyMax = dbmax + poolSize;
			keyMin = dbmax + 1;
			nextKey = keyMin;
		}
	}
}
