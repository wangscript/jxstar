package org.jxstar.service.studio;


import org.jxstar.service.BoException;
import org.jxstar.service.util.WhereUtil;
import org.jxstar.test.AbstractTest;

public class DefineTest extends AbstractTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String funid = "run_malrecord";
		String user_id = "administor";
		String where_sql = "";
		try {
			where_sql = WhereUtil.queryWhere(funid, user_id, where_sql, "0");
		} catch (BoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(where_sql);
	}

}
